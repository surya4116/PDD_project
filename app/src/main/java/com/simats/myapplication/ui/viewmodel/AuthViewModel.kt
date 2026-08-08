package com.simats.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.myapplication.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class OtpSent(val maskedEmail: String, val debugOtp: String?) : ForgotPasswordState()
    data class OtpVerified(val resetToken: String) : ForgotPasswordState()
    object ResetSuccess : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    fun login(phone: String, passwordHash: String, role: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(phone, passwordHash, role)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun registerUser(name: String, phone: String, email: String, passwordHash: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.registerUser(name, phone, email, passwordHash)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "User registration failed")
            }
        }
    }

    fun registerProvider(shopName: String, name: String, phone: String, email: String, passwordHash: String, categoryId: Int, location: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.registerProvider(shopName, name, phone, email, passwordHash, categoryId, location)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Provider registration failed")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    fun logout() {
        repository.logout()
    }

    // --- Forgot Password ---

    fun sendOtp(identifier: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            val result = repository.sendOtp(identifier)
            if (result.isSuccess) {
                val (maskedEmail, debugOtp) = result.getOrThrow()
                _forgotPasswordState.value = ForgotPasswordState.OtpSent(maskedEmail, debugOtp)
            } else {
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun verifyOtp(identifier: String, otp: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            val result = repository.verifyOtp(identifier, otp)
            if (result.isSuccess) {
                _forgotPasswordState.value = ForgotPasswordState.OtpVerified(result.getOrThrow())
            } else {
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    result.exceptionOrNull()?.message ?: "OTP verification failed"
                )
            }
        }
    }

    fun resetPassword(resetToken: String, newPassword: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            val result = repository.resetPassword(resetToken, newPassword)
            if (result.isSuccess) {
                _forgotPasswordState.value = ForgotPasswordState.ResetSuccess
            } else {
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    result.exceptionOrNull()?.message ?: "Password reset failed"
                )
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}
