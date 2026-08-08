package com.simats.myapplication.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.simats.myapplication.ui.viewmodel.AuthState
import com.simats.myapplication.ui.viewmodel.AuthViewModel
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderLoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit = {},
    onNavigateToForgot: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onLoginSuccess()
            }
            is AuthState.Error -> {
                val error = (authState as AuthState.Error).message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Service Provider Icon
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PrimaryPurple.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "Service Provider",
                tint = PrimaryPurple,
                modifier = Modifier.padding(20.dp).fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Service Provider Portal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage your appointments, services, and earnings",
            fontSize = 15.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Business Phone or Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle visibility")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Forgot Password?",
            color = PrimaryPurple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onNavigateToForgot() }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(PurpleGradientStart, PurpleGradientEnd)))
                .clickable {
                    if (phone.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.login(phone, password, "Provider")
                    } else {
                        Toast.makeText(context, "Please enter phone/email and password", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign In as Service Provider", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Don't have a provider account? ", color = TextSecondary)
            Text(
                text = "Register Account",
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
