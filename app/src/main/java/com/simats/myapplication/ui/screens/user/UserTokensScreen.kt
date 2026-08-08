package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.UserViewModel
import com.simats.myapplication.data.local.entity.BookingWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTokensScreen(
    viewModel: UserViewModel,
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTokenClick: (Int) -> Unit = {}
) {
    val bookingsWithDetails by viewModel.userBookingsWithDetails.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show review dialog state
    var reviewTargetBooking by remember { mutableStateOf<BookingWithDetails?>(null) }

    LaunchedEffect(uiState) {
        uiState?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiState()
        }
    }

    // Review Dialog
    reviewTargetBooking?.let { booking ->
        ReviewDialog(
            centerName = booking.center.name,
            onDismiss = { reviewTargetBooking = null },
            onSubmit = { rating, comment ->
                viewModel.submitFeedback(booking.center.id, rating, comment)
                reviewTargetBooking = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onHomeClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Tokens") },
                    label = { Text("Tokens") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPurple,
                        selectedTextColor = PrimaryPurple,
                        indicatorColor = PrimaryPurpleLight.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("My Tokens", fontWeight = FontWeight.Bold) },
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

            if (bookingsWithDetails.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = "Empty", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No active tokens", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(bookingsWithDetails.size) { index ->
                        val item = bookingsWithDetails[index]
                        TokenCard(
                            tokenNumber = item.booking.tokenNumber,
                            status = item.booking.status,
                            queuePosition = item.booking.queuePosition,
                            centerName = item.center.name,
                            slotTiming = "${item.slot.startTime} - ${item.slot.endTime}",
                            estimatedWaitTime = item.queueToken?.estimatedWaitTimeMins ?: 0,
                            onClick = { onTokenClick(item.booking.id) },
                            onCancel = { viewModel.cancelBooking(item.booking.id) },
                            onWriteReview = { reviewTargetBooking = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewDialog(
    centerName: String,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryPurple.copy(alpha = 0.12f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Review",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Rate Your Experience",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    centerName,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Star rating row
                Text("Tap to Rate", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val filled = star <= rating
                        Icon(
                            imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $star",
                            tint = if (filled) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = star.toFloat() }
                        )
                    }
                }

                if (rating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val ratingLabel = when (rating.toInt()) {
                        1 -> "Poor 😞"
                        2 -> "Fair 😐"
                        3 -> "Good 🙂"
                        4 -> "Very Good 😊"
                        5 -> "Excellent! 🌟"
                        else -> ""
                    }
                    Text(ratingLabel, fontSize = 14.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Share your experience (optional)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (rating > 0) onSubmit(rating, comment)
                        },
                        modifier = Modifier.weight(2f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rating > 0) PrimaryPurple else Color.LightGray
                        ),
                        enabled = rating > 0
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Submit", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Review", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TokenCard(
    tokenNumber: String,
    status: String,
    queuePosition: Int,
    centerName: String,
    slotTiming: String,
    estimatedWaitTime: Int,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    onWriteReview: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tokenNumber, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                
                val statusColor = when (status) {
                    "Waiting" -> WarningOrange
                    "In Premise" -> SuccessGreen
                    "Completed" -> Color(0xFF2196F3)
                    "Cancelled" -> ErrorRed
                    "Expired" -> ErrorRed
                    else -> TextSecondary
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(centerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Slot: $slotTiming", fontSize = 14.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Queue Position", fontSize = 12.sp, color = TextSecondary)
                    Text("$queuePosition", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("Wait Time", fontSize = 12.sp, color = TextSecondary)
                    Text("$estimatedWaitTime mins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                if (status == "Waiting" || status == "In Premise") {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = ErrorRed)
                    }
                }
            }

            // Write Review button for completed bookings
            if (status == "Completed") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onWriteReview,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.12f)
                    ),
                    elevation = null
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Review",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Write a Review",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
