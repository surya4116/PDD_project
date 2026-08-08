package com.simats.myapplication.ui.screens.provider

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAnalyticsScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {}
) {
    val allBookings by viewModel.myBookingsWithDetails.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val totalBookingsCount = allBookings.size
    val completedCount = allBookings.count { it.booking.status == "Completed" }
    val cancelledCount = allBookings.count { it.booking.status == "Cancelled" || it.booking.status == "Failed" }
    val waitingCount = allBookings.count { it.booking.status == "Waiting" || it.booking.status == "In Premise" || it.booking.status == "Called" }

    // Dynamic estimated revenue: Completed appointments multiplied by average service cost ($25)
    val estimatedRevenue = completedCount * 25

    // Chart bar height growth animation on load
    var animateBars by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateBars = true
    }

    val maxCount = maxOf(completedCount, cancelledCount, waitingCount, 1).toFloat()

    val topCategories = remember(allBookings, categories) {
        if (allBookings.isEmpty()) {
            emptyList()
        } else {
            allBookings
                .groupBy { it.center.categoryId }
                .mapNotNull { entry ->
                    val cat = categories.find { it.id == entry.key }
                    if (cat != null) {
                        val count = entry.value.size
                        val percentage = (count.toFloat() / allBookings.size * 100).toInt()
                        Triple(cat.name, count, "$percentage% of total")
                    } else {
                        null
                    }
                }
                .sortedByDescending { it.second }
                .map { it.first to it.third }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Service Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Download Report */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = PrimaryPurple)
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("My Business Earnings & Performance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProviderAnalyticsCard("Total Customer Jobs", "$totalBookingsCount", Modifier.weight(1f))
                ProviderAnalyticsCard("Earnings Est.", "$$estimatedRevenue", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Job Completion Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Animated Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val completedHeightFraction by animateFloatAsState(
                        targetValue = if (animateBars) (completedCount.toFloat() / maxCount) else 0f,
                        animationSpec = tween(1000)
                    )
                    ProviderChartBar(
                        label = "Completed",
                        count = completedCount,
                        heightFraction = completedHeightFraction,
                        color = SuccessGreen
                    )

                    val waitingHeightFraction by animateFloatAsState(
                        targetValue = if (animateBars) (waitingCount.toFloat() / maxCount) else 0f,
                        animationSpec = tween(1000)
                    )
                    ProviderChartBar(
                        label = "Waiting",
                        count = waitingCount,
                        heightFraction = waitingHeightFraction,
                        color = WarningOrange
                    )

                    val cancelledHeightFraction by animateFloatAsState(
                        targetValue = if (animateBars) (cancelledCount.toFloat() / maxCount) else 0f,
                        animationSpec = tween(1000)
                    )
                    ProviderChartBar(
                        label = "Cancelled",
                        count = cancelledCount,
                        heightFraction = cancelledHeightFraction,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Services Distribution", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            if (topCategories.isEmpty()) {
                ProviderTopServiceItem("General Consultation", "0% of total")
                ProviderTopServiceItem("Detailed Service", "0% of total")
            } else {
                topCategories.forEach { (catName, percentStr) ->
                    ProviderTopServiceItem(catName, percentStr)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProviderChartBar(
    label: String,
    count: Int,
    heightFraction: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight().width(75.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(heightFraction.coerceAtLeast(0.06f))
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}

@Composable
fun ProviderAnalyticsCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
        }
    }
}

@Composable
fun ProviderTopServiceItem(name: String, percentage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontWeight = FontWeight.Medium, color = TextPrimary)
        Text(percentage, color = PrimaryPurple, fontWeight = FontWeight.Bold)
    }
}
