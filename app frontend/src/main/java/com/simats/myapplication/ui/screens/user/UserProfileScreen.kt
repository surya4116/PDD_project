package com.simats.myapplication.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import com.simats.myapplication.ui.viewmodel.UserViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserViewModel,
    onHomeClick: () -> Unit = {},
    onTokensClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsState()
    
    var showChangePasswordDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    var oldPassword by remember { androidx.compose.runtime.mutableStateOf("") }
    var newPassword by remember { androidx.compose.runtime.mutableStateOf("") }
    var confirmPassword by remember { androidx.compose.runtime.mutableStateOf("") }
    var changePasswordError by remember { androidx.compose.runtime.mutableStateOf("") }
    var changePasswordSuccess by remember { androidx.compose.runtime.mutableStateOf("") }

    var emailNotificationsEnabled by remember { androidx.compose.runtime.mutableStateOf(true) }
    var smsNotificationsEnabled by remember { androidx.compose.runtime.mutableStateOf(true) }
    var pushNotificationsEnabled by remember { androidx.compose.runtime.mutableStateOf(true) }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false 
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                changePasswordError = ""
                changePasswordSuccess = ""
            },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (changePasswordError.isNotEmpty()) {
                        Text(changePasswordError, color = ErrorRed, fontSize = 12.sp)
                    }
                    if (changePasswordSuccess.isNotEmpty()) {
                        Text(changePasswordSuccess, color = SuccessGreen, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    changePasswordError = ""
                    changePasswordSuccess = ""
                    if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        changePasswordError = "All fields are required"
                        return@TextButton
                    }
                    if (newPassword != confirmPassword) {
                        changePasswordError = "New passwords do not match"
                        return@TextButton
                    }
                    viewModel.changePassword(oldPassword, newPassword) { success, message ->
                        if (success) {
                            changePasswordSuccess = message
                            oldPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } else {
                            changePasswordError = message
                        }
                    }
                }) {
                    Text("Update", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showChangePasswordDialog = false 
                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    changePasswordError = ""
                    changePasswordSuccess = ""
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Email Notifications", fontSize = 16.sp, color = TextPrimary)
                        Switch(
                            checked = emailNotificationsEnabled,
                            onCheckedChange = { emailNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight.copy(alpha = 0.5f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SMS Notifications", fontSize = 16.sp, color = TextPrimary)
                        Switch(
                            checked = smsNotificationsEnabled,
                            onCheckedChange = { smsNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight.copy(alpha = 0.5f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Push Notifications", fontSize = 16.sp, color = TextPrimary)
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { pushNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight.copy(alpha = 0.5f))
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showNotificationSettingsDialog = false 
                }) {
                    Text("Save", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
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
                    selected = false,
                    onClick = onTokensClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPurple,
                        selectedTextColor = PrimaryPurple,
                        indicatorColor = PrimaryPurpleLight.copy(alpha = 0.2f)
                    )
                )
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

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = TextSecondary, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(user?.name ?: "John Doe", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(user?.phone ?: "+1 234 567 8900", fontSize = 14.sp, color = TextSecondary)
            Text(user?.email ?: "user@smith.com", fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    UserProfileMenuItem(Icons.AutoMirrored.Filled.EventNote, "My Bookings", onClick = onBookingsClick)
                    UserProfileMenuItem(Icons.Default.LocationOn, "Saved Locations", onClick = {})
                    UserProfileMenuItem(Icons.Default.Lock, "Change Password", onClick = { showChangePasswordDialog = true })
                    UserProfileMenuItem(Icons.Default.Notifications, "Notification Settings", onClick = { showNotificationSettingsDialog = true })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                elevation = null
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UserProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryPurpleLight.copy(alpha = 0.1f)
        ) {
            Icon(icon, contentDescription = title, tint = PrimaryPurple, modifier = Modifier.padding(8.dp).size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}
