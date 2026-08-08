package com.simats.myapplication.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Choose your role",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select how you want to use SmartQueue Pro",
            fontSize = 16.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        RoleCard(
            title = "Customer",
            description = "Book services and track queue status",
            icon = Icons.Default.Person,
            isSelected = selectedRole == "User" || selectedRole == "Customer",
            onClick = { selectedRole = "User" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleCard(
            title = "Service Provider",
            description = "Deliver services and manage appointments",
            icon = Icons.Default.Storefront,
            isSelected = selectedRole == "Provider" || selectedRole == "Admin",
            onClick = { selectedRole = "Provider" }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { if (selectedRole.isNotEmpty()) onRoleSelected(selectedRole) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                disabledContentColor = Color.White
            ),
            enabled = selectedRole.isNotEmpty()
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) PrimaryPurple else Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryPurple.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectionPreview() {
    SmartQTheme {
        RoleSelectionScreen()
    }
}
