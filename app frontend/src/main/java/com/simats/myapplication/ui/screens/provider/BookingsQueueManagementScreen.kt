package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsQueueManagementScreen(
    viewModel: ProviderViewModel,
    slotId: Int,
    onBack: () -> Unit = {},
    onCompleteClick: (Int) -> Unit = {}
) {
    // Safely collect Flow by remembering the instance across recompositions
    val queueFlow = remember(slotId) { viewModel.getActiveQueueForSlot(slotId) }
    val queue by queueFlow.collectAsState(initial = emptyList())
    val currentlyServing = queue.firstOrNull { it.status == "Called" }
    val waitingList = queue.filter { it.status == "Waiting" || it.status == "In Premise" }

    var showDelayDialog by remember { mutableStateOf(false) }
    var delayMins by remember { mutableStateOf("") }

    if (showDelayDialog) {
        AlertDialog(
            onDismissRequest = { showDelayDialog = false },
            title = { Text("Set Slot Delay") },
            text = {
                OutlinedTextField(
                    value = delayMins,
                    onValueChange = { delayMins = it },
                    label = { Text("Delay in Minutes") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val delay = delayMins.toIntOrNull() ?: 0
                    viewModel.setSlotDelay(slotId, delay)
                    showDelayDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDelayDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Job Queue", fontWeight = FontWeight.Bold) },
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

            // Active Serving Banner
            if (currentlyServing != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp), spotColor = WarningOrange),
                    shape = RoundedCornerShape(16.dp),
                    color = WarningOrange
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Currently Serving Customer", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text("${currentlyServing.tokenNumber} | Booking ID: ${currentlyServing.id}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.skipBooking(currentlyServing.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Skip", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onCompleteClick(currentlyServing.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete", color = WarningOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active token called", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.callNextBooking(slotId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Call Next Customer", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Waiting Queue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                TextButton(onClick = { showDelayDialog = true }) {
                    Text("Set Delay", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(waitingList.size) { index ->
                    val booking = waitingList[index]
                    ProviderQueueUserCard(booking.tokenNumber, "Booking ID: ${booking.id}", booking.status, SuccessGreen)
                }
                if (waitingList.isEmpty()) {
                    item {
                        Text("Waiting list is empty.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderQueueUserCard(token: String, name: String, status: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(token, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(status, fontSize = 12.sp, color = statusColor)
            }
            IconButton(onClick = { /* View Details */ }) {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = "View", modifier = Modifier.size(16.dp), tint = TextSecondary)
            }
        }
    }
}
