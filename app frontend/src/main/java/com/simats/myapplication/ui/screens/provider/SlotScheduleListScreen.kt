package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.data.local.entity.SlotEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotScheduleListScreen(
    viewModel: ProviderViewModel,
    serviceId: Int,
    onBack: () -> Unit = {},
    onAddSlot: () -> Unit = {},
    onScheduleClick: (Int) -> Unit = {}
) {
    val slots by remember(serviceId) {
        viewModel.getSlotsForService(serviceId)
    }.collectAsState()

    var slotToDelete by remember { mutableStateOf<SlotEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("All") }

    fun getTodayString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    var currentDate by remember { mutableStateOf(getTodayString()) }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var isTrackingToday by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Auto-refresh when a new day starts (timer polling)
    LaunchedEffect(Unit) {
        while (true) {
            val nowStr = getTodayString()
            if (nowStr != currentDate) {
                currentDate = nowStr
                if (isTrackingToday) {
                    selectedDate = nowStr
                }
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    // Refresh today's date when screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val nowStr = getTodayString()
                if (nowStr != currentDate) {
                    currentDate = nowStr
                    if (isTrackingToday) {
                        selectedDate = nowStr
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (slotToDelete != null) {
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            title = { Text("Delete Work Slot", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the slot: ${slotToDelete?.startTime} - ${slotToDelete?.endTime}? All booked appointments/tokens under this slot will be cancelled.") },
            confirmButton = {
                Button(
                    onClick = {
                        slotToDelete?.let { viewModel.deleteSlot(it) }
                        slotToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete Slot", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    fun computeSlotStatus(slot: SlotEntity): String {
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = sdfDate.format(java.util.Date())
        
        if (slot.date.isNotEmpty() && slot.date < todayStr) return "Completed"
        if (slot.status == "Completed" || slot.status == "Cancelled") return slot.status
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
                    else -> "Upcoming"
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return "Upcoming"
    }

    val filteredSlots = slots.filter { slot ->
        val computedStatus = computeSlotStatus(slot)
        val matchesSearch = slot.startTime.contains(searchQuery, ignoreCase = true) ||
                slot.endTime.contains(searchQuery, ignoreCase = true) ||
                slot.date.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedFilterTab) {
            "Active Now" -> computedStatus == "Running"
            "Upcoming" -> computedStatus == "Upcoming"
            "Completed" -> computedStatus == "Completed" || computedStatus == "Cancelled"
            else -> true
        }

        val matchesDate = if (selectedDate.isNotEmpty()) slot.date == selectedDate else true

        matchesSearch && matchesTab && matchesDate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Work Slots", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSlot,
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Work Slot")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search available slots...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PrimaryPurple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Date Selection & Filter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header: Selected Date Status Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryPurple.copy(alpha = 0.12f)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.padding(6.dp).size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Date Filter",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when {
                                        selectedDate.isEmpty() -> "All Dates"
                                        selectedDate == currentDate -> "Today ($selectedDate)"
                                        else -> selectedDate
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        if (selectedDate == currentDate) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Live Today",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Action Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isTodaySelected = selectedDate == currentDate
                        FilterChip(
                            selected = isTodaySelected,
                            onClick = {
                                selectedDate = currentDate
                                isTrackingToday = true
                            },
                            label = { Text("Today", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White,
                                containerColor = BackgroundWhite,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isTodaySelected,
                                borderColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedBorderColor = PrimaryPurple
                            )
                        )

                        Button(
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val formattedMonth = String.format("%02d", month + 1)
                                        val formattedDay = String.format("%02d", dayOfMonth)
                                        val picked = "$year-$formattedMonth-$formattedDay"
                                        selectedDate = picked
                                        isTrackingToday = (picked == currentDate)
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDate.isNotEmpty() && selectedDate != currentDate) PrimaryPurple else PrimaryPurple.copy(alpha = 0.12f),
                                contentColor = if (selectedDate.isNotEmpty() && selectedDate != currentDate) Color.White else PrimaryPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = "Pick Date",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedDate.isNotEmpty() && selectedDate != currentDate) selectedDate else "Choose Date",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        val isAllSelected = selectedDate.isEmpty()
                        FilterChip(
                            selected = isAllSelected,
                            onClick = {
                                selectedDate = ""
                                isTrackingToday = false
                            },
                            label = { Text("All", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TextPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = BackgroundWhite,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAllSelected,
                                borderColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedBorderColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active Now", "Upcoming", "Completed").forEach { tab ->
                    val isSelected = selectedFilterTab == tab
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryPurple else SurfaceWhite,
                        modifier = Modifier
                            .clickable { selectedFilterTab = tab }
                            .shadow(2.dp, RoundedCornerShape(20.dp)),
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredSlots.size) { index ->
                    val slot = filteredSlots[index]
                    val computedStatus = computeSlotStatus(slot)
                    ProviderSlotCard(
                        slot = slot,
                        computedStatus = computedStatus,
                        onClick = { onScheduleClick(slot.id) },
                        onDelete = { slotToDelete = slot }
                    )
                }
                if (filteredSlots.isEmpty()) {
                    item {
                        Text("No working slots match your filter criteria.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSlotCard(
    slot: SlotEntity,
    computedStatus: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Time Slot Badge & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryPurple.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${slot.startTime} - ${slot.endTime}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }

                val (statusBg, statusText) = when (computedStatus) {
                    "Running" -> Pair(SuccessGreen, Color.White)
                    "Upcoming" -> Pair(PrimaryPurple.copy(alpha = 0.15f), PrimaryPurple)
                    "Completed" -> Pair(Color.LightGray.copy(alpha = 0.4f), TextSecondary)
                    "Cancelled" -> Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
                    else -> Pair(PrimaryPurple.copy(alpha = 0.15f), PrimaryPurple)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    val displayStatus = when(computedStatus) {
                        "Running" -> "🟢 Active Now"
                        "Completed" -> "Time Passed"
                        else -> computedStatus
                    }
                    Text(
                        text = displayStatus,
                        color = statusText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Embedded Details Container: Date & Capacity Progress
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = BackgroundWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Date: ${slot.date}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Tokens",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${slot.currentTokens}/${slot.maxTokens} Tokens",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Capacity Progress Bar
                    val progress = if (slot.maxTokens > 0) (slot.currentTokens.toFloat() / slot.maxTokens.toFloat()).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = if (progress >= 1.0f) ErrorRed else PrimaryPurple,
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    if (slot.delayMins > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Slot Delay: +${slot.delayMins} mins",
                                color = WarningOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Manage Queue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(42.dp).background(ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

