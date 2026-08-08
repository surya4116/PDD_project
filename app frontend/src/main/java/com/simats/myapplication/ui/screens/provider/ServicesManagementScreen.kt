package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.data.local.entity.ServiceEntity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesManagementScreen(
    viewModel: ProviderViewModel,
    centerId: Int,
    onBack: () -> Unit = {},
    onServiceClick: (Int) -> Unit = {}
) {
    val services by remember(centerId) {
        viewModel.getServicesForCenter(centerId)
    }.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("09:30 AM") }
    var tokens by remember { mutableStateOf("5") }

    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var editStartTime by remember { mutableStateOf("09:00 AM") }
    var editEndTime by remember { mutableStateOf("09:30 AM") }
    var editTokens by remember { mutableStateOf("5") }

    val context = LocalContext.current
    val timeFormat = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US) }

    fun calculateDuration(start: String, end: String): Int {
        try {
            val startCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(start) ?: java.util.Date() }
            val endCal = java.util.Calendar.getInstance().apply { time = timeFormat.parse(end) ?: java.util.Date() }
            
            var diffMs = endCal.timeInMillis - startCal.timeInMillis
            if (diffMs < 0) {
                diffMs += 24 * 60 * 60 * 1000 // Handle overnight shift
            }
            return (diffMs / (60 * 1000)).toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    // Prefill edit values
    LaunchedEffect(editingService) {
        editingService?.let {
            val regex = Regex("""^(.+?)\s*-\s*(.+?)\s*\((\d+)\s*[Tt]okens\)$""")
            val matchResult = regex.matchEntire(it.name)
            if (matchResult != null) {
                editStartTime = matchResult.groupValues[1]
                editEndTime = matchResult.groupValues[2]
                editTokens = matchResult.groupValues[3]
            } else {
                editStartTime = "09:00 AM"
                editEndTime = "09:15 AM"
                editTokens = "5"
            }
        }
    }

    // Add Service Dialog
    if (showAddDialog) {
        val calculatedDuration = calculateDuration(startTime, endTime)
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Slot Timing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).clickable {
                            showTimePicker(context, startTime) { selectedTime -> startTime = selectedTime }
                        }) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = {},
                                label = { Text("Start Time") },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable {
                            showTimePicker(context, endTime) { selectedTime -> endTime = selectedTime }
                        }) {
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = {},
                                label = { Text("End Time") },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    OutlinedTextField(
                        value = tokens,
                        onValueChange = { tokens = it },
                        label = { Text("Number of Tokens") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Calculated Duration: $calculatedDuration mins",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (startTime.isNotEmpty() && endTime.isNotEmpty() && tokens.isNotEmpty()) {
                        val computedName = "$startTime - $endTime ($tokens Tokens)"
                        val computedDuration = "$calculatedDuration mins"
                        viewModel.addService(centerId, computedName, computedDuration)
                        startTime = "09:00 AM"
                        endTime = "09:30 AM"
                        tokens = "5"
                        showAddDialog = false
                    }
                }) { Text("Add Slot") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Service Dialog
    if (editingService != null) {
        val calculatedDuration = calculateDuration(editStartTime, editEndTime)
        AlertDialog(
            onDismissRequest = { editingService = null },
            title = { Text("Edit Slot Timing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).clickable {
                            showTimePicker(context, editStartTime) { selectedTime -> editStartTime = selectedTime }
                        }) {
                            OutlinedTextField(
                                value = editStartTime,
                                onValueChange = {},
                                label = { Text("Start Time") },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable {
                            showTimePicker(context, editEndTime) { selectedTime -> editEndTime = selectedTime }
                        }) {
                            OutlinedTextField(
                                value = editEndTime,
                                onValueChange = {},
                                label = { Text("End Time") },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editTokens,
                        onValueChange = { editTokens = it },
                        label = { Text("Number of Tokens") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Calculated Duration: $calculatedDuration mins",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingService?.let {
                        if (editStartTime.isNotEmpty() && editEndTime.isNotEmpty() && editTokens.isNotEmpty()) {
                            val computedName = "$editStartTime - $editEndTime ($editTokens Tokens)"
                            val computedDuration = "$calculatedDuration mins"
                            viewModel.updateService(it.copy(name = computedName, duration = computedDuration))
                            editingService = null
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingService = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provided Slots of the Day", fontWeight = FontWeight.Bold) },
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
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Slot")
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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(services.size) { index ->
                    val service = services[index]
                    ProviderServiceCard(
                        name = service.name, 
                        duration = service.duration, 
                        onClick = { onServiceClick(service.id) },
                        onEdit = { editingService = service },
                        onDelete = { viewModel.deleteService(service) }
                    )
                }
                if (services.isEmpty()) {
                    item {
                        Text("No slots configured yet. Click + to add your daily slots.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderServiceCard(
    name: String, 
    duration: String, 
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Duration: $duration", fontSize = 14.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryPurple)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
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
}}

