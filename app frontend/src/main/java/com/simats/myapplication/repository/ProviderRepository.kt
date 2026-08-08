package com.simats.myapplication.repository


import com.simats.myapplication.data.local.entity.*
import com.simats.myapplication.utils.NetworkHelper
import com.simats.myapplication.utils.optLongSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ProviderRepository {
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
        ).apply {
            userName = bObj.optString("userName", "")
        }
        
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

    // Service management
    suspend fun updateService(service: ServiceEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", service.id)
                    put("name", service.name)
                    put("duration", service.duration)
                }
                val responseStr = NetworkHelper.post("services/update_service.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to update service")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteService(service: ServiceEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", service.id)
                }
                val responseStr = NetworkHelper.post("services/delete_service.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to delete service")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Provider's own bookings
    fun getBookingsForProvider(providerId: Int): Flow<List<BookingWithDetails>> = pollFlow {
        try {
            val params = mapOf("providerId" to providerId.toString())
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
            android.util.Log.e("ProviderRepository", "Error fetching live provider bookings", e)
            emptyList()
        }
    }

    suspend fun updateBooking(booking: BookingEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val action = when (booking.status) {
                    "In Premise" -> "checkin"
                    "Cancelled" -> "cancel"
                    "Failed" -> "reject"
                    "Completed" -> "complete"
                    else -> "update_entity"
                }
                val jsonParams = JSONObject().apply {
                    put("action", action)
                    put("bookingId", booking.id)
                    put("status", booking.status)
                    put("queuePosition", booking.queuePosition)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to update booking")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteBooking(booking: BookingEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "delete")
                    put("bookingId", booking.id)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to delete booking")))
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
            android.util.Log.e("ProviderRepository", "Error getting live categories", e)
            emptyList()
        }
    }
    
    fun getServiceCentersByProvider(providerId: Int): Flow<List<ServiceCenterEntity>> = pollFlow {
        try {
            val params = mapOf("providerId" to providerId.toString())
            val responseStr = NetworkHelper.get("service_centers/get_centers.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<ServiceCenterEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("centers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ServiceCenterEntity(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            categoryId = obj.getInt("categoryId"),
                            address = obj.getString("address"),
                            providerId = obj.optInt("providerId", obj.optInt("adminId", 0)),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("ProviderRepository", "Error syncing provider centers", e)
            emptyList()
        }
    }
    
    suspend fun addServiceCenter(center: ServiceCenterEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("name", center.name)
                    put("categoryId", center.categoryId)
                    put("address", center.address)
                    put("providerId", center.providerId)
                }
                val responseStr = NetworkHelper.post("service_centers/add_center.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optLongSafe("centerId"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to add center")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateServiceCenter(center: ServiceCenterEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", center.id)
                    put("name", center.name)
                    put("address", center.address)
                    put("isActive", center.isActive)
                }
                val responseStr = NetworkHelper.post("service_centers/update_center.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to update center")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteServiceCenter(center: ServiceCenterEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", center.id)
                }
                val responseStr = NetworkHelper.post("service_centers/delete_center.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to delete center")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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
            android.util.Log.e("ProviderRepository", "Error getting services", e)
            emptyList()
        }
    }
    
    suspend fun addService(service: ServiceEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("centerId", service.centerId)
                    put("name", service.name)
                    put("duration", service.duration)
                }
                val responseStr = NetworkHelper.post("services/add_service.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optLongSafe("id"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to add service")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getSlotsForService(serviceId: Int): Flow<List<SlotEntity>> = pollFlow {
        try {
            val params = mapOf("serviceId" to serviceId.toString())
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
                            status = obj.optString("status", "Upcoming"),
                            delayMins = obj.optInt("delayMins", 0)
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("ProviderRepository", "Error getting slots for service", e)
            emptyList()
        }
    }
    
    suspend fun getServiceById(serviceId: Int): ServiceEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf("serviceId" to serviceId.toString())
                val responseStr = NetworkHelper.get("services/get_services.php", params)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val array = responseJson.getJSONArray("services")
                    if (array.length() > 0) {
                        val obj = array.getJSONObject(0)
                        return@withContext ServiceEntity(
                            id = obj.getInt("id"),
                            centerId = obj.getInt("centerId"),
                            name = obj.getString("name"),
                            duration = obj.getString("duration")
                        )
                    }
                }
                null
            } catch (e: Exception) {
                android.util.Log.e("ProviderRepository", "Error getting service by id", e)
                null
            }
        }
    }

    suspend fun addSlot(slot: SlotEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("serviceId", slot.serviceId)
                    put("centerId", slot.centerId)
                    put("date", slot.date)
                    put("startTime", slot.startTime)
                    put("endTime", slot.endTime)
                    put("maxTokens", slot.maxTokens)
                }
                val responseStr = NetworkHelper.post("slots/add_slot.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(responseJson.optLongSafe("id"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to add slot")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSlot(slot: SlotEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", slot.id)
                }
                val responseStr = NetworkHelper.post("slots/delete_slot.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to delete slot")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSlotDelay(slotId: Int, delayMins: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("id", slotId)
                    put("delayMins", delayMins)
                }
                val responseStr = NetworkHelper.post("slots/update_slot_delay.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to update slot delay")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Queue Management
    fun getActiveQueueForSlot(slotId: Int): Flow<List<BookingEntity>> = pollFlow {
        try {
            val params = mapOf("slotId" to slotId.toString())
            val responseStr = NetworkHelper.get("queues/get_queue.php", params)
            val responseJson = JSONObject(responseStr)
            val list = mutableListOf<BookingEntity>()
            if (responseJson.optBoolean("success", false)) {
                val array = responseJson.getJSONArray("queue")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BookingEntity(
                            id = obj.getInt("id"),
                            userId = obj.getInt("userId"),
                            slotId = obj.getInt("slotId"),
                            centerId = obj.getInt("centerId"),
                            tokenNumber = obj.getString("tokenNumber"),
                            queuePosition = obj.getInt("queuePosition"),
                            status = obj.getString("status"),
                            bookingTime = obj.optLongSafe("bookingTime")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("ProviderRepository", "Error getting active queue", e)
            emptyList()
        }
    }

    suspend fun markBookingCompleted(bookingId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "complete")
                    put("bookingId", bookingId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to complete booking")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Provider dashboard stats
    fun getProviderStats(providerId: Int): Flow<Map<String, Int>> = pollFlow {
        try {
            val params = mapOf("providerId" to providerId.toString())
            val responseStr = NetworkHelper.get("dashboard/get_dashboard_stats.php", params)
            val responseJson = JSONObject(responseStr)
            if (responseJson.optBoolean("success", false)) {
                mapOf(
                    "totalBookings" to responseJson.optInt("totalBookings", 0),
                    "completedServices" to responseJson.optInt("completedServices", 0),
                    "activeTokens" to responseJson.optInt("activeTokens", 0),
                    "missedAppointments" to responseJson.optInt("missedAppointments", 0)
                )
            } else mapOf()
        } catch (e: Exception) { mapOf() }
    }

    suspend fun getBookingById(bookingId: Int): BookingEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf("bookingId" to bookingId.toString())
                val responseStr = NetworkHelper.get("bookings/get_bookings.php", params)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    val array = responseJson.getJSONArray("bookings")
                    if (array.length() > 0) {
                        val bObj = array.getJSONObject(0).getJSONObject("booking")
                        return@withContext BookingEntity(
                            id = bObj.getInt("id"),
                            userId = bObj.getInt("userId"),
                            slotId = bObj.getInt("slotId"),
                            centerId = bObj.getInt("centerId"),
                            tokenNumber = bObj.getString("tokenNumber"),
                            queuePosition = bObj.getInt("queuePosition"),
                            status = bObj.getString("status"),
                            bookingTime = bObj.optLongSafe("bookingTime")
                        )
                    }
                }
                null
            } catch (e: Exception) {
                android.util.Log.e("ProviderRepository", "Error getting booking by id", e)
                null
            }
        }
    }

    suspend fun callNextBooking(slotId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "call_next")
                    put("slotId", slotId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to call next booking")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun skipBooking(bookingId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonParams = JSONObject().apply {
                    put("action", "skip")
                    put("bookingId", bookingId)
                }
                val responseStr = NetworkHelper.post("bookings/update_booking.php", jsonParams)
                val responseJson = JSONObject(responseStr)
                if (responseJson.optBoolean("success", false)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed to skip booking")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun sendNotification(userId: Int, title: String, message: String): Result<Long> {
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
                    Result.failure(Exception(responseJson.optString("message", "Failed to send notification")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun autoCompletePastSlotsAndBookings(todayDate: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            // Replaced by XAMPP API cron or server-side logic in production
            Result.success(Unit)
        }
    }
}
