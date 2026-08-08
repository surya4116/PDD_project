package com.simats.myapplication.data.local.entity

data class UserEntity(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val passwordHash: String,
    val isDisabled: Boolean = false
)

data class ProviderEntity(
    val id: Int = 0,
    val shopName: String,
    val name: String,
    val phone: String,
    val email: String,
    val passwordHash: String,
    val categoryId: Int,
    val location: String
)

data class CategoryEntity(
    val id: Int = 0,
    val name: String,
    val iconName: String,
    val prefix: String // e.g. "SLN"
)

data class ServiceCenterEntity(
    val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val address: String,
    val providerId: Int,
    val isActive: Boolean = true
)

data class ServiceEntity(
    val id: Int = 0,
    val centerId: Int,
    val name: String,
    val duration: String
)

data class SlotEntity(
    val id: Int = 0,
    val serviceId: Int,
    val centerId: Int,
    val date: String,
    val startTime: String,
    val endTime: String,
    val maxTokens: Int,
    val currentTokens: Int = 0,
    val status: String = "Upcoming", // Upcoming, Running, Completed
    val delayMins: Int = 0
)

data class BookingEntity(
    val id: Int = 0,
    val userId: Int,
    val slotId: Int,
    val centerId: Int,
    val tokenNumber: String,
    val queuePosition: Int,
    val status: String, // Waiting, In Premise, Completed, Failed
    val bookingTime: Long,
    var userName: String? = ""
)

data class QueueTokenEntity(
    val id: Int = 0,
    val bookingId: Int,
    val tokenNumber: String,
    val estimatedWaitTimeMins: Int,
    val issuedAt: Long
)

data class CheckInEntity(
    val id: Int = 0,
    val bookingId: Int,
    val qrCodeData: String,
    val checkInTime: Long,
    val method: String // "QR" or "Manual"
)

data class BookingWithDetails(
    val booking: BookingEntity,
    val slot: SlotEntity,
    val center: ServiceCenterEntity,
    val queueToken: QueueTokenEntity?
)

data class FeedbackEntity(
    val id: Int = 0,
    val userId: Int,
    val rating: Float,
    val comments: String,
    val timestamp: Long = System.currentTimeMillis(),
    var userName: String? = "Anonymous"
)

data class NotificationEntity(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
