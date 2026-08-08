package com.simats.myapplication.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class NetworkException(val responseCode: Int, message: String, cause: Throwable? = null) : Exception(message, cause)

object NetworkHelper {
    
    // Set to false to connect directly to XAMPP MySQL backend database
    const val IS_MOCK_MODE = false
    
    private val mockBookings = mutableListOf<JSONObject>()
    
    init {
        // Seed an initial mock booking to display on the tokens tab
        val bookingId = 100
        val bookingObj = JSONObject().apply {
            put("id", bookingId)
            put("userId", 42)
            put("slotId", 1)
            put("centerId", 6)
            put("tokenNumber", "HSP001")
            put("queuePosition", 1)
            put("status", "Confirmed")
            put("bookingTime", System.currentTimeMillis() - 600000)
        }
        
        val slotObj = JSONObject().apply {
            put("id", 1)
            put("serviceId", 1)
            put("centerId", 6)
            put("date", "2026-06-03")
            put("startTime", "09:00 AM")
            put("endTime", "09:30 AM")
            put("maxTokens", 15)
            put("currentTokens", 1)
            put("status", "Running")
            put("delayMins", 0)
        }
        
        val centerObj = JSONObject().apply {
            put("id", 6)
            put("name", "oopd")
            put("categoryId", 1)
            put("address", "poonamalle")
            put("adminId", 6)
            put("isActive", true)
        }
        
        val tokenObj = JSONObject().apply {
            put("id", bookingId + 1000)
            put("bookingId", bookingId)
            put("tokenNumber", "HSP001")
            put("estimatedWaitTimeMins", 10)
            put("issuedAt", System.currentTimeMillis() - 600000)
        }
        
        val compositeObj = JSONObject().apply {
            put("booking", bookingObj)
            put("slot", slotObj)
            put("center", centerObj)
            put("queueToken", tokenObj)
        }
        
        mockBookings.add(compositeObj)
    }
    
