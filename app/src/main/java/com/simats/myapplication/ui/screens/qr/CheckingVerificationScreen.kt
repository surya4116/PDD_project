package com.simats.myapplication.ui.screens.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.UserViewModel

@Composable
fun CheckingVerificationScreen(
    viewModel: UserViewModel,
    onVerificationComplete: (Boolean) -> Unit = {}
) {
    val bookings by viewModel.userBookings.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(2000)
        val firstWaiting = bookings.firstOrNull { it.status == "Waiting" }
        if (firstWaiting != null) {
            viewModel.checkIn(firstWaiting.id)
            onVerificationComplete(true)
        } else {
            onVerificationComplete(false)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = PrimaryPurple,
            strokeWidth = 6.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Verifying your check-in...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please wait a moment while we confirm your details.",
            fontSize = 16.sp,
            color = TextSecondary
        )
    }
}

// Preview removed to support view model binding
