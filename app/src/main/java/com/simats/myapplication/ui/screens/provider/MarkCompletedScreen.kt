package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.data.local.entity.BookingEntity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkCompletedScreen(
    viewModel: ProviderViewModel,
    bookingId: Int,
    onBack: () -> Unit = {},
    onCompleted: () -> Unit = {}
) {
    var booking by remember { mutableStateOf<BookingEntity?>(null) }

    LaunchedEffect(bookingId) {
        viewModel.getBookingById(bookingId) { result ->
            booking = result
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState == "Booking Marked Completed") {
            viewModel.clearUiState()
            onCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Completion", fontWeight = FontWeight.Bold) },
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
                    onClick = { viewModel.markCompleted(bookingId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark Job as Completed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Service Completion Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryPurpleLight.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Complete Service Job", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (booking != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryPurpleLight.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.padding(16.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Customer ID: ${booking?.userId}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Status: ${booking?.status}", fontSize = 14.sp, color = TextSecondary)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Token Number", color = TextSecondary)
                            Text(booking?.tokenNumber ?: "", fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Booking ID", color = TextSecondary)
                            Text(booking?.id.toString(), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        }
    }
}
