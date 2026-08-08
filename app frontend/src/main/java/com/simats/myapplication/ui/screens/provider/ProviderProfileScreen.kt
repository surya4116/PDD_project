package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.data.local.entity.ProviderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    viewModel: ProviderViewModel? = null,
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onManageServices: () -> Unit = {},
    onShopInfo: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentProvider by if (viewModel != null) {
        viewModel.currentProvider.collectAsState()
    } else {
        remember { mutableStateOf<ProviderEntity?>(null) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Service Provider Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryPurple)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Storefront, contentDescription = "Provider Profile", tint = PrimaryPurple, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val providerName = currentProvider?.name ?: "Service Provider"
            val shopName = currentProvider?.shopName ?: ""
            val providerEmail = currentProvider?.email ?: ""
            val providerPhone = currentProvider?.phone ?: ""

            Text(providerName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (shopName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(shopName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryPurple)
            }
            if (providerPhone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(providerPhone, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
            if (providerEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(providerEmail, fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Provider Account Settings
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProviderProfileMenuItem(Icons.Default.Store, "Business & Location Info", onClick = onShopInfo)
                    ProviderProfileMenuItem(Icons.Default.MedicalServices, "Manage Offered Services", onClick = onManageServices)
                    ProviderProfileMenuItem(Icons.Default.Lock, "Change Password", onClick = onSettingsClick)
                    ProviderProfileMenuItem(Icons.Default.Notifications, "Notification Preferences", onClick = onSettingsClick)
                    ProviderProfileMenuItem(Icons.Default.Settings, "Account Configurations", onClick = onSettingsClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                elevation = null
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProviderProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
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
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}
