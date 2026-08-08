package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.data.local.entity.BookingWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManagementScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {}
) {
    val bookingsWithDetails by viewModel.myBookingsWithDetails.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf(viewModel.initialBookingFilter) }
    fun getTodayString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    var currentDate by remember { mutableStateOf(getTodayString()) }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var isTrackingToday by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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

    LaunchedEffect(uiState) {
        uiState?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiState()
        }
    }

    val filteredBookings = bookingsWithDetails.filter {
        val matchesSearch = it.booking.tokenNumber.contains(searchQuery, ignoreCase = true) ||
                it.center.name.contains(searchQuery, ignoreCase = true)
        
        val matchesTab = when (selectedFilterTab) {
            "Waiting" -> it.booking.status == "Waiting" || it.booking.status == "Called"
            "In Premise" -> it.booking.status == "In Premise" || it.booking.status == "Called"
            "Completed" -> it.booking.status == "Completed"
            "Cancelled" -> it.booking.status == "Cancelled" || it.booking.status == "Failed"
            else -> true
        }
        val matchesDate = if (selectedDate.isNotEmpty()) it.slot.date == selectedDate else true
        matchesSearch && matchesTab && matchesDate
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Customer Requests & Jobs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
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
                placeholder = { Text("Search customer bookings...") },
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
                        // "Today" Quick Chip
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

                        // "Choose Date" Picker Button
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

                        // "All Dates" Chip
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
                listOf("All", "Waiting", "In Premise", "Completed", "Cancelled").forEach { tab ->
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
                items(filteredBookings.size) { index ->
                    val item = filteredBookings[index]
                    ProviderBookingCard(
                        tokenNumber = item.booking.tokenNumber,
                        centerName = item.center.name,
                        userName = item.booking.userName ?: "Unknown",
                        status = item.booking.status,
                        slotDate = item.slot.date,
                        startTime = item.slot.startTime,
                        endTime = item.slot.endTime,
                        queuePosition = item.booking.queuePosition,
                        delayMins = item.slot.delayMins,
                        onApprove = { viewModel.updateBookingStatus(item.booking, "In Premise") },
                        onReject = { viewModel.updateBookingStatus(item.booking, "Failed") },
                        onCancel = { viewModel.updateBookingStatus(item.booking, "Cancelled") },
                        onComplete = { viewModel.updateBookingStatus(item.booking, "Completed") }
                    )
                }

                if (filteredBookings.isEmpty()) {
                    item {
                        Text("No customer requests match your criteria.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderBookingCard(
    tokenNumber: String,
    centerName: String,
    userName: String,
    status: String,
    slotDate: String,
    startTime: String,
    endTime: String,
    queuePosition: Int = 0,
    delayMins: Int = 0,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Token Badge & Status Pill
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
                            Icons.Default.ConfirmationNumber,
                            contentDescription = "Token",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            tokenNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                        if (queuePosition > 0) {
                            Text(
                                "(#$queuePosition)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryPurple.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                val (statusBg, statusText) = when (status) {
                    "Waiting" -> Pair(WarningOrange.copy(alpha = 0.15f), WarningOrange)
                    "In Premise", "Called" -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                    "Completed" -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                    "Cancelled", "Failed" -> Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
                    else -> Pair(Color.LightGray.copy(alpha = 0.2f), TextSecondary)
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = status,
                        color = statusText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details Section: Customer & Shop Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = "Shop",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = centerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slot & Time Details Container
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
                                text = "Date: $slotDate",
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
                                Icons.Default.Schedule,
                                contentDescription = "Time Slot",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "$startTime - $endTime",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                    }

                    if (delayMins > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Slot Delay: +$delayMins mins",
                                color = WarningOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (status == "Waiting") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else if (status == "In Premise" || status == "Called") {
                Spacer(modifier = Modifier.height(14.dp))

                // Primary action: Mark as Completed (full width, prominent)
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Mark as Completed",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary action: Cancel (subtle text button)
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = ErrorRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Cancel Appointment",
                        color = ErrorRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
