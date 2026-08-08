package com.simats.myapplication.ui.screens.qr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onClose: () -> Unit = {},
    onScanSuccess: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Toggle Flashlight */ }) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = "Flashlight", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Neon Purple Scanner Effect
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Transparent)
                    .clickable { onScanSuccess() } // Simulate scan
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(4.dp, PrimaryPurpleLight) // Neon effect
                ) {}
                
                // Animated Line Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PrimaryPurpleLight)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Align QR code within the frame to scan",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QRScannerPreview() {
    SmartQTheme {
        QRScannerScreen()
    }
}
