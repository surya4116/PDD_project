package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

import com.simats.myapplication.ui.viewmodel.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import com.simats.myapplication.data.local.entity.BookingWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveQueueTrackingScreen(
    viewModel: UserViewModel,
    bookingId: Int,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val bookingWithDetails by remember(bookingId) {
        viewModel.getBookingWithDetails(bookingId)
    }.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Queue", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh Action */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        }
    ) { paddingValues ->
        if (bookingWithDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        
        val details = bookingWithDetails!!
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(details.center.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(details.center.address, fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(40.dp))

            // Main Status Dashboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(PrimaryPurpleLight, PrimaryPurple)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currently Serving
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Position", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${details.booking.queuePosition}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }

                    // Divider line
                    Box(modifier = Modifier.height(80.dp).width(1.dp).background(Color.White.copy(alpha = 0.3f)))

                    // User Token
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Token", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(details.booking.tokenNumber, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Wait Time Card with AI counter allocation & congestion levels
            val congestion = com.simats.myapplication.utils.AIQueuePredictor.detectCongestion(
                details.slot.currentTokens, details.slot.maxTokens
            )
            val counter = com.simats.myapplication.utils.AIQueuePredictor.allocateCounter(
                details.booking.tokenNumber
            )

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimated Wait Time", fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${details.queueToken?.estimatedWaitTimeMins ?: 0} mins", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WarningOrange)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Status", fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(details.booking.status, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = "Counter", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Allocated: $counter", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        
                        // Congestion Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (congestion == "High Congestion") ErrorRed.copy(alpha = 0.1f) 
                                    else if (congestion == "Moderate Congestion") WarningOrange.copy(alpha = 0.1f)
                                    else SuccessGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = congestion,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (congestion == "High Congestion") ErrorRed 
                                        else if (congestion == "Moderate Congestion") WarningOrange
                                        else SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Queue Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress List
            if (details.booking.queuePosition == 1) {
                QueueProgressItem(token = details.booking.tokenNumber, status = "Serving Now", isCurrent = true, isServing = true)
            } else if (details.booking.queuePosition > 1) {
                QueueProgressItem(token = "...", status = "Serving Now", isCurrent = false, isServing = true)
                QueueProgressItem(token = details.booking.tokenNumber, status = "Waiting (${details.booking.queuePosition})", isCurrent = true)
            } else {
                QueueProgressItem(token = details.booking.tokenNumber, status = details.booking.status, isCurrent = true)
            }

            if (details.booking.status == "Completed") {
                Spacer(modifier = Modifier.height(24.dp))
                FeedbackSubmissionCard(
                    onSubmit = { rating, comments ->
                        viewModel.submitFeedback(details.booking.centerId, rating, comments)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (details.booking.status == "Waiting" || details.booking.status == "In Premise") {
                Button(
                    onClick = {
                        viewModel.cancelBooking(bookingId)
                        onCancel()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    elevation = null
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = ErrorRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QueueProgressItem(token: String, status: String, isCurrent: Boolean, isServing: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) PrimaryPurpleLight.copy(alpha = 0.1f) else SurfaceWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isServing) WarningOrange else if (isCurrent) PrimaryPurple else Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                token,
                fontSize = 18.sp,
                fontWeight = if (isCurrent || isServing) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) PrimaryPurple else TextPrimary
            )
        }
        Text(
            status,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isServing) WarningOrange else if (isCurrent) PrimaryPurple else TextSecondary
        )
    }
}

@Composable
fun FeedbackSubmissionCard(
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5f) }
    var comments by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!submitted) {
                Text("Step 12: Rate Your Experience", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your feedback helps us improve our services.", fontSize = 14.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Star Selector Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..5) {
                        val icon = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder
                        Icon(
                            imageVector = icon,
                            contentDescription = "Star $i",
                            tint = WarningOrange,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = i.toFloat() }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    placeholder = { Text("Write your feedback here (e.g. Excellent Service)...") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        onSubmit(rating, comments)
                        submitted = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Submit Feedback", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = "Submitted", tint = SuccessGreen, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Thank You!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Your feedback was successfully submitted.", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}


