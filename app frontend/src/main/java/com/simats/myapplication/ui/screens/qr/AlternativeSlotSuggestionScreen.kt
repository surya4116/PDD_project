package com.simats.myapplication.ui.screens.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeSlotSuggestionScreen(
    onBack: () -> Unit = {},
    onReschedule: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alternative Slots", fontWeight = FontWeight.Bold) },
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
                    onClick = onReschedule,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Reschedule Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
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

            // Calendar Illustration Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryPurpleLight.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = PrimaryPurple, modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Missed your slot?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Here are some available nearby slots you can book instead.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Suggestions List
            SuggestionCard(time = "Today, 11:30 AM", center = "City Care Hospital", distance = "0 km")
            SuggestionCard(time = "Today, 01:00 PM", center = "City Care Hospital", distance = "0 km")
            SuggestionCard(time = "Today, 12:15 PM", center = "Wellness Clinic", distance = "1.5 km")
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SuggestionCard(time: String, center: String, distance: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                Spacer(modifier = Modifier.height(4.dp))
                Text(center, fontSize = 14.sp, color = TextPrimary)
                Text("Distance: $distance", fontSize = 12.sp, color = TextSecondary)
            }
            
            RadioButton(selected = false, onClick = { /* Select */ }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlternativeSlotSuggestionPreview() {
    SmartQTheme {
        AlternativeSlotSuggestionScreen()
    }
}
