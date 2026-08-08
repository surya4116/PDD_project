package com.simats.myapplication.repository


import com.simats.myapplication.data.local.entity.BookingEntity
import com.simats.myapplication.data.local.entity.CategoryEntity
import com.simats.myapplication.data.local.entity.ServiceCenterEntity
import com.simats.myapplication.data.local.entity.ServiceEntity
import com.simats.myapplication.data.local.entity.SlotEntity
import com.simats.myapplication.data.local.entity.QueueTokenEntity
import com.simats.myapplication.data.local.entity.BookingWithDetails
import com.simats.myapplication.data.local.entity.FeedbackEntity
import com.simats.myapplication.data.local.entity.NotificationEntity
import com.simats.myapplication.data.local.entity.UserEntity
import com.simats.myapplication.utils.NetworkHelper
import com.simats.myapplication.utils.optLongSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class UserRepository {
    private fun <T> pollFlow(intervalMs: Long = 4000L, block: suspend () -> T): Flow<T> = flow {
        while (true) {
            emit(block())
            delay(intervalMs)
        }
    }

    private fun parseBookingWithDetails(obj: JSONObject): BookingWithDetails {
        val bObj = obj.getJSONObject("booking")
        val sObj = obj.getJSONObject("slot")
        val cObj = obj.getJSONObject("center")
        val qObj = obj.optJSONObject("queueToken")
        
        val booking = BookingEntity(
            id = bObj.getInt("id"),
            userId = bObj.getInt("userId"),
            slotId = bObj.getInt("slotId"),
            centerId = bObj.getInt("centerId"),
            tokenNumber = bObj.getString("tokenNumber"),
            queuePosition = bObj.getInt("queuePosition"),
            status = bObj.getString("status"),
            bookingTime = bObj.optLongSafe("bookingTime")
        )
        
        val slot = SlotEntity(
            id = sObj.getInt("id"),
            serviceId = sObj.optInt("serviceId", 0),
            centerId = sObj.getInt("centerId"),
            date = sObj.getString("date"),
            startTime = sObj.getString("startTime"),
            endTime = sObj.getString("endTime"),
            maxTokens = sObj.getInt("maxTokens"),
            currentTokens = sObj.getInt("currentTokens"),
            status = sObj.optString("status", "Upcoming"),
            delayMins = sObj.optInt("delayMins", 0)
        )
        
        val center = ServiceCenterEntity(
            id = cObj.getInt("id"),
            name = cObj.getString("name"),
            categoryId = cObj.getInt("categoryId"),
            address = cObj.getString("address"),
            providerId = cObj.optInt("providerId", cObj.optInt("adminId", 0)),
            isActive = cObj.optBoolean("isActive", true)
        )
        
        val queueToken = if (qObj != null) {
            QueueTokenEntity(
                id = qObj.getInt("id"),
                bookingId = qObj.getInt("bookingId"),
                tokenNumber = qObj.getString("tokenNumber"),
                estimatedWaitTimeMins = qObj.getInt("estimatedWaitTimeMins"),
                issuedAt = qObj.optLongSafe("issuedAt")
            )
        } else {
            null
        }
        
        return BookingWithDetails(booking, slot, center, queueToken)
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf("id" to userId.toString(), "role" to "User")
                val responseStr = NetworkHelper.get("auth/get_user.php", params)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val userJson = responseJson.getJSONObject("user")
                    UserEntity(
                        id = userJson.getInt("id"),
                        name = userJson.getString("name"),
                        phone = userJson.getString("phone"),
                        email = userJson.getString("email"),
                        passwordHash = "",
                        isDisabled = userJson.optBoolean("isDisabled", false)
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("UserRepository", "Error fetching user profile", e)
                null
            }
        }
    }

    fun getUserByIdFlow(userId: Int): Flow<UserEntity?> = pollFlow {
        getUserById(userId)
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", user.id)
                    put("name", user.name)
                    put("phone", user.phone)
                    put("email", user.email)
                    put("isDisabled", user.isDisabled)
                }
                val responseStr = NetworkHelper.post("users/update_profile.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to update user profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getCategories(): Flow<List<CategoryEntity>> = pollFlow {
        try {
            val responseStr = NetworkHelper.get("get_categories.php")
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<CategoryEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("categories")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CategoryEntity(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            iconName = obj.getString("iconName"),
                            prefix = obj.getString("prefix")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting categories", e)
            emptyList()
        }
    }
    
    fun getAllServiceCenters(): Flow<List<ServiceCenterEntity>> = pollFlow {
        try {
            val responseStr = NetworkHelper.get("service_centers/get_centers.php")
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<ServiceCenterEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("centers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val center = ServiceCenterEntity(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        categoryId = obj.getInt("categoryId"),
                        address = obj.getString("address"),
                        providerId = obj.optInt("providerId", obj.optInt("adminId", 0)),
                        isActive = obj.optBoolean("isActive", true)
                    )
                    list.add(center)
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting all centers", e)
            emptyList()
        }
    }

    fun getServiceCentersByCategory(categoryId: Int): Flow<List<ServiceCenterEntity>> = pollFlow {
        try {
            val params = mapOf("categoryId" to categoryId.toString())
            val responseStr = NetworkHelper.get("service_centers/get_centers.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<ServiceCenterEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("centers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val center = ServiceCenterEntity(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        categoryId = obj.getInt("categoryId"),
                        address = obj.getString("address"),
                        providerId = obj.optInt("providerId", obj.optInt("adminId", 0)),
                        isActive = obj.optBoolean("isActive", true)
                    )
                    list.add(center)
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting centers by category", e)
            emptyList()
        }
    }
    
    fun getServicesForCenter(centerId: Int): Flow<List<ServiceEntity>> = pollFlow {
        try {
            val params = mapOf("centerId" to centerId.toString())
            val responseStr = NetworkHelper.get("services/get_services.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<ServiceEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("services")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ServiceEntity(
                            id = obj.getInt("id"),
                            centerId = obj.getInt("centerId"),
                            name = obj.getString("name"),
                            duration = obj.getString("duration")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting services", e)
            emptyList()
        }
    }
    
    fun getSlotsForCenter(centerId: Int): Flow<List<SlotEntity>> = pollFlow {
        try {
            val targetCenterId = if (centerId > 0) centerId else 1
            val params = mapOf("centerId" to targetCenterId.toString())
            val responseStr = NetworkHelper.get("slots/get_slots.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<SlotEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("slots")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SlotEntity(
                            id = obj.getInt("id"),
                            serviceId = obj.getInt("serviceId"),
                            centerId = obj.getInt("centerId"),
                            date = obj.getString("date"),
                            startTime = obj.getString("startTime"),
                            endTime = obj.getString("endTime"),
                            maxTokens = obj.getInt("maxTokens"),
                            currentTokens = obj.getInt("currentTokens"),
                            status = obj.getString("status"),
                            delayMins = obj.optInt("delayMins", 0)
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting live slots", e)
            emptyList()
        }
    }

    suspend fun generateSlotsForDate(centerId: Int, dateStr: String) {
        // Will be implemented via API if needed
    }
    
    fun searchServiceCenters(query: String): Flow<List<ServiceCenterEntity>> = pollFlow {
        try {
            val params = mapOf("query" to query)
            val responseStr = NetworkHelper.get("service_centers/get_centers.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<ServiceCenterEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("centers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val center = ServiceCenterEntity(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        categoryId = obj.getInt("categoryId"),
                        address = obj.getString("address"),
                        providerId = obj.optInt("providerId", obj.optInt("adminId", 0)),
                        isActive = obj.optBoolean("isActive", true)
                    )
                    list.add(center)
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error searching centers", e)
            emptyList()
        }
    }
    
    suspend fun getAlternativeSlots(serviceId: Int, currentSlotId: Int): List<SlotEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf("serviceId" to serviceId.toString())
                val responseStr = NetworkHelper.get("slots/get_slots.php", params)
                val responseJson = JSONObject(responseStr)
                val list = mutableListOf<SlotEntity>()
                if (responseJson.optBoolean("success", false)) {
                    val array = responseJson.getJSONArray("slots")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val id = obj.getInt("id")
                        if (id != currentSlotId) {
                            list.add(
                                SlotEntity(
                                    id = id,
                                    serviceId = obj.getInt("serviceId"),
                                    centerId = obj.getInt("centerId"),
                                    date = obj.getString("date"),
                                    startTime = obj.getString("startTime"),
                                    endTime = obj.getString("endTime"),
                                    maxTokens = obj.getInt("maxTokens"),
                                    currentTokens = obj.getInt("currentTokens"),
                                    status = obj.optString("status", "Upcoming"),
                                    delayMins = obj.optInt("delayMins", 0)
                                )
                            )
                        }
                    }
                }
                list
            } catch (e: Exception) {
                android.util.Log.e("UserRepository", "Error in getAlternativeSlots", e)
                emptyList()
            }
        }
    }
    
    suspend fun getServiceCenterById(id: Int): ServiceCenterEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val responseStr = NetworkHelper.get("service_centers/get_centers.php")
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val array = responseJson.getJSONArray("centers")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.getInt("id") == id) {
                            return@withContext ServiceCenterEntity(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                categoryId = obj.getInt("categoryId"),
                                address = obj.getString("address"),
                                providerId = obj.optInt("providerId", obj.optInt("adminId", 0)),
                                isActive = obj.optBoolean("isActive", true)
                            )
                        }
                    }
                }
                null
            } catch (e: Exception) {
                android.util.Log.e("UserRepository", "Error in getServiceCenterById", e)
                null
            }
        }
    }

    suspend fun bookSlot(userId: Int, slotId: Int, centerId: Int): Result<BookingEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("userId", userId)
                    put("slotId", slotId)
                    put("centerId", centerId)
                }
                val responseStr = NetworkHelper.post("bookings/create_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                
                if (responseJson.optBoolean("success", false)) {
                    val bObj = responseJson.getJSONObject("booking")
                    val createdBooking = BookingEntity(
                        id = bObj.getInt("id"),
                        userId = bObj.getInt("userId"),
                        slotId = bObj.getInt("slotId"),
                        centerId = bObj.getInt("centerId"),
                        tokenNumber = bObj.getString("tokenNumber"),
                        queuePosition = bObj.getInt("queuePosition"),
                        status = bObj.getString("status"),
                        bookingTime = bObj.optLongSafe("bookingTime")
                    )
                    Result.success(createdBooking)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Booking failed on server")))
                }
            } catch (netEx: Exception) {
                android.util.Log.e("UserRepository", "Network booking failed", netEx)
                Result.failure(Exception("Failed to connect to server. Please try again."))
            }
        }
    }
    
    fun getUserBookings(userId: Int): Flow<List<BookingEntity>> = pollFlow {
        try {
            val params = mapOf("userId" to userId.toString())
            val responseStr = NetworkHelper.get("bookings/get_bookings.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<BookingEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("bookings")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val bObj = obj.getJSONObject("booking")
                    list.add(BookingEntity(
                        id = bObj.getInt("id"),
                        userId = bObj.getInt("userId"),
                        slotId = bObj.getInt("slotId"),
                        centerId = bObj.getInt("centerId"),
                        tokenNumber = bObj.getString("tokenNumber"),
                        queuePosition = bObj.getInt("queuePosition"),
                        status = bObj.getString("status"),
                        bookingTime = bObj.optLongSafe("bookingTime")
                    ))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getUserBookingsWithDetails(userId: Int): Flow<List<BookingWithDetails>> = pollFlow {
        try {
            val params = mapOf("userId" to userId.toString())
            val responseStr = NetworkHelper.get("bookings/get_bookings.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<BookingWithDetails>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("bookings")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(parseBookingWithDetails(obj))
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error fetching live user bookings", e)
            emptyList()
        }
    }
    
    fun getBookingWithDetailsById(bookingId: Int): Flow<BookingWithDetails?> = pollFlow {
        try {
            val params = mapOf("bookingId" to bookingId.toString())
            val responseStr = NetworkHelper.get("bookings/get_bookings.php", params)
            val responseJson = JSONObject(responseStr)
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("bookings")
                if (array.length() > 0) {
                    parseBookingWithDetails(array.getJSONObject(0))
                } else null
            } else null
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error fetching live booking by id", e)
            null
        }
    }
    

    
    suspend fun checkInBooking(bookingId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "checkin")
                    put("bookingId", bookingId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Check-in failed")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun expireMissedBookings(slotId: Int) {
        // No-op or call a php cleanup script if needed. For now, handled dynamically by callNextBooking / markBookingCompleted inside update_booking.php.
    }

    suspend fun rescheduleBooking(bookingId: Int, newSlotId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "reschedule")
                    put("bookingId", bookingId)
                    put("newSlotId", newSlotId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Reschedule failed")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cancelBooking(bookingId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "cancel")
                    put("bookingId", bookingId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Cancel failed")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun insertFeedback(userId: Int, centerId: Int?, rating: Float, comments: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("userId", userId)
                    if (centerId != null) {
                        put("centerId", centerId)
                    }
                    put("rating", rating)
                    put("comments", comments)
                }
                val responseStr = NetworkHelper.post("feedback/add_feedback.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optLongSafe("id"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Feedback insert failed")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = pollFlow {
        try {
            val responseStr = NetworkHelper.get("feedback/get_feedback.php")
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<FeedbackEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("feedbacks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        FeedbackEntity(
                            id = obj.getInt("id"),
                            userId = obj.getInt("userId"),
                            rating = obj.getDouble("rating").toFloat(),
                            comments = obj.getString("comments"),
                            timestamp = obj.optLongSafe("timestamp")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting all feedback", e)
            emptyList()
        }
    }

    fun getNotificationsByUser(userId: Int): Flow<List<NotificationEntity>> = pollFlow {
        try {
            val params = mapOf("userId" to userId.toString())
            val responseStr = NetworkHelper.get("notifications/get_notifications.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<NotificationEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("notifications")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        NotificationEntity(
                            id = obj.getInt("id"),
                            userId = obj.getInt("userId"),
                            title = obj.getString("title"),
                            message = obj.getString("message"),
                            timestamp = obj.optLongSafe("timestamp"),
                            isRead = obj.optBoolean("isRead", false)
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting user notifications", e)
            emptyList()
        }
    }

    suspend fun insertNotification(userId: Int, title: String, message: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("userId", userId)
                    put("title", title)
                    put("message", message)
                }
                val responseStr = NetworkHelper.post("notifications/send_notification.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optLongSafe("notificationId"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to insert notification")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun markNotificationsAsRead(userId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("userId", userId)
                }
                val responseStr = NetworkHelper.post("notifications/mark_read.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to mark notifications read")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getFeedbackForCenter(centerId: Int): Flow<List<FeedbackEntity>> = pollFlow {
        try {
            val params = mapOf("centerId" to centerId.toString())
            val responseStr = NetworkHelper.get("feedback/get_feedback.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<FeedbackEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("feedbacks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val feedback = FeedbackEntity(
                        id = obj.getInt("id"),
                        userId = obj.getInt("userId"),
                        rating = obj.getDouble("rating").toFloat(),
                        comments = obj.getString("comments"),
                        timestamp = obj.getLong("timestamp")
                    ).apply {
                        userName = obj.optString("userName", "Anonymous")
                    }
                    list.add(feedback)
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting feedback for center", e)
            emptyList()
        }
    }
}
