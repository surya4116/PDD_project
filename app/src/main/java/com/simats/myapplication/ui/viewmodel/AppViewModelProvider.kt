package com.simats.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.simats.myapplication.SmartQApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(smartQApplication().authRepository)
        }
        initializer {
            ProviderViewModel(smartQApplication().providerRepository, smartQApplication().authRepository)
        }
        initializer {
            UserViewModel(smartQApplication().userRepository, smartQApplication().authRepository)
        }
    }
}

fun CreationExtras.smartQApplication(): SmartQApplication =
    (this[APPLICATION_KEY] as SmartQApplication)