    /**
     * Checks if the backend API is online. Tests local health directly in mock mode.
     */
    suspend fun checkHealth(): Unit = withContext(Dispatchers.IO) {
        if (IS_MOCK_MODE) {
            return@withContext
        }
        
        val endpoint = "health.php"
        val urlString = BackendConfig.BASE_URL + endpoint
        val url = URL(urlString)
        
        var conn: HttpURLConnection? = null
        try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                if (!json.optBoolean("success", false)) {
                    throw Exception("Backend server status is offline")
                }
            } else {
                throw NetworkException(responseCode, "API Response Error")
            }
        } catch (e: Exception) {
            if (e is NetworkException) throw e
            throw NetworkException(-1, e.message ?: "Network error", e)
        } finally {
            conn?.disconnect()
        }
    }
    
    /**
     * Executes an HTTP POST request. Stubbed in mock mode to return dynamic local state.
     */
    suspend fun post(endpoint: String, jsonParams: JSONObject): String = withContext(Dispatchers.IO) {
        if (IS_MOCK_MODE) {
            android.util.Log.d("NetworkHelper", "MOCK POST Request - Endpoint: $endpoint Params: $jsonParams")
            
            if (endpoint.contains("login.php")) {
                val isAdmin = endpoint.contains("admin_login")
                val response = JSONObject().apply {
                    put("success", true)
                    if (isAdmin) {
                        put("admin", JSONObject().apply {
                            put("id", 6)
                            put("shopName", "oopd")
                            put("name", "surya")
                            put("phone", "7702970147")
                            put("email", "pasumarthisurya0@gmail.com")
                            put("categoryId", 1)
                            put("location", "poonamalle")
                        })
                    } else {
                        put("user", JSONObject().apply {
                            put("id", 42)
                            put("name", "Surya User")
                            put("phone", "9876543210")
                            put("email", "user@smartq.com")
                            put("isDisabled", false)
                        })
                    }
                }
                return@withContext response.toString()
            }
            
            if (endpoint.contains("register")) {
                val isAdmin = endpoint.contains("register_admin")
                val response = JSONObject().apply {
                    put("success", true)
                    if (isAdmin) {
                        put("adminId", 6)
                    } else {
                        put("userId", 42)
                    }
                }
                return@withContext response.toString()
            }
            
            if (endpoint == "bookings/create_booking.php") {
                val userId = jsonParams.getInt("userId")
                val slotId = jsonParams.getInt("slotId")
                val centerId = jsonParams.getInt("centerId")
                val bookingId = mockBookings.size + 101
                
                val bookingObj = JSONObject().apply {
                    put("id", bookingId)
                    put("userId", userId)
                    put("slotId", slotId)
                    put("centerId", centerId)
                    put("tokenNumber", "HSP00${mockBookings.size + 2}")
                    put("queuePosition", mockBookings.size + 1)
                    put("status", "Confirmed")
                    put("bookingTime", System.currentTimeMillis())
                }
                
                val slotObj = JSONObject().apply {
                    put("id", slotId)
                    put("serviceId", 1)
                    put("centerId", centerId)
                    put("date", "2026-06-03")
                    put("startTime", "09:00 AM")
                    put("endTime", "09:30 AM")
                    put("maxTokens", 15)
                    put("currentTokens", 1)
                    put("status", "Running")
                    put("delayMins", 0)
                }
                
                val centerObj = JSONObject().apply {
                    put("id", centerId)
                    put("name", "oopd")
                    put("categoryId", 1)
                    put("address", "poonamalle")
                    put("adminId", 6)
                    put("isActive", true)
                }
                
                val tokenObj = JSONObject().apply {
                    put("id", bookingId + 1000)
                    put("bookingId", bookingId)
                    put("tokenNumber", "HSP00${mockBookings.size + 2}")
                    put("estimatedWaitTimeMins", 15)
                    put("issuedAt", System.currentTimeMillis())
                }
                
                val compositeObj = JSONObject().apply {
                    put("booking", bookingObj)
                    put("slot", slotObj)
                    put("center", centerObj)
                    put("queueToken", tokenObj)
                }
                
                mockBookings.add(compositeObj)
                
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("booking", bookingObj)
                }.toString()
            }
            
            if (endpoint == "bookings/update_booking.php") {
                val action = jsonParams.optString("action", "")
                val bookingId = jsonParams.optInt("bookingId", -1)
                val match = mockBookings.find { it.getJSONObject("booking").getInt("id") == bookingId }
                if (match != null) {
                    val bookingObj = match.getJSONObject("booking")
                    if (action == "cancel") {
                        bookingObj.put("status", "Cancelled")
                    } else if (action == "checkin") {
                        bookingObj.put("status", "CheckedIn")
                    }
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                }.toString()
            }
            
            if (endpoint == "users/update_profile.php") {
                return@withContext JSONObject().apply {
                    put("success", true)
                }.toString()
            }
            
            if (endpoint == "feedback/add_feedback.php") {
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("id", System.currentTimeMillis())
                }.toString()
            }
            
            if (endpoint == "notifications/send_notification.php") {
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("notificationId", System.currentTimeMillis())
                }.toString()
            }

            return@withContext JSONObject().apply {
                put("success", true)
            }.toString()
        }
        
        val urlString = BackendConfig.BASE_URL + endpoint
        val url = URL(urlString)
        
        var conn: HttpURLConnection? = null
        try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            
            conn.outputStream.use { os ->
                os.write(jsonParams.toString().toByteArray(charset("UTF-8")))
            }
            
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = conn.inputStream.bufferedReader().use { it.readText() }
                result
            } else {
                throw NetworkException(responseCode, "API POST error")
            }
        } catch (e: Exception) {
            if (e is NetworkException) throw e
            throw NetworkException(-1, e.message ?: "Network error", e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Executes an HTTP GET request. Stubbed in mock mode to return filtered lists.
     */
    suspend fun get(endpoint: String, queryParams: Map<String, String>? = null): String = withContext(Dispatchers.IO) {
        if (IS_MOCK_MODE) {
            android.util.Log.d("NetworkHelper", "MOCK GET Request - Endpoint: $endpoint Params: $queryParams")
            
            if (endpoint.contains("get_user.php")) {
                val role = queryParams?.get("role") ?: "User"
                val response = JSONObject().apply {
                    put("success", true)
                    if (role == "Admin") {
                        put("admin", JSONObject().apply {
                            put("id", 6)
                            put("shopName", "oopd")
                            put("name", "surya")
                            put("phone", "7702970147")
                            put("email", "pasumarthisurya0@gmail.com")
                            put("categoryId", 1)
                            put("location", "poonamalle")
                        })
                    } else {
                        put("user", JSONObject().apply {
                            put("id", 42)
                            put("name", "Surya User")
                            put("phone", "9876543210")
                            put("email", "user@smartq.com")
                            put("isDisabled", false)
                        })
                    }
                }
                return@withContext response.toString()
            }
            
            if (endpoint.contains("get_categories.php")) {
                val responseArray = JSONArray().apply {
                    put(JSONObject().apply { put("id", 1); put("name", "Hospital"); put("iconName", "LocalHospital"); put("prefix", "HSP") })
                    put(JSONObject().apply { put("id", 2); put("name", "Bank"); put("iconName", "AccountBalance"); put("prefix", "BNK") })
                    put(JSONObject().apply { put("id", 3); put("name", "Salon"); put("iconName", "ContentCut"); put("prefix", "SLN") })
                    put(JSONObject().apply { put("id", 4); put("name", "Clinics"); put("iconName", "MedicalServices"); put("prefix", "CLN") })
                    put(JSONObject().apply { put("id", 5); put("name", "Other Service"); put("iconName", "Category"); put("prefix", "OTH") })
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("categories", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_centers.php")) {
                val responseArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", 6)
                        put("name", "oopd")
                        put("categoryId", 1)
                        put("address", "poonamalle")
                        put("adminId", 6)
                        put("isActive", true)
                    })
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("centers", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_services.php")) {
                val responseArray = JSONArray().apply {
                    put(JSONObject().apply { put("id", 1); put("centerId", 6); put("name", "General Consultation"); put("duration", "15 mins") })
                    put(JSONObject().apply { put("id", 2); put("centerId", 6); put("name", "Pediatrics Special"); put("duration", "20 mins") })
                    put(JSONObject().apply { put("id", 3); put("centerId", 6); put("name", "Dental Checkup"); put("duration", "30 mins") })
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("services", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_slots.php")) {
                val responseArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", 1)
                        put("serviceId", 1)
                        put("centerId", 6)
                        put("date", "2026-06-03")
                        put("startTime", "09:00 AM")
                        put("endTime", "09:30 AM")
                        put("maxTokens", 15)
                        put("currentTokens", 1)
                        put("status", "Running")
                        put("delayMins", 0)
                    })
                    put(JSONObject().apply {
                        put("id", 2)
                        put("serviceId", 1)
                        put("centerId", 6)
                        put("date", "2026-06-03")
                        put("startTime", "11:30 AM")
                        put("endTime", "12:00 PM")
                        put("maxTokens", 15)
                        put("currentTokens", 0)
                        put("status", "Upcoming")
                        put("delayMins", 0)
                    })
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("slots", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_bookings.php")) {
                val bookingIdParam = queryParams?.get("bookingId")
                val responseArray = JSONArray()
                if (bookingIdParam != null) {
                    val targetId = bookingIdParam.toIntOrNull()
                    mockBookings.find { it.getJSONObject("booking").getInt("id") == targetId }?.let {
                        responseArray.put(it)
                    }
                } else {
                    mockBookings.forEach { responseArray.put(it) }
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("bookings", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_notifications.php")) {
                val responseArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", 1)
                        put("userId", 42)
                        put("title", "Welcome to SmartQueue Pro")
                        put("message", "Manage your slot and track live token status offline!")
                        put("timestamp", System.currentTimeMillis())
                        put("isRead", false)
                    })
                }
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("notifications", responseArray)
                }.toString()
            }
            
            if (endpoint.contains("get_feedback.php")) {
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("feedbacks", JSONArray())
                }.toString()
            }
            
            if (endpoint.contains("get_dashboard_stats.php")) {
                return@withContext JSONObject().apply {
                    put("success", true)
                    put("totalBookings", mockBookings.size)
                    put("completedBookings", 0)
                    put("cancelledBookings", mockBookings.count { it.getJSONObject("booking").getString("status") == "Cancelled" })
                    put("activeQueueLength", mockBookings.count { it.getJSONObject("booking").getString("status") == "Confirmed" })
                }.toString()
            }

            return@withContext JSONObject().apply {
                put("success", true)
            }.toString()
        }
        
        var urlString = BackendConfig.BASE_URL + endpoint
        if (queryParams != null && queryParams.isNotEmpty()) {
            val queryBuilder = StringBuilder("?")
            queryParams.forEach { (key, value) ->
                queryBuilder.append(java.net.URLEncoder.encode(key, "UTF-8"))
                    .append("=")
                    .append(java.net.URLEncoder.encode(value, "UTF-8"))
                    .append("&")
            }
            urlString += queryBuilder.toString().dropLast(1)
        }
        
        val url = URL(urlString)
        var conn: HttpURLConnection? = null
        try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = conn.inputStream.bufferedReader().use { it.readText() }
                result
            } else {
                throw NetworkException(responseCode, "API GET error")
            }
        } catch (e: Exception) {
            if (e is NetworkException) throw e
            throw NetworkException(-1, e.message ?: "Network error", e)
        } finally {
            conn?.disconnect()
        }
    }
}

fun org.json.JSONObject.optLongSafe(key: String, defaultValue: Long = 0L): Long {
    val value = opt(key) ?: return defaultValue
    if (value is Number) {
        return value.toLong()
    }
    if (value is String) {
        return value.toLongOrNull() ?: value.toDoubleOrNull()?.toLong() ?: defaultValue
    }
    return defaultValue
}
