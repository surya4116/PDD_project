package com.simats.myapplication.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.utils.BackendConfig
import com.simats.myapplication.utils.NetworkHelper
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDiagnosticsScreen(onBack: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var backendUrl by remember { mutableStateOf(BackendConfig.BASE_URL) }
    var pingResult by remember { mutableStateOf("Not Tested") }
    var apiStatus by remember { mutableStateOf("Offline") }
    var dbStatus by remember { mutableStateOf("Not Checked") }

    fun runDiagnostics() {
        coroutineScope.launch {
            isLoading = true
            try {
                val responseStr = NetworkHelper.get("diagnostics.php")
                val json = JSONObject(responseStr)
                if (json.optBoolean("success", false) || json.optString("apiStatus") == "online") {
                    pingResult = "${json.optInt("latencyMs", 0)} ms"
                    apiStatus = "Online"
                    dbStatus = json.optString("dbStatus", "connected")
                } else {
                    pingResult = "Failed"
                    apiStatus = "Online"
                    dbStatus = json.optString("dbStatus", "Database Error")
                }
            } catch (e: Exception) {
                pingResult = "Failed (Timeout/Refused)"
                apiStatus = "Offline"
                dbStatus = "Database Connection Failed"
            } finally {
                isLoading = false
            }
        }
    }

    // Run automatically on first launch
    LaunchedEffect(Unit) {
        runDiagnostics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "System Status Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    DiagnosticRow(
                        label = "Backend URL",
                        value = backendUrl,
                        icon = Icons.Default.Info,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DiagnosticRow(
                        label = "Ping Result",
                        value = pingResult,
                        icon = Icons.Default.Speed,
                        color = if (pingResult.contains("ms")) SuccessGreen else Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DiagnosticRow(
                        label = "API Status",
                        value = apiStatus,
                        icon = Icons.Default.Dns,
                        color = if (apiStatus == "Online") SuccessGreen else Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DiagnosticRow(
                        label = "Database Status",
                        value = dbStatus,
                        icon = Icons.Default.Storage,
                        color = if (dbStatus == "connected") SuccessGreen else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { runDiagnostics() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryPurple),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Diagnostics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DiagnosticRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryPurple.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
