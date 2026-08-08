package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

@Composable
fun BookingConfirmationScreen(
    viewModel: UserViewModel,
    bookingId: Int,
    onTrackQueue: (Int) -> Unit = {},
    onViewTokens: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val bookingWithDetailsState by remember(bookingId) {
        viewModel.getBookingWithDetails(bookingId)
    }.collectAsState()

    val userBookingsWithDetails by viewModel.userBookingsWithDetails.collectAsState()

    val details = bookingWithDetailsState ?: userBookingsWithDetails.find { it.booking.id == bookingId } ?: userBookingsWithDetails.firstOrNull()

    if (details == null) {
        Box(modifier = Modifier.fillMaxSize().background(PrimaryPurple), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = Color.White)
                Text("Confirming your booking...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val services by remember(details.center.id) {
        viewModel.getServicesForCenter(details.center.id)
    }.collectAsState()
    val serviceName = services.find { it.id == details.slot.serviceId }?.name ?: "General Service"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryPurple)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Icon
        Surface(
            shape = CircleShape,
            color = SuccessGreen,
            modifier = Modifier
                .size(70.dp)
                .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Booking Confirmed!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your slot has been successfully booked.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Confirmation Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(BackgroundWhite)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                Text(
                    text = "Your Token Number",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryPurpleLight.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = details.booking.tokenNumber,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details
                ConfirmationDetailRow("Service Center", details.center.name)
                ConfirmationDetailRow("Date", details.slot.date)
                ConfirmationDetailRow("Time", "${details.slot.startTime} - ${details.slot.endTime}")
                ConfirmationDetailRow("Service", serviceName)

                Spacer(modifier = Modifier.height(16.dp))

                // Check-in QR Code Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        coil.compose.SubcomposeAsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=SMARTQ_BOOKING_${details.booking.id}",
                            contentDescription = "Booking QR Code",
                            modifier = Modifier.fillMaxSize(),
                            loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(28.dp)) } },
                            error = { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = PrimaryPurple, modifier = Modifier.fillMaxSize(0.7f)) } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Reminder Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningOrange.copy(alpha = 0.1f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = WarningOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Please show your QR code at the reception upon arrival for quick check-in.",
                        fontSize = 11.sp,
                        color = WarningOrange,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Track Queue Button (Primary Filled)
                Button(
                    onClick = { onTrackQueue(bookingId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Track Queue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. View My Token Button (Secondary Outline)
                OutlinedButton(
                    onClick = onViewTokens,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, PrimaryPurple),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
                ) {
                    Text("View My Token", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Back To Home Button (Tertiary Outline)
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color.Gray.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}
