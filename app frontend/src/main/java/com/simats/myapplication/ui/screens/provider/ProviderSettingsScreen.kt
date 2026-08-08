package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 1. Password Change State
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    // 2. Notification Preferences State
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var bookingAlertsEnabled by remember { mutableStateOf(true) }
    var queueCancelAlertsEnabled by remember { mutableStateOf(true) }
    var soundVibrationEnabled by remember { mutableStateOf(true) }

    // 3. Service Rules & Configurations State
    var workingHours by remember { mutableStateOf("09:00 AM - 05:00 PM") }
    var tokenPrefix by remember { mutableStateOf("PRV") }
    var maxDailySlots by remember { mutableStateOf("50") }
    var maxTokenLimit by remember { mutableStateOf("15") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Provider Settings", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: SECURITY & CHANGE PASSWORD
            Text("Security & Password", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryPurpleLight.copy(alpha = 0.15f)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Change Account Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Update your security credentials", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Old Password Field
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                Icon(
                                    imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // New Password Field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Please fill in all password fields") }
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("New passwords do not match") }
                                return@Button
                            }
                            if (newPassword.length < 4) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Password must be at least 4 characters") }
                                return@Button
                            }

                            isChangingPassword = true
                            viewModel.changePassword(oldPassword, newPassword) { success, message ->
                                isChangingPassword = false
                                if (success) {
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        },
                        enabled = !isChangingPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        if (isChangingPassword) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating Password...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Password", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: NOTIFICATION PREFERENCES
            Text("Notification Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryPurpleLight.copy(alpha = 0.15f)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("App Push Notifications", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Receive real-time push alerts", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = {
                                pushNotificationsEnabled = it
                                coroutineScope.launch { snackbarHostState.showSnackbar(if (it) "Push Notifications Enabled" else "Push Notifications Disabled") }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryPurple)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryPurpleLight.copy(alpha = 0.15f)
                            ) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("New Customer Booking Alerts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Notify when a customer books a token", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = bookingAlertsEnabled,
                            onCheckedChange = {
                                bookingAlertsEnabled = it
                                coroutineScope.launch { snackbarHostState.showSnackbar(if (it) "Booking Alerts Enabled" else "Booking Alerts Disabled") }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryPurple)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryPurpleLight.copy(alpha = 0.15f)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sound & Vibration Alerts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Play sound on active queue calls", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = soundVibrationEnabled,
                            onCheckedChange = {
                                soundVibrationEnabled = it
                                coroutineScope.launch { snackbarHostState.showSnackbar(if (it) "Sound & Vibration Enabled" else "Sound & Vibration Disabled") }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryPurple)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: WORKING HOURS & BOOKING CONFIGURATIONS
            Text("Service Rules & Working Hours", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = { workingHours = it },
                        label = { Text("Business Operating Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tokenPrefix,
                        onValueChange = { tokenPrefix = it },
                        label = { Text("Token Prefix (e.g. PRV)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxDailySlots,
                        onValueChange = { maxDailySlots = it },
                        label = { Text("Maximum Daily Slots") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxTokenLimit,
                        onValueChange = { maxTokenLimit = it },
                        label = { Text("Max Tokens Per Slot") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Provider configuration rules saved!") }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configurations", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
