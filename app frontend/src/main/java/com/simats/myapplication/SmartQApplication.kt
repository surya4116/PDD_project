package com.simats.myapplication

import android.app.Application

import com.simats.myapplication.data.local.entity.*
import com.simats.myapplication.repository.ProviderRepository
import com.simats.myapplication.repository.AuthRepository
import com.simats.myapplication.repository.UserRepository
import com.simats.myapplication.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmartQApplication : Application() {
    lateinit var sessionManager: SessionManager
    lateinit var authRepository: AuthRepository
    lateinit var providerRepository: ProviderRepository
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        
        authRepository = AuthRepository(sessionManager)
        providerRepository = ProviderRepository()
        userRepository = UserRepository()


    }
}
