package com.simats.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.myapplication.data.local.entity.*
import com.simats.myapplication.repository.ProviderRepository
import com.simats.myapplication.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProviderViewModel(
    private val providerRepository: ProviderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val todayStr = sdf.format(java.util.Date())
                    providerRepository.autoCompletePastSlotsAndBookings(todayStr)
                } catch (e: Exception) {
                    android.util.Log.e("ProviderViewModel", "Auto complete error", e)
                }
                delay(10000)
            }
        }
    }

    private val currentProviderIdFlow = flow {
        while (true) {
            emit(authRepository.getCurrentUserId())
            delay(2000)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentProvider = currentProviderIdFlow.flatMapLatest { id ->
        if (id != -1) {
            flow<ProviderEntity?> {
                while (true) {
                    val user = authRepository.getCurrentUser()
                    if (user is ProviderEntity) {
                        emit(user)
                    } else {
                        emit(null)
                    }
                    delay(4000)
                }
            }
        } else {
            flowOf<ProviderEntity?>(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories = providerRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val serviceCenters = currentProviderIdFlow.flatMapLatest { id ->
        if (id != -1) {
            providerRepository.getServiceCentersByProvider(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myBookingsWithDetails = currentProviderIdFlow.flatMapLatest { id ->
        if (id != -1) {
            providerRepository.getBookingsForProvider(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<String?>(null)
    val uiState: StateFlow<String?> = _uiState

    fun updateBookingStatus(booking: BookingEntity, status: String) {
        viewModelScope.launch {
            val updated = booking.copy(status = status)
            providerRepository.updateBooking(updated)
            
            providerRepository.sendNotification(
                userId = booking.userId,
                title = "Booking $status",
                message = "Your booking for token ${booking.tokenNumber} has been updated to $status by your Service Provider."
            )
            _uiState.value = "Booking Status Updated to $status"
        }
    }

    fun deleteBooking(booking: BookingEntity) {
        viewModelScope.launch {
            providerRepository.deleteBooking(booking)
            _uiState.value = "Booking Deleted"
        }
    }

    fun sendNotification(userId: Int, title: String, message: String) {
        viewModelScope.launch {
            providerRepository.sendNotification(userId, title, message)
            _uiState.value = "Notification Dispatched"
        }
    }

    fun updateService(service: ServiceEntity) {
        viewModelScope.launch {
            providerRepository.updateService(service)
            _uiState.value = "Service Updated"
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            providerRepository.deleteService(service)
            _uiState.value = "Service Deleted"
        }
    }

    fun addServiceCenter(name: String, categoryId: Int, address: String) {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            val center = ServiceCenterEntity(name = name, categoryId = categoryId, address = address, providerId = id)
            providerRepository.addServiceCenter(center)
            _uiState.value = "Service Center Added"
        }
    }

    fun updateServiceCenter(center: ServiceCenterEntity) {
        viewModelScope.launch {
            providerRepository.updateServiceCenter(center)
            _uiState.value = "Service Center Updated"
        }
    }

    fun deleteServiceCenter(center: ServiceCenterEntity) {
        viewModelScope.launch {
            providerRepository.deleteServiceCenter(center)
            _uiState.value = "Service Center Deleted"
        }
    }

    fun getServicesForCenter(centerId: Int): StateFlow<List<ServiceEntity>> {
        return providerRepository.getServicesForCenter(centerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getOrCreateDefaultService(centerId: Int, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val services = providerRepository.getServicesForCenter(centerId).first()
                if (services.isNotEmpty()) {
                    onComplete(services.first().id)
                } else {
                    val service = ServiceEntity(centerId = centerId, name = "Daily Schedule", duration = "N/A")
                    val result = providerRepository.addService(service)
                    if (result.isSuccess) {
                        onComplete(result.getOrNull()?.toInt() ?: -1)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProviderViewModel", "Error getting default service", e)
            }
        }
    }

    fun addService(centerId: Int, name: String, duration: String) {
        viewModelScope.launch {
            val service = ServiceEntity(centerId = centerId, name = name, duration = duration)
            providerRepository.addService(service)
            _uiState.value = "Service Added"
        }
    }

    fun addServiceWithSlots(
        centerId: Int,
        name: String,
        duration: String,
        date: String,
        startTime: String,
        endTime: String,
        tokensPerSlot: Int
    ) {
        viewModelScope.launch {
            val service = ServiceEntity(centerId = centerId, name = name, duration = duration)
            val result = providerRepository.addService(service)
            if (result.isSuccess) {
                val serviceId = result.getOrNull()?.toInt() ?: return@launch
                val durationMins = duration.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 15
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                try {
                    val startCalendar = java.util.Calendar.getInstance().apply {
                        time = timeFormat.parse(startTime) ?: return@launch
                    }
                    val endCalendar = java.util.Calendar.getInstance().apply {
                        time = timeFormat.parse(endTime) ?: return@launch
                    }

                    var slotsAddedCount = 0
                    while (startCalendar.timeInMillis < endCalendar.timeInMillis) {
                        val slotStart = timeFormat.format(startCalendar.time)
                        startCalendar.add(java.util.Calendar.MINUTE, durationMins)
                        if (startCalendar.timeInMillis > endCalendar.timeInMillis) break
                        val slotEnd = timeFormat.format(startCalendar.time)

                        val slot = SlotEntity(
                            serviceId = serviceId,
                            centerId = centerId,
                            date = date,
                            startTime = slotStart,
                            endTime = slotEnd,
                            maxTokens = tokensPerSlot
                        )
                        providerRepository.addSlot(slot)
                        slotsAddedCount++
                    }
                    _uiState.value = "Service Added and $slotsAddedCount Slots Generated"
                } catch (e: Exception) {
                    android.util.Log.e("ProviderViewModel", "Error generating slots", e)
                    _uiState.value = "Service Added but Slot Generation Failed"
                }
            } else {
                _uiState.value = "Failed to Add Service"
            }
        }
    }

    fun getSlotsForService(serviceId: Int): StateFlow<List<SlotEntity>> {
        return providerRepository.getSlotsForService(serviceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addSlot(serviceId: Int, date: String, startTime: String, endTime: String, maxTokens: Int) {
        viewModelScope.launch {
            val service = providerRepository.getServiceById(serviceId)
            if (service != null) {
                val slot = SlotEntity(
                    serviceId = serviceId,
                    centerId = service.centerId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    maxTokens = maxTokens
                )
                providerRepository.addSlot(slot)
                android.util.Log.d("ProviderViewModel", "Slot Creation Success: Service $serviceId, Max Tokens $maxTokens")
                _uiState.value = "Slot Added"
            }
        }
    }

    fun deleteSlot(slot: SlotEntity) {
        viewModelScope.launch {
            providerRepository.deleteSlot(slot)
            _uiState.value = "Work Slot Deleted"
        }
    }

    var initialBookingFilter = "All"

    fun getActiveQueueForSlot(slotId: Int): Flow<List<BookingEntity>> {
        return providerRepository.getActiveQueueForSlot(slotId)
    }

    fun markCompleted(bookingId: Int) {
        viewModelScope.launch {
            providerRepository.markBookingCompleted(bookingId)
            android.util.Log.d("ProviderViewModel", "Queue Update: Booking $bookingId marked as Completed")
            _uiState.value = "Booking Marked Completed"
        }
    }

    fun callNextBooking(slotId: Int) {
        viewModelScope.launch {
            val result = providerRepository.callNextBooking(slotId)
            if (result.isSuccess) {
                _uiState.value = "Next Token Called"
            } else {
                _uiState.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun skipBooking(bookingId: Int) {
        viewModelScope.launch {
            val result = providerRepository.skipBooking(bookingId)
            if (result.isSuccess) {
                _uiState.value = "Token Skipped / Cancelled"
            } else {
                _uiState.value = "Error: Skipping failed"
            }
        }
    }

    fun getBookingById(bookingId: Int, onResult: (BookingEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(providerRepository.getBookingById(bookingId))
        }
    }

    fun setSlotDelay(slotId: Int, delayMins: Int) {
        viewModelScope.launch {
            providerRepository.updateSlotDelay(slotId, delayMins)
            _uiState.value = "Delay Updated to $delayMins mins"
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            if (id <= 0) {
                onResult(false, "Provider session expired. Please log in again.")
                return@launch
            }
            val res = authRepository.changePassword(id, "Provider", oldPass, newPass)
            if (res.isSuccess) {
                val msg = res.getOrDefault("Password updated successfully")
                _uiState.value = msg
                onResult(true, msg)
            } else {
                val err = res.exceptionOrNull()?.message ?: "Failed to change password"
                _uiState.value = err
                onResult(false, err)
            }
        }
    }

    fun clearUiState() {
        _uiState.value = null
    }

    fun addSlotsBatch(serviceId: Int, date: String, startTime: String, endTime: String, durationMins: Int, tokensPerSlot: Int) {
        viewModelScope.launch {
            val service = providerRepository.getServiceById(serviceId) ?: return@launch
            val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            try {
                val startCalendar = java.util.Calendar.getInstance().apply {
                    time = timeFormat.parse(startTime) ?: return@launch
                }
                val endCalendar = java.util.Calendar.getInstance().apply {
                    time = timeFormat.parse(endTime) ?: return@launch
                }

                var slotsAddedCount = 0
                while (startCalendar.timeInMillis < endCalendar.timeInMillis) {
                    val slotStart = timeFormat.format(startCalendar.time)
                    startCalendar.add(java.util.Calendar.MINUTE, durationMins)
                    if (startCalendar.timeInMillis > endCalendar.timeInMillis) break
                    val slotEnd = timeFormat.format(startCalendar.time)

                    val slot = SlotEntity(
                        serviceId = serviceId,
                        centerId = service.centerId,
                        date = date,
                        startTime = slotStart,
                        endTime = slotEnd,
                        maxTokens = tokensPerSlot
                    )
                    providerRepository.addSlot(slot)
                    slotsAddedCount++
                }
                _uiState.value = "$slotsAddedCount Work Slots Generated Successfully"
            } catch (e: Exception) {
                android.util.Log.e("ProviderViewModel", "Error generating slots", e)
                _uiState.value = "Error: Invalid Time Format"
            }
        }
    }

    fun saveLastSlotConfig(startTime: String, endTime: String, numSlots: Int, tokensPerSlot: Int, date: String) {
        authRepository.saveLastSlotConfig(startTime, endTime, numSlots, tokensPerSlot, date)
    }
    fun getLastSlotStartTime(): String = authRepository.getLastSlotStartTime()
    fun getLastSlotEndTime(): String = authRepository.getLastSlotEndTime()
    fun getLastSlotNumSlots(): Int = authRepository.getLastSlotNumSlots()
    fun getLastSlotTokenCount(): Int = authRepository.getLastSlotTokensPerSlot()
    fun getLastSlotDate(): String = authRepository.getLastSlotDate()

    fun getServiceById(serviceId: Int, onResult: (ServiceEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(providerRepository.getServiceById(serviceId))
        }
    }
}


