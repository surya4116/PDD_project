package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.data.local.entity.BookingEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenControlPanelScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {}
) {
    val bookingsWithDetails by viewModel.myBookingsWithDetails.collectAsState()
    val centers by viewModel.serviceCenters.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedCenterId by remember { mutableStateOf<Int?>(null) }
    
    var centerDropdownExpanded by remember { mutableStateOf(false) }

    val filteredBookings = bookingsWithDetails.filter {
        it.booking.tokenNumber.contains(searchQuery, ignoreCase = true) ||
        it.center.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Token Control Panel", fontWeight = FontWeight.Bold) },
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

            // Search Panel
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tokens by number or center...") },
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

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Manual Token Dispatcher Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Issue On-Site Token", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Customer Phone") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Select Service Center
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { centerDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    val sel = centers.find { it.id == selectedCenterId }
                                    Text(sel?.name ?: "Select Service Center", color = PrimaryPurple)
                                }
                                DropdownMenu(
                                    expanded = centerDropdownExpanded,
                                    onDismissRequest = { centerDropdownExpanded = false }
                                ) {
                                    centers.forEach { center ->
                                        DropdownMenuItem(
                                            text = { Text(center.name) },
                                            onClick = {
                                                selectedCenterId = center.id
                                                centerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    selectedCenterId?.let { centerId ->
                                        viewModel.sendNotification(1, "Walk-in Token Issued", "A manual token has been issued for $customerName.")
                                        customerName = ""
                                        customerPhone = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Text("Issue Token", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                item {
                    Text("Live Token Queue Status", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                items(filteredBookings.size) { index ->
                    val item = filteredBookings[index]
                    ProviderTokenCardItem(
                        tokenNumber = item.booking.tokenNumber,
                        centerName = item.center.name,
                        status = item.booking.status,
                        onCancel = { viewModel.updateBookingStatus(item.booking, "Cancelled") }
                    )
                }

                if (filteredBookings.isEmpty()) {
                    item {
                        Text("No active tokens found.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderTokenCardItem(tokenNumber: String, centerName: String, status: String, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tokenNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                Text(centerName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text("Status: $status", fontSize = 12.sp, color = TextSecondary)
            }
            if (status != "Cancelled" && status != "Completed") {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null
                ) {
                    Text("Cancel", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
