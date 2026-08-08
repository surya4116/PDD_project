package com.simats.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smartq_session", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, role: String) {
        prefs.edit().apply {
            putInt("USER_ID", userId)
            putString("USER_ROLE", role)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    fun getUserId(): Int = prefs.getInt("USER_ID", -1)

    fun getUserRole(): String? = prefs.getString("USER_ROLE", null)

    fun saveLastSlotConfig(startTime: String, endTime: String, numSlots: Int, tokensPerSlot: Int, date: String) {
        prefs.edit().apply {
            putString("LAST_SLOT_START_TIME", startTime)
            putString("LAST_SLOT_END_TIME", endTime)
            putInt("LAST_SLOT_NUM_SLOTS", numSlots)
            putInt("LAST_SLOT_TOKENS_PER_SLOT", tokensPerSlot)
            putString("LAST_SLOT_DATE", date)
            apply()
        }
    }

    fun getLastSlotStartTime(): String = prefs.getString("LAST_SLOT_START_TIME", "") ?: ""
    fun getLastSlotEndTime(): String = prefs.getString("LAST_SLOT_END_TIME", "") ?: ""
    fun getLastSlotNumSlots(): Int = prefs.getInt("LAST_SLOT_NUM_SLOTS", 8)
    fun getLastSlotTokensPerSlot(): Int = prefs.getInt("LAST_SLOT_TOKENS_PER_SLOT", 1)
    fun getLastSlotDate(): String = prefs.getString("LAST_SLOT_DATE", "") ?: ""
}
