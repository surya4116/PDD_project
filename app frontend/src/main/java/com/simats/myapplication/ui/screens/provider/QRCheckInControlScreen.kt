package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCheckInControlScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {}
) {
    val bookingsWithDetails by viewModel.myBookingsWithDetails.collectAsState()

    val checkedInBookings = bookingsWithDetails.filter { it.booking.status == "In Premise" || it.booking.status == "Called" || it.booking.status == "Completed" }
    val missedCheckIns = bookingsWithDetails.filter { it.booking.status == "Expired" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Check-In Control", fontWeight = FontWeight.Bold) },
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

            // Summary Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Checked In", tint = SuccessGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${checkedInBookings.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Verified Customer Logs", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Missed", tint = ErrorRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${missedCheckIns.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Missed Appointments", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Verified Customer Check-In Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(checkedInBookings.size) { index ->
                    val item = checkedInBookings[index]
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.booking.bookingTime))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Verified", tint = SuccessGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.booking.tokenNumber, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(item.center.name, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Text(timeStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                }

                if (checkedInBookings.isEmpty()) {
                    item {
                        Text("No verified customer check-in logs found.", color = TextSecondary)
                    }
                }
            }
        }
    }
}
