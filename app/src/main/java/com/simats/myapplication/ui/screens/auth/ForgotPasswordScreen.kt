package com.simats.myapplication.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.simats.myapplication.ui.theme.*
import com.simats.myapplication.ui.viewmodel.AuthViewModel
import com.simats.myapplication.ui.viewmodel.ForgotPasswordState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.forgotPasswordState.collectAsState()
    val context = LocalContext.current

    var identifier by remember { mutableStateOf("") }
    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    val scrollState = rememberScrollState()

    // Handle State Transitions
    LaunchedEffect(state) {
        when (state) {
            is ForgotPasswordState.ResetSuccess -> {
                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetForgotPasswordState()
                onResetSuccess()
            }
            is ForgotPasswordState.Error -> {
                Toast.makeText(context, (state as ForgotPasswordState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetForgotPasswordState()
                        onNavigateBack()
                    }) {
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Illustration/Icon Header
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PrimaryPurple.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                val icon = when (state) {
                    is ForgotPasswordState.OtpSent -> Icons.Default.Email
                    is ForgotPasswordState.OtpVerified -> Icons.Default.Lock
                    else -> Icons.Default.Lock
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.padding(24.dp).fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val title = when (state) {
                is ForgotPasswordState.OtpSent -> "Verify Code"
                is ForgotPasswordState.OtpVerified -> "New Password"
                else -> "Forgot Password"
            }
            
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val subtitle = when (state) {
                is ForgotPasswordState.OtpSent -> "Enter the 4-digit code sent to your email"
                is ForgotPasswordState.OtpVerified -> "Set a new secure password for your account"
                else -> "Don't worry! Enter your registered email to receive a reset code"
            }

            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // STEP 1: IDENTIFIER (EMAIL)
            if (state is ForgotPasswordState.Idle || state is ForgotPasswordState.Loading || state is ForgotPasswordState.Error) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Email Address or Phone Number") },
                    placeholder = { Text("example@gmail.com or 9876543210") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    enabled = state !is ForgotPasswordState.Loading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (identifier.isNotBlank()) {
                            viewModel.sendOtp(identifier.trim())
                        } else {
                            Toast.makeText(context, "Please enter your email or phone number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = state !is ForgotPasswordState.Loading
                ) {
                    if (state is ForgotPasswordState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Code", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // STEP 2: OTP VERIFICATION
            if (state is ForgotPasswordState.OtpSent) {
                LaunchedEffect(Unit) {
                    focusRequester1.requestFocus()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OtpBox(
                        value = otp1,
                        focusRequester = focusRequester1,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp1 = newValue
                                if (newValue.isNotEmpty()) {
                                    focusRequester2.requestFocus()
                                }
                            }
                        }
                    )
                    OtpBox(
                        value = otp2,
                        focusRequester = focusRequester2,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp2 = newValue
                                if (newValue.isNotEmpty()) {
                                    focusRequester3.requestFocus()
                                } else {
                                    focusRequester1.requestFocus()
                                }
                            }
                        }
                    )
                    OtpBox(
                        value = otp3,
                        focusRequester = focusRequester3,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp3 = newValue
                                if (newValue.isNotEmpty()) {
                                    focusRequester4.requestFocus()
                                } else {
                                    focusRequester2.requestFocus()
                                }
                            }
                        }
                    )
                    OtpBox(
                        value = otp4,
                        focusRequester = focusRequester4,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp4 = newValue
                                if (newValue.isEmpty()) {
                                    focusRequester3.requestFocus()
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val code = otp1 + otp2 + otp3 + otp4
                        if (code.length == 4) {
                            viewModel.verifyOtp(identifier, code)
                        } else {
                            Toast.makeText(context, "Please enter the complete 4-digit code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = state !is ForgotPasswordState.Loading
                ) {
                    if (state is ForgotPasswordState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify Code", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Didn't receive the code? Resend",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.sendOtp(identifier) }
                )
            }

            // STEP 3: RESET PASSWORD
            if (state is ForgotPasswordState.OtpVerified) {
                val resetToken = (state as ForgotPasswordState.OtpVerified).resetToken
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(PurpleGradientStart, PurpleGradientEnd)))
                        .clickable(enabled = state !is ForgotPasswordState.Loading) {
                            if (newPassword.length < 6) {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            } else if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.resetPassword(resetToken, newPassword)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state is ForgotPasswordState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Update Password", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = {
                viewModel.resetForgotPasswordState()
                onNavigateBack()
            }) {
                Text("Return to Login", color = PrimaryPurple, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(60.dp)
            .height(60.dp)
            .focusRequester(focusRequester),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = Color.LightGray
        )
    )
}
