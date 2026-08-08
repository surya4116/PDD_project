package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.simats.myapplication.ui.viewmodel.UserViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    viewModel: UserViewModel,
    onCenterClick: (Int) -> Unit = {},
    onTokensClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val categories by viewModel.categories.collectAsState()
    val serviceCenters by viewModel.serviceCenters.collectAsState()
    val allServiceCenters by viewModel.allServiceCenters.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val currentUser by viewModel.currentUser.collectAsState()
    val userName = currentUser?.name ?: "User"

    val myBookingsWithDetails by viewModel.userBookingsWithDetails.collectAsState()
    val dynamicTotalBookings = myBookingsWithDetails.size
    val dynamicActiveTokens = myBookingsWithDetails.count { it.booking.status == "Waiting" || it.booking.status == "In Premise" || it.booking.status == "Called" }
    val dynamicCompletedBookings = myBookingsWithDetails.count { it.booking.status == "Completed" }

    var selectedLocation by remember { mutableStateOf("Poonamallee, Chennai") }
    var showLocationDialog by remember { mutableStateOf(false) }

    val popularLocations = remember {
        listOf(
            "Poonamallee, Chennai",
            "Velachery, Chennai",
            "Guindy, Chennai",
            "Tambaram, Chennai",
            "Anna Nagar, Chennai",
            "T. Nagar, Chennai",
            "Adyar, Chennai",
            "Chromepet, Chennai",
            "Porur, Chennai",
            "Koyambedu, Chennai",
            "Mylapore, Chennai",
            "Egmore, Chennai"
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Select Location", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose your surrounding location to find nearby services:", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Detect GPS Location Button
                    Button(
                        onClick = {
                            selectedLocation = "Poonamallee, Chennai"
                            showLocationDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-Detect GPS Location", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(popularLocations.size) { index ->
                            val loc = popularLocations[index]
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLocation = loc
                                        showLocationDialog = false
                                    },
                                color = if (selectedLocation == loc) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(loc, fontSize = 14.sp, fontWeight = if (selectedLocation == loc) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Close", color = PrimaryPurple)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPurple,
                        selectedTextColor = PrimaryPurple,
                        indicatorColor = PrimaryPurpleLight.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Tokens") },
                    label = { Text("Tokens") },
                    selected = false,
                    onClick = onTokensClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { paddingValues ->
        val activeServiceCenters = remember(serviceCenters) {
            serviceCenters.filter { it.isActive }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Welcome back, $userName!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Find your service center and skip the queue", fontSize = 14.sp, color = TextSecondary)
                    }
                    Surface(
                        shape = CircleShape,
                        color = PrimaryPurpleLight.copy(alpha = 0.2f),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onNotificationsClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryPurple)
                            if (unreadCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = ErrorRed,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$unreadCount",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Metric Cards Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserStatCard(
                            title = "Total Bookings",
                            value = "$dynamicTotalBookings",
                            icon = Icons.Default.ConfirmationNumber,
                            iconBgColor = PrimaryPurple.copy(alpha = 0.1f),
                            iconTintColor = PrimaryPurple,
                            modifier = Modifier.weight(1f)
                        )
                        UserStatCard(
                            title = "Active Tokens",
                            value = "$dynamicActiveTokens",
                            icon = Icons.Default.Science,
                            iconBgColor = SuccessGreen.copy(alpha = 0.1f),
                            iconTintColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    UserStatCard(
                        title = "Completed Bookings",
                        value = "$dynamicCompletedBookings",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = Color(0xFFE3F2FD),
                        iconTintColor = Color(0xFF1976D2),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Location Selector Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryPurple.copy(alpha = 0.08f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLocationDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = PrimaryPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Active Location", fontSize = 11.sp, color = TextSecondary)
                                Text(selectedLocation, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryPurple
                        ) {
                            Text("Change", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search service centers by name or location...") },
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

                Text("Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                // Categories Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        val icon = when (cat.iconName) {
                            "LocalHospital" -> Icons.Default.LocalHospital
                            "AccountBalance" -> Icons.Default.AccountBalance
                            "ContentCut" -> Icons.Default.ContentCut
                            "MedicalServices" -> Icons.Default.MedicalServices
                            "Category" -> Icons.Default.Category
                            else -> Icons.Default.Category
                        }
                        val count = allServiceCenters.count { it.categoryId == cat.id && it.isActive }
                        CategoryItem(
                            icon = icon, 
                            title = cat.name,
                            count = count,
                            isSelected = selectedCategory == cat.id,
                            onClick = { viewModel.selectCategory(cat.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Nearby Centers", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nearby Centers List
            items(activeServiceCenters.size) { index ->
                val center = activeServiceCenters[index]
                val mockTravelTime = (center.id * 3 + 12) // Mock travel time calculation
                val cat = categories.find { it.id == center.categoryId }
                val icon = when (cat?.iconName) {
                    "LocalHospital" -> Icons.Default.LocalHospital
                    "AccountBalance" -> Icons.Default.AccountBalance
                    "ContentCut" -> Icons.Default.ContentCut
                    "MedicalServices" -> Icons.Default.MedicalServices
                    "Category" -> Icons.Default.Category
                    else -> Icons.Default.Store
                }
                ServiceCenterCard(
                    name = center.name,
                    category = cat?.name ?: "Unknown",
                    address = center.address,
                    distance = "${mockTravelTime} mins drive",
                    time = "Available Now",
                    icon = icon,
                    onClick = { onCenterClick(center.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (activeServiceCenters.isEmpty() && searchQuery.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = "Empty", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No service centers available", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else if (activeServiceCenters.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Text("No service centers found for '$searchQuery'.", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(icon: ImageVector, title: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) PrimaryPurple else SurfaceWhite,
            modifier = Modifier
                .size(64.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f))
        ) {
            Icon(icon, contentDescription = title, tint = if (isSelected) Color.White else PrimaryPurple, modifier = Modifier.padding(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, color = if (isSelected) PrimaryPurple else TextPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        Text("$count centers", fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun ServiceCenterCard(
    name: String,
    category: String,
    address: String,
    distance: String,
    time: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = category, tint = PrimaryPurple)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(category, fontSize = 12.sp, color = TextSecondary)
                if (address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(address, fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Distance", modifier = Modifier.size(12.dp), tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(distance, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = "Time", modifier = Modifier.size(12.dp), tint = WarningOrange)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, fontSize = 12.sp, color = WarningOrange, fontWeight = FontWeight.Medium)
                }
            }
            
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun UserStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTintColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDashboardPreview() {
    SmartQTheme {
        // UserDashboardScreen()
    }
}
