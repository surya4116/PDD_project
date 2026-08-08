package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.simats.myapplication.ui.viewmodel.UserViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.data.local.entity.SlotEntity

import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotBookingScreen(
    viewModel: UserViewModel,
    centerId: Int,
    onBack: () -> Unit = {},
    onConfirmBooking: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val slotsFlow = remember(centerId) { viewModel.getSlotsForCenter(centerId) }
    val rawSlots by slotsFlow.collectAsState(initial = emptyList())
    val serviceCenters by viewModel.serviceCenters.collectAsState()
    val center = serviceCenters.find { it.id == centerId }

    var selectedSlotId by remember { mutableStateOf<Int?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun getTodayString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    var currentDate by remember { mutableStateOf(getTodayString()) }
    var selectedDate by remember { mutableStateOf(currentDate) }

    // Clear previous uiState on screen launch and update currentDate ticker for live responsiveness
    LaunchedEffect(Unit) {
        viewModel.clearUiState()
        while (true) {
            val nowStr = getTodayString()
            if (nowStr != currentDate) {
                currentDate = nowStr
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    // Auto-generate slots for custom picked date if missing
    LaunchedEffect(selectedDate) {
        if (selectedDate.isNotEmpty() && rawSlots.none { it.date == selectedDate }) {
            viewModel.generateSlotsForDate(centerId, selectedDate)
        }
    }

    fun computeSlotStatus(slot: SlotEntity): String {
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = sdfDate.format(java.util.Date())

        if (slot.date.isNotEmpty() && slot.date < todayStr) return "Completed"
        if (slot.status == "Completed" || slot.status == "Cancelled") return slot.status
        // Trust server-side status if it says Running
        if (slot.status == "Running") return "Running"

        if (slot.date.isNotEmpty() && slot.date == todayStr) {
            try {
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                val nowTimeStr = timeFormat.format(java.util.Date())
                val nowCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(nowTimeStr) ?: java.util.Date() }
                val endCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(slot.endTime) ?: java.util.Date() }
                val startCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(slot.startTime) ?: java.util.Date() }

                return when {
                    nowCal.after(endCal) -> "Completed"
                    nowCal.after(startCal) || nowCal == startCal -> "Running"
                    else -> if (slot.currentTokens >= slot.maxTokens) "Full" else "Upcoming"
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return if (slot.currentTokens >= slot.maxTokens) "Full" else "Upcoming"
    }

    val availableDates = remember(rawSlots, currentDate) {
        val dates = rawSlots.map { it.date }.filter { it.isNotEmpty() }.distinct().sorted()
        if (dates.isEmpty()) listOf(currentDate) else dates
    }

    LaunchedEffect(availableDates) {
        if (selectedDate.isNotEmpty() && !availableDates.contains(selectedDate)) {
            val todayAvailable = availableDates.find { it == currentDate }
            selectedDate = todayAvailable ?: availableDates.firstOrNull() ?: currentDate
        }
    }

    val validSlots = rawSlots.filter { slot ->
        val isDateMatch = if (selectedDate.isNotEmpty()) slot.date == selectedDate else true
        isDateMatch
    }

    val selectedSlot = rawSlots.find { it.id == selectedSlotId }
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state != null && !hasNavigated) {
            if (state.startsWith("Booking Successful:")) {
                hasNavigated = true
                isBooking = false
                val bookingIdStr = state.substringAfter("Booking Successful:")
                val bookingId = bookingIdStr.toIntOrNull() ?: -1
                viewModel.clearUiState()
                if (bookingId > 0) {
                    onConfirmBooking(bookingId)
                } else {
                    errorMessage = "Invalid Booking ID received"
                }
            } else if (state.startsWith("Error:")) {
                isBooking = false
                errorMessage = state.substringAfter("Error:")
                viewModel.clearUiState()
            }
        }
    }

    if (isBooking) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Booking Error", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Slot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Live", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Text("Live Updates", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(16.dp)
                    .shadow(8.dp, spotColor = Color.Black.copy(0.05f))
            ) {
                if (selectedSlot != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryPurple.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                Text("Selected Slot:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("${selectedSlot.startTime} - ${selectedSlot.endTime}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryPurple)
                            }
                            Text(selectedSlot.date, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Waiting Time:", color = TextSecondary)
                    Text("~15 mins", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        selectedSlotId?.let {
                            isBooking = true
                            viewModel.bookSlot(it, centerId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = selectedSlotId != null && !isBooking
                ) {
                    Text("Confirm Booking", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Service Center Info Banner Card
            if (center != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryPurple.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = "Center",
                                tint = PrimaryPurple,
                                modifier = Modifier.padding(12.dp).size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(center.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = TextSecondary, modifier = Modifier.size(13.dp))
                                Text(center.address, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Select Date Section
            Text(
                text = "Select Date",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "All Dates" chip
                val isAllSelected = selectedDate.isEmpty()
                Surface(
                    modifier = Modifier.clickable {
                        selectedDate = ""
                        selectedSlotId = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAllSelected) PrimaryPurple else SurfaceWhite,
                    border = if (!isAllSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)) else null
                ) {
                    Text(
                        text = "All Dates",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) Color.White else TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                availableDates.forEach { dateStr ->
                    val isSelected = selectedDate == dateStr
                    val label = if (dateStr == currentDate) "Today ($dateStr)" else dateStr
                    Surface(
                        modifier = Modifier.clickable {
                            selectedDate = dateStr
                            selectedSlotId = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryPurple else SurfaceWhite,
                        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = if (isSelected) Color.White else PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                // "Choose Date" Date Picker Button
                val isCustomSelected = selectedDate.isNotEmpty() && !availableDates.contains(selectedDate) && selectedDate != currentDate
                Surface(
                    modifier = Modifier.clickable {
                        val calendar = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val formattedMonth = String.format("%02d", month + 1)
                                val formattedDay = String.format("%02d", dayOfMonth)
                                val picked = "$year-$formattedMonth-$formattedDay"
                                selectedDate = picked
                                selectedSlotId = null
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCustomSelected) PrimaryPurple else SurfaceWhite,
                    border = if (!isCustomSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = "Pick Date",
                            tint = if (isCustomSelected) Color.White else PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCustomSelected) selectedDate else "Choose Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomSelected) Color.White else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Available Times Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Slots",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${validSlots.size} Slots Available",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots Grid
            if (validSlots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No available work slots for the selected date.", color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                val chunkedSlots = validSlots.chunked(2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chunkedSlots.forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val status = computeSlotStatus(slot)
                                // A slot is available to book if it's not full, completed, or cancelled
                                val isAvailable = slot.currentTokens < slot.maxTokens &&
                                    status != "Completed" && status != "Cancelled"
                                Box(modifier = Modifier.weight(1f)) {
                                    TimeSlotChip(
                                        slotDate = slot.date,
                                        startTime = slot.startTime,
                                        endTime = slot.endTime,
                                        bookedCount = slot.currentTokens,
                                        maxTokens = slot.maxTokens,
                                        isAvailable = isAvailable,
                                        isSelected = selectedSlotId == slot.id,
                                        showDate = selectedDate.isEmpty(),
                                        status = status,
                                        onClick = { if (isAvailable) selectedSlotId = slot.id }
                                    )
                                }
                            }
                            if (rowSlots.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TimeSlotChip(
    slotDate: String,
    startTime: String,
    endTime: String,
    bookedCount: Int,
    maxTokens: Int,
    isAvailable: Boolean,
    isSelected: Boolean,
    showDate: Boolean = false,
    status: String = "Upcoming",
    onClick: () -> Unit
) {
    val isRunning = status == "Running"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = when {
            isSelected -> PrimaryPurple
            isRunning && isAvailable -> Color(0xFF1B5E20).copy(alpha = 0.08f)
            !isAvailable -> Color.LightGray.copy(alpha = 0.25f)
            else -> SurfaceWhite
        },
        border = BorderStroke(
            width = if (isSelected || isRunning) 2.dp else 1.dp,
            color = when {
                isSelected -> PrimaryPurple
                isRunning && isAvailable -> SuccessGreen
                !isAvailable -> Color.Transparent
                else -> Color.LightGray.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Active Now" badge for running slots
            if (isRunning && isAvailable) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreen,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "🟢 Active Now",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = when {
                        isSelected -> Color.White
                        isRunning && isAvailable -> SuccessGreen
                        !isAvailable -> Color.Gray
                        else -> PrimaryPurple
                    },
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$startTime - $endTime",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isSelected -> Color.White
                        isRunning && isAvailable -> SuccessGreen
                        !isAvailable -> Color.Gray
                        else -> TextPrimary
                    },
                    textAlign = TextAlign.Center
                )
            }
            if (showDate && slotDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = slotDate,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val statusText = when {
                status == "Completed" -> "Time Passed"
                status == "Cancelled" -> "Cancelled"
                !isAvailable -> "Fully Booked"
                else -> "($bookedCount/$maxTokens Booked)"
            }
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else if (!isAvailable) ErrorRed else TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

