package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotAndTokenManagementScreen(
    viewModel: ProviderViewModel,
    serviceId: Int,
    onBack: () -> Unit = {},
    onCreate: () -> Unit = {}
) {
    val context = LocalContext.current
    
    val lastDate = remember { viewModel.getLastSlotDate().ifEmpty {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    } }
    val lastStartTime = remember { viewModel.getLastSlotStartTime().ifEmpty { "09:00 AM" } }
    val lastEndTime = remember { viewModel.getLastSlotEndTime().ifEmpty { "05:00 PM" } }
    val lastNumSlots = remember { viewModel.getLastSlotNumSlots() }
    val lastTokensPerSlot = remember { viewModel.getLastSlotTokenCount() }

    var date by remember { mutableStateOf(lastDate) }
    var startTime by remember { mutableStateOf(lastStartTime) }
    var endTime by remember { mutableStateOf(lastEndTime) }
    var numSlots by remember { mutableStateOf(lastNumSlots.toString()) }
    var tokensPerSlot by remember { mutableStateOf(lastTokensPerSlot.toString()) }

    LaunchedEffect(serviceId) {
        viewModel.getServiceById(serviceId) { service ->
            service?.let {
                val regex = Regex("""^(.+?)\s*-\s*(.+?)\s*\((\d+)\s*[Tt]okens\)$""")
                val matchResult = regex.matchEntire(it.name)
                if (matchResult != null) {
                    startTime = matchResult.groupValues[1]
                    endTime = matchResult.groupValues[2]
                    tokensPerSlot = matchResult.groupValues[3]
                    numSlots = "1"
                }
            }
        }
    }

    val timeFormat = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US) }
    
    val calculatedDuration = remember(startTime, endTime, numSlots) {
        try {
            val startCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(startTime) ?: java.util.Date() }
            val endCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(endTime) ?: java.util.Date() }
            
            var diffMs = endCal.timeInMillis - startCal.timeInMillis
            if (diffMs < 0) {
                diffMs += 24 * 60 * 60 * 1000 // Handle overnight shift
            }
            val totalMins = (diffMs / (60 * 1000)).toInt()
            val slots = numSlots.toIntOrNull() ?: 1
            if (slots > 0) totalMins / slots else 0
        } catch (e: Exception) {
            0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Work Slots", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val slots = numSlots.toIntOrNull() ?: 8
                        val tokens = tokensPerSlot.toIntOrNull() ?: 1
                        
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        if (date.isNotEmpty() && date < today) {
                            android.widget.Toast.makeText(context, "Cannot create slots for past dates", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (calculatedDuration > 0 && tokens > 0 && date.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                            viewModel.addSlotsBatch(
                                serviceId = serviceId,
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                durationMins = calculatedDuration,
                                tokensPerSlot = tokens
                            )
                            viewModel.saveLastSlotConfig(
                                startTime = startTime,
                                endTime = endTime,
                                numSlots = slots,
                                tokensPerSlot = tokens,
                                date = date
                            )
                            onCreate()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Generate Availability Slots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Set shift hours, number of slots, and capacity per slot to automatically generate availability.", color = TextSecondary)
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. 2026-10-14)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Shift Start (opens clock picker)
                Box(modifier = Modifier.weight(1f).clickable {
                    showTimePicker(context, startTime) { selectedTime -> startTime = selectedTime }
                }) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {},
                        label = { Text("Shift Start") },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Shift End (opens clock picker)
                Box(modifier = Modifier.weight(1f).clickable {
                    showTimePicker(context, endTime) { selectedTime -> endTime = selectedTime }
                }) {
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {},
                        label = { Text("Shift End") },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = numSlots,
                    onValueChange = { numSlots = it },
                    label = { Text("Number of Slots") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = tokensPerSlot,
                    onValueChange = { tokensPerSlot = it },
                    label = { Text("Tokens per Slot") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Auto-Calculated Duration display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryPurple.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Auto-Calculated Slot Duration:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$calculatedDuration minutes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun showTimePicker(context: android.content.Context, currentTime: String, onTimeSelected: (String) -> Unit) {
    val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
    val calendar = java.util.Calendar.getInstance()
    try {
        val date = timeFormat.parse(currentTime)
        if (date != null) calendar.time = date
    } catch (e: java.lang.Exception) {}

    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    android.app.TimePickerDialog(context, { _, h, m ->
        val selectedCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, h)
            set(java.util.Calendar.MINUTE, m)
        }
        onTimeSelected(timeFormat.format(selectedCal.time))
    }, hour, minute, false).show()
}
