package com.simats.myapplication.ui.screens.provider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.data.local.entity.ServiceCenterEntity
import com.simats.myapplication.ui.components.MapWebView
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCentersManagementScreen(
    viewModel: ProviderViewModel,
    onBack: () -> Unit = {},
    onAddCenter: () -> Unit = {},
    onCenterClick: (Int) -> Unit = {}
) {
    val centers by viewModel.serviceCenters.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var centerToDelete by remember { mutableStateOf<ServiceCenterEntity?>(null) }
    var expandedMapCenterId by remember { mutableStateOf<Int?>(null) }

    val filteredCenters = remember(centers, searchQuery) {
        if (searchQuery.isBlank()) centers
        else centers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true) }
    }

    // Delete Confirmation Dialog
    if (centerToDelete != null) {
        AlertDialog(
            onDismissRequest = { centerToDelete = null },
            title = { Text("Delete Service Center", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${centerToDelete?.name}'? All offered services, slots, and active queues under this center will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        centerToDelete?.let { viewModel.deleteServiceCenter(it) }
                        centerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete Shop", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { centerToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Service Centers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCenter,
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Center")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your centers or address...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PrimaryPurple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredCenters.size) { index ->
                    val center = filteredCenters[index]
                    val isMapExpanded = expandedMapCenterId == center.id

                    ProviderServiceCenterCard(
                        center = center,
                        isMapExpanded = isMapExpanded,
                        onClick = { onCenterClick(center.id) },
                        onToggleMap = {
                            expandedMapCenterId = if (isMapExpanded) null else center.id
                        },
                        onDelete = {
                            centerToDelete = center
                        }
                    )
                }

                if (filteredCenters.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No service centers matching '$searchQuery'" else "No service centers found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text("Click + below to register your service location", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProviderServiceCenterCard(
    center: ServiceCenterEntity,
    isMapExpanded: Boolean,
    onClick: () -> Unit = {},
    onToggleMap: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurpleLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, contentDescription = "Store", tint = PrimaryPurple, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(center.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(center.address, fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (center.isActive) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (center.isActive) "Active" else "Inactive",
                            color = if (center.isActive) SuccessGreen else ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Map View Toggle Button
                    IconButton(onClick = onToggleMap) {
                        Icon(
                            imageVector = if (isMapExpanded) Icons.Default.Map else Icons.Default.Place,
                            contentDescription = "Map Location",
                            tint = if (isMapExpanded) PrimaryPurple else TextSecondary
                        )
                    }

                    // Delete Shop Button
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Shop", tint = ErrorRed)
                    }
                }
            }

            // Expanded Embedded Google Map View
            AnimatedVisibility(
                visible = isMapExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Service Location Map", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        Text(center.address, fontSize = 11.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        MapWebView(locationQuery = center.address)
                    }
                }
            }
        }
    }
}
