package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: ProviderViewModel,
    onSettingsClick: () -> Unit = {},
    onQueuesClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onTokenControlClick: () -> Unit = {},
    onBookingManagementClick: () -> Unit = {},
    onNotificationDispatcherClick: () -> Unit = {},
    onQRCheckInClick: () -> Unit = {},
    onProviderProfileClick: () -> Unit = {}
) {
    val myBookingsWithDetails by viewModel.myBookingsWithDetails.collectAsState()
    val myServiceCenters by viewModel.serviceCenters.collectAsState()
    val currentProvider by viewModel.currentProvider.collectAsState()
    val providerName = currentProvider?.name ?: "Service Provider"
    val shopName = currentProvider?.shopName ?: ""

    val dynamicTotalBookings = myBookingsWithDetails.size
    val dynamicCompletedBookings = myBookingsWithDetails.count { it.booking.status == "Completed" }
    val dynamicActiveTokens = myBookingsWithDetails.count { it.booking.status == "Waiting" || it.booking.status == "In Premise" || it.booking.status == "Called" }
    val dynamicMissedAppointments = myBookingsWithDetails.count { it.booking.status == "Expired" || it.booking.status == "Cancelled" || it.booking.status == "Failed" }
    val dynamicWaitingCustomers = myBookingsWithDetails.count { it.booking.status == "Waiting" || it.booking.status == "In Premise" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Provider Portal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (shopName.isNotEmpty()) {
                            Text(shopName, fontSize = 12.sp, color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationDispatcherClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Dispatch Notification", tint = TextPrimary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryPurple, selectedTextColor = PrimaryPurple)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Services") },
                    label = { Text("Services") },
                    selected = false,
                    onClick = onQueuesClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProviderProfileClick
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = PrimaryPurple.copy(0.15f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome back,", fontSize = 14.sp, color = Color.White.copy(0.8f))
                                Text(providerName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Service Provider Operations Command", fontSize = 12.sp, color = Color.White.copy(0.85f))
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(0.2f),
                                modifier = Modifier.clickable { onProviderProfileClick() }
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.padding(12.dp).size(26.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.initialBookingFilter = "All"
                                    onBookingManagementClick()
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Bookings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onQueuesClick,
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Services", color = PrimaryPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Operational Stat Overview Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Service Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProviderStatCard(title = "Total Requests", value = "$dynamicTotalBookings", icon = Icons.Default.Assignment, modifier = Modifier.weight(1f), color = PrimaryPurple, onClick = {
                        viewModel.initialBookingFilter = "All"
                        onBookingManagementClick()
                    })
                    ProviderStatCard(title = "In Queue", value = "$dynamicWaitingCustomers", icon = Icons.Default.HourglassEmpty, modifier = Modifier.weight(1f), color = WarningOrange, onClick = {
                        viewModel.initialBookingFilter = "Waiting"
                        onBookingManagementClick()
                    })
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProviderStatCard(title = "Active Tokens", value = "$dynamicActiveTokens", icon = Icons.Default.ConfirmationNumber, modifier = Modifier.weight(1f), color = SuccessGreen, onClick = {
                        viewModel.initialBookingFilter = "In Premise"
                        onBookingManagementClick()
                    })
                    ProviderStatCard(title = "Completed", value = "$dynamicCompletedBookings", icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f), color = Color(0xFF1976D2), onClick = {
                        viewModel.initialBookingFilter = "Completed"
                        onBookingManagementClick()
                    })
                }
            }

            // Quick Actions Section Header
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Service Provider Tools", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ProviderQuickActionCard("Services & Slots", Icons.Default.Storefront, onQueuesClick, Modifier.weight(1f))
                        ProviderQuickActionCard("Token Control Panel", Icons.Default.ConfirmationNumber, onTokenControlClick, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ProviderQuickActionCard("Customer Bookings", Icons.Default.Assignment, onBookingManagementClick, Modifier.weight(1f))
                        ProviderQuickActionCard("QR Scanner Check-In", Icons.Default.QrCodeScanner, onQRCheckInClick, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ProviderQuickActionCard("Dispatch Alert", Icons.Default.NotificationsActive, onNotificationDispatcherClick, Modifier.weight(1f))
                        ProviderQuickActionCard("Reports & Analytics", Icons.Default.BarChart, onAnalyticsClick, Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProviderQuickActionCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = PrimaryPurple)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
        }
    }
}

@Composable
fun ProviderStatCard(
    title: String, 
    value: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier, 
    color: Color = PrimaryPurple,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.padding(6.dp).size(20.dp))
                }
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
    }
}
