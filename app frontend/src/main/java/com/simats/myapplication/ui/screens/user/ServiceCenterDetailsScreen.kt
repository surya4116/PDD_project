package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.simats.myapplication.ui.viewmodel.UserViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCenterDetailsScreen(
    viewModel: UserViewModel,
    centerId: Int,
    onBack: () -> Unit = {},
    onBookSlot: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val serviceCenters by viewModel.serviceCenters.collectAsState()
    val center = serviceCenters.find { it.id == centerId }
    val feedbacks by remember(centerId) { viewModel.getFeedbackForCenter(centerId) }.collectAsState()

    if (center == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Center Details", fontWeight = FontWeight.Bold) },
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
                    onClick = onBookSlot,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Book Slot Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Banner Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Banner Image", color = PrimaryPurple)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = center.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = center.address,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val avgRating = if (feedbacks.isNotEmpty()) feedbacks.map { it.rating }.average().toFloat() else 4.8f
                val reviewsCount = feedbacks.size
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = WarningOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (reviewsCount > 0) String.format("%.1f (%d Reviews)", avgRating, reviewsCount) else "No reviews yet",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location and Hours Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(
                        icon = Icons.Default.LocationOn,
                        title = "1.2 km",
                        subtitle = "Downtown Ave",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = Icons.Default.AccessTime,
                        title = "Open",
                        subtitle = "09:00 AM - 08:00 PM",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Location Map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = {
                        val address = if (center.address.isEmpty()) center.name else center.address
                        val gmmIntentUri = android.net.Uri.parse("google.navigation:q=" + android.net.Uri.encode(address))
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(address))
                            )
                            context.startActivity(webIntent)
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Navigate", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigate", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }


                val services by remember(centerId) {
                    viewModel.getServicesForCenter(centerId)
                }.collectAsState()

                Text("Available Services", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                services.forEach { service ->
                    ServiceItemRow(service.name, service.duration)
                }
                if (services.isEmpty()) {
                    Text("No explicit services available. General consultation applies.", color = TextSecondary, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text("Customer Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                feedbacks.take(5).forEach { fb ->
                    ReviewRow(
                        userName = fb.userName ?: "Anonymous",
                        rating = fb.rating,
                        comments = fb.comments
                    )
                }

                if (feedbacks.isEmpty()) {
                    Text("No reviews yet. Be the first to review!", color = TextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = title, tint = PrimaryPurple)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun ServiceItemRow(name: String, duration: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryPurple)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, fontSize = 16.sp, color = TextPrimary)
        }
        Text(duration, fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
fun ReviewRow(userName: String, rating: Float, comments: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(String.format("%.1f", rating), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(comments, fontSize = 14.sp, color = TextSecondary)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceCenterDetailsPreview() {
    SmartQTheme {
        // ServiceCenterDetailsScreen()
    }
}
