package com.simats.myapplication.utils

import android.os.Build

object BackendConfig {
    val BASE_URL: String
        get() {
            val isEmulator = Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk" == Build.PRODUCT

            return if (isEmulator) {
                // Special IP for Android Emulator to connect to host's localhost (XAMPP)
                "http://10.0.2.2/smartqueue/api/"
            } else {
                // IP for physical Android device testing on local Wi-Fi
                "http://10.18.148.163/smartqueue/api/"
            }
        }
}
