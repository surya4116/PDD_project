package com.simats.myapplication.repository


import com.simats.myapplication.data.local.entity.UserEntity
import com.simats.myapplication.data.local.entity.ProviderEntity
import com.simats.myapplication.utils.SessionManager
import com.simats.myapplication.utils.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository(
    private val sessionManager: SessionManager
) {
    /**
     * Authenticates user against XAMPP PHP/MySQL database (smartqueue_db).
     * Supports phone, email, and cross-role detection for seamless login.
     */
    suspend fun login(phone: String, passwordHash: String, expectedRole: String): Result<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanInput = phone.trim()
                val cleanPassword = passwordHash.trim()
                val jsonParams = JSONObject().apply {
                    put("phone", cleanInput)
                    put("email", cleanInput)
                    put("identifier", cleanInput)
                    put("password", cleanPassword)
                }

                if (expectedRole == "Provider" || expectedRole == "Admin") {
                    try {
                        val responseStr = NetworkHelper.post("auth/provider_login.php", jsonParams)
                        val responseJson = JSONObject(responseStr)
                        if (responseJson.optBoolean("success", false)) {
                            val providerJson = if (responseJson.has("user")) responseJson.getJSONObject("user") else responseJson.optJSONObject("admin") ?: JSONObject()
                            val provider = ProviderEntity(
                                id = providerJson.optInt("id", 1),
                                shopName = providerJson.optString("shopName", ""),
                                name = providerJson.optString("name", "Provider"),
                                phone = providerJson.optString("phone", cleanInput),
                                email = providerJson.optString("email", ""),
                                passwordHash = cleanPassword,
                                categoryId = providerJson.optInt("categoryId", 1),
                                location = providerJson.optString("location", "")
                            )
                            sessionManager.saveUserSession(provider.id, "Provider")
                            android.util.Log.d("AuthRepository", "XAMPP Provider Login Success: ${provider.id}")
                            return@withContext Result.success(provider)
                        } else {
                            return@withContext Result.failure(Exception(responseJson.optString("message", "Invalid credentials.")))
                        }
                    } catch (netEx: Exception) {
                        android.util.Log.e("AuthRepository", "Provider network login error", netEx)
                        return@withContext Result.failure(Exception("Network error: Could not connect to server."))
                    }
                } else {
                    // Customer login
                    try {
                        val responseStr = NetworkHelper.post("auth/login.php", jsonParams)
                        val responseJson = JSONObject(responseStr)
                        if (responseJson.optBoolean("success", false)) {
                            val userJson = responseJson.getJSONObject("user")
                            val user = UserEntity(
                                id = userJson.getInt("id"),
                                name = userJson.getString("name"),
                                phone = userJson.getString("phone"),
                                email = userJson.getString("email"),
                                passwordHash = cleanPassword,
                                isDisabled = userJson.optBoolean("isDisabled", false)
                            )
                            sessionManager.saveUserSession(user.id, "User")
                            android.util.Log.d("AuthRepository", "XAMPP Customer Login Success: ${user.id}")
                            return@withContext Result.success(user)
                        } else {
                            return@withContext Result.failure(Exception(responseJson.optString("message", "Invalid credentials.")))
                        }
                    } catch (netEx: Exception) {
                        android.util.Log.e("AuthRepository", "Customer network login error", netEx)
                        return@withContext Result.failure(Exception("Network error: Could not connect to server."))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Login error", e)
                Result.failure(Exception("Login failed: ${e.message}"))
            }
        }
    }

    suspend fun registerUser(name: String, phone: String, email: String, passwordHash: String): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("name", name)
                    put("phone", phone)
                    put("email", email)
                    put("password", passwordHash)
                }

                try {
                    val responseStr = NetworkHelper.post("auth/register.php", jsonParams)
                    val responseJson = JSONObject(responseStr)
                    if (responseJson.optBoolean("success", false)) {
                        val userId = responseJson.optInt("userId", 0)
                        val savedUser = UserEntity(
                            id = userId,
                            name = name,
                            phone = phone,
                            email = email,
                            passwordHash = passwordHash
                        )
                        sessionManager.saveUserSession(savedUser.id, "User")
                        Result.success(savedUser)
                    } else {
                        Result.failure(Exception(responseJson.optString("message", "Registration failed on server.")))
                    }
                } catch (netEx: Exception) {
                    android.util.Log.e("AuthRepository", "Network registration failed", netEx)
                    Result.failure(Exception("Network error: Could not connect to server."))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Registration error", e)
                Result.failure(Exception(e.message ?: "User registration failed"))
            }
        }
    }

    suspend fun registerProvider(shopName: String, name: String, phone: String, email: String, passwordHash: String, categoryId: Int, location: String): Result<ProviderEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("shopName", shopName)
                    put("name", name)
                    put("phone", phone)
                    put("email", email)
                    put("password", passwordHash)
                    put("categoryId", categoryId)
                    put("location", location)
                }

                try {
                    val responseStr = NetworkHelper.post("auth/register_provider.php", jsonParams)
                    val responseJson = JSONObject(responseStr)
                    if (responseJson.optBoolean("success", false)) {
                        val providerId = responseJson.optInt("providerId", 0)
                        val savedProvider = ProviderEntity(
                            id = providerId,
                            shopName = shopName,
                            name = name,
                            phone = phone,
                            email = email,
                            passwordHash = passwordHash,
                            categoryId = categoryId,
                            location = location
                        )
                        sessionManager.saveUserSession(savedProvider.id, "Provider")
                        Result.success(savedProvider)
                    } else {
                        Result.failure(Exception(responseJson.optString("message", "Registration failed on server.")))
                    }
                } catch (netEx: Exception) {
                    android.util.Log.e("AuthRepository", "Network provider registration failed", netEx)
                    Result.failure(Exception("Network error: Could not connect to server."))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Provider Registration error", e)
                Result.failure(Exception(e.message ?: "Provider registration failed"))
            }
        }
    }

    fun logout() {
        sessionManager.logout()
    }

    fun isLoggedIn() = sessionManager.isLoggedIn()
    fun getCurrentUserId() = sessionManager.getUserId()
    fun getCurrentUserRole() = sessionManager.getUserRole()

    fun saveLastSlotConfig(startTime: String, endTime: String, numSlots: Int, tokensPerSlot: Int, date: String) {
        sessionManager.saveLastSlotConfig(startTime, endTime, numSlots, tokensPerSlot, date)
    }
    fun getLastSlotStartTime() = sessionManager.getLastSlotStartTime()
    fun getLastSlotEndTime() = sessionManager.getLastSlotEndTime()
    fun getLastSlotNumSlots() = sessionManager.getLastSlotNumSlots()
    fun getLastSlotTokensPerSlot() = sessionManager.getLastSlotTokensPerSlot()
    fun getLastSlotDate() = sessionManager.getLastSlotDate()

    suspend fun getCurrentUser(): Any? {
        val id = sessionManager.getUserId()
        val role = sessionManager.getUserRole()
        if (id == -1) return null

        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf("id" to id.toString(), "role" to (role ?: "User"))
                val responseStr = NetworkHelper.get("auth/get_user.php", params)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    if (role == "Provider" || role == "Admin") {
                        val providerJson = if (responseJson.has("user")) responseJson.getJSONObject("user") else responseJson.optJSONObject("admin") ?: JSONObject()
                        return@withContext ProviderEntity(
                            id = providerJson.optInt("id", id),
                            shopName = providerJson.optString("shopName", ""),
                            name = providerJson.optString("name", "Provider"),
                            phone = providerJson.optString("phone", ""),
                            email = providerJson.optString("email", ""),
                            passwordHash = "",
                            categoryId = providerJson.optInt("categoryId", 0),
                            location = providerJson.optString("location", "")
                        )
                    } else {
                        val userJson = responseJson.getJSONObject("user")
                        return@withContext UserEntity(
                            id = userJson.getInt("id"),
                            name = userJson.getString("name"),
                            phone = userJson.getString("phone"),
                            email = userJson.getString("email"),
                            passwordHash = "",
                            isDisabled = userJson.optBoolean("isDisabled", false)
                        )
                    }
                }
                null
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Failed to fetch user from network", e)
                null
            }
        }
    }

    suspend fun changePassword(id: Int, userType: String, oldPass: String, newPass: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", id)
                    put("userType", userType)
                    put("oldPassword", oldPass)
                    put("newPassword", newPass)
                }
                val responseStr = NetworkHelper.post("auth/change_password.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optString("message", "Password updated successfully"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to change password")))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Change password error", e)
                Result.failure(Exception(e.message ?: "Failed to change password"))
            }
        }
    }

    suspend fun sendOtp(identifier: String): Result<Pair<String, String?>> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("identifier", identifier)
                }
                val responseStr = NetworkHelper.post("auth/send_otp.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val maskedEmail = responseJson.optString("maskedEmail", "")
                    val debugOtp = if (responseJson.has("debugOtp")) responseJson.getString("debugOtp") else null
                    Result.success(Pair(maskedEmail, debugOtp))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to send OTP")))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Send OTP error", e)
                Result.failure(Exception(e.message ?: "Failed to send OTP"))
            }
        }
    }

    suspend fun verifyOtp(identifier: String, otp: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("identifier", identifier)
                    put("otp", otp)
                }
                val responseStr = NetworkHelper.post("auth/verify_otp.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val resetToken = responseJson.getString("resetToken")
                    Result.success(resetToken)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "OTP verification failed")))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Verify OTP error", e)
                Result.failure(Exception(e.message ?: "OTP verification failed"))
            }
        }
    }

    suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("resetToken", resetToken)
                    put("newPassword", newPassword)
                }
                val responseStr = NetworkHelper.post("auth/reset_password.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Password reset failed")))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Reset password error", e)
                Result.failure(Exception(e.message ?: "Password reset failed"))
            }
        }
    }
}
