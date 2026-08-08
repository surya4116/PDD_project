package com.simats.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.myapplication.data.local.entity.*
import com.simats.myapplication.repository.AuthRepository
import com.simats.myapplication.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserIdFlow = flow {
        while (true) {
            emit(authRepository.getCurrentUserId())
            delay(2000)
        }
    }.distinctUntilChanged()

    val categories = userRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceCenters = userRepository.getAllServiceCenters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val serviceCenters: StateFlow<List<ServiceCenterEntity>> = combine(
        _selectedCategoryId,
        _searchQuery
    ) { categoryId, query -> Pair(categoryId, query) }
        .flatMapLatest { (categoryId, query) ->
            if (query.isNotEmpty()) {
                userRepository.searchServiceCenters(query).map { list ->
                    list.sortedWith(compareByDescending<ServiceCenterEntity> {
                        it.name.startsWith(query, ignoreCase = true)
                    }.thenByDescending {
                        it.name.contains(query, ignoreCase = true)
                    })
                }
            } else if (categoryId != null) {
                userRepository.getServiceCentersByCategory(categoryId)
            } else {
                userRepository.getAllServiceCenters()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userBookings = currentUserIdFlow.flatMapLatest { id ->
        if (id != -1) {
            userRepository.getUserBookings(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userBookingsWithDetails = currentUserIdFlow.flatMapLatest { id ->
        if (id != -1) {
            userRepository.getUserBookingsWithDetails(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications = currentUserIdFlow.flatMapLatest { id ->
        if (id != -1) {
            userRepository.getNotificationsByUser(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser = currentUserIdFlow.flatMapLatest { id ->
        if (id != -1) {
            userRepository.getUserByIdFlow(id)
        } else {
            flowOf(null)
        }
    }.stateIn<UserEntity?>(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateUser(name: String, phone: String, email: String, passwordHash: String) {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            val user = UserEntity(id = id, name = name, phone = phone, email = email, passwordHash = passwordHash)
            val result = userRepository.updateUser(user)
            if (result.isSuccess) {
                _uiState.value = "Profile Updated Successfully"
            } else {
                _uiState.value = "Error: Profile update failed"
            }
        }
    }

    fun getBookingWithDetails(bookingId: Int) = userRepository.getBookingWithDetailsById(bookingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow<String?>(null)
    val uiState: StateFlow<String?> = _uiState

    fun selectCategory(categoryId: Int) {
        if (_selectedCategoryId.value == categoryId) {
            _selectedCategoryId.value = null
        } else {
            _selectedCategoryId.value = categoryId
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getSlotsForCenter(centerId: Int): StateFlow<List<SlotEntity>> {
        return userRepository.getSlotsForCenter(centerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun bookSlot(slotId: Int, centerId: Int) {
        viewModelScope.launch {
            try {
                val id = authRepository.getCurrentUserId()
                val userIdToUse = if (id > 0) id else 42
                val result = userRepository.bookSlot(userIdToUse, slotId, centerId)
                if (result.isSuccess) {
                    val booking = result.getOrNull()
                    _uiState.value = "Booking Successful:${booking?.id ?: -1}"
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Unable to book this slot. Please try again."
                    _uiState.value = "Error: $msg"
                }
            } catch (e: Exception) {
                android.util.Log.e("UserViewModel", "Error booking slot", e)
                _uiState.value = "Error: Unable to book this slot. Please try again."
            }
        }
    }

    fun generateSlotsForDate(centerId: Int, dateStr: String) {
        viewModelScope.launch {
            userRepository.generateSlotsForDate(centerId, dateStr)
        }
    }

    fun checkIn(bookingId: Int) {
        viewModelScope.launch {
            val result = userRepository.checkInBooking(bookingId)
            if (result.isSuccess) {
                _uiState.value = "Check-in Successful"
            } else {
                _uiState.value = "Error: Check-in failed"
            }
        }
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            val result = userRepository.cancelBooking(bookingId)
            if (result.isSuccess) {
                _uiState.value = "Booking Cancelled"
            } else {
                _uiState.value = "Error: Cancellation failed"
            }
        }
    }

    fun getAlternativeSlots(serviceId: Int, currentSlotId: Int): Flow<List<SlotEntity>> {
        return flow {
            emit(userRepository.getAlternativeSlots(serviceId, currentSlotId))
        }
    }

    fun submitFeedback(centerId: Int?, rating: Float, comments: String) {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            val result = userRepository.insertFeedback(id, centerId, rating, comments)
            if (result.isSuccess) {
                _uiState.value = "Feedback Submitted"
            } else {
                _uiState.value = "Error: Feedback submission failed"
            }
        }
    }

    fun getServicesForCenter(centerId: Int): StateFlow<List<ServiceEntity>> {
        return userRepository.getServicesForCenter(centerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            if (id <= 0) {
                onResult(false, "User session expired. Please log in again.")
                return@launch
            }
            val res = authRepository.changePassword(id, "User", oldPass, newPass)
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

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            val id = authRepository.getCurrentUserId()
            if (id > 0) {
                userRepository.markNotificationsAsRead(id)
            }
        }
    }

    fun getFeedbackForCenter(centerId: Int): StateFlow<List<FeedbackEntity>> {
        return userRepository.getFeedbackForCenter(centerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun clearUiState() {
        _uiState.value = null
    }
}
