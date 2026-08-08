const API_BASE_URL = '../backend/api';

const api = {
    async request(endpoint, method = 'GET', data = null) {
        const url = `${API_BASE_URL}${endpoint}`;
        const options = { method, headers: { 'Content-Type': 'application/json' } };
        if (data && method !== 'GET') options.body = JSON.stringify(data);
        try {
            const response = await fetch(url, options);
            const text = await response.text();
            try { return JSON.parse(text); }
            catch (e) { console.error('Non-JSON response:', text); throw new Error('Invalid API response'); }
        } catch (error) {
            console.error(`API Error (${endpoint}):`, error);
            throw error;
        }
    },

    // ── Auth
    login: (phone, password) =>
        api.request('/auth/login.php', 'POST', { phone, password }),
    loginProvider: (phone, password) =>
        api.request('/auth/provider_login.php', 'POST', { phone, password }),
    register: (fullname, phone, email, password) =>
        api.request('/auth/register.php', 'POST', { name: fullname, fullname, phone, email, password }),

    // ── Users
    getUser: (userId) => api.request(`/users/get_user.php?id=${userId}`),
    updateProfile: (data) => api.request('/users/update_profile.php', 'POST', data),

    // ── Dashboard
    getDashboardStats: (userId, isProvider = false) =>
        api.request(`/dashboard/get_dashboard_stats.php?${isProvider ? 'provider_id' : 'user_id'}=${userId}`),

    // ── Categories & Centers
    getCategories: () => api.request('/get_categories.php'),
    getCenters: (categoryId = null) =>
        api.request(`/service_centers/get_centers.php${categoryId ? '?categoryId=' + categoryId : ''}`),
    getServices: (centerId) =>
        api.request(`/services/get_services.php?centerId=${centerId}`),

    // ── Slots
    getSlots: (serviceId) => api.request(`/slots/get_slots.php?serviceId=${serviceId}`),
    getProviderSlots: (centerId) => api.request(`/slots/get_slots.php?centerId=${centerId}`),
    addSlot: (data) => api.request('/slots/add_slot.php', 'POST', data),
    updateSlotDelay: (slotId, delayMins) =>
        api.request('/slots/update_slot_delay.php', 'POST', { slotId, delayMins }),

    // ── Bookings
    createBooking: (userId, slotId, centerId) =>
        api.request('/bookings/create_booking.php', 'POST', { userId, slotId, centerId }),
    getBookings: (userId, isProvider = false) =>
        api.request(`/bookings/get_bookings.php?${isProvider ? 'providerId' : 'userId'}=${userId}`),

    // Provider: Accept (Waiting → Running/In Premise)
    acceptBooking: (bookingId) =>
        api.request('/bookings/update_booking.php', 'POST', { action: 'checkin', bookingId }),

    // Provider: Complete
    completeBooking: (bookingId) =>
        api.request('/bookings/update_booking.php', 'POST', { action: 'complete', bookingId }),

    // Provider: Cancel / Reject
    rejectBooking: (bookingId) =>
        api.request('/bookings/update_booking.php', 'POST', { action: 'cancel', bookingId }),

    // User: Cancel own booking
    cancelBooking: (bookingId, userId) =>
        api.request('/bookings/cancel_booking.php', 'POST', { bookingId, userId }),

    // ── Notifications
    getNotifications: (userId) =>
        api.request(`/notifications/get_notifications.php?userId=${userId}`),
    markNotificationRead: (notifId) =>
        api.request('/notifications/mark_read.php', 'POST', { notification_id: notifId }),
    sendNotification: (data) =>
        api.request('/notifications/send_notification.php', 'POST', data),

    // ── Queue
    getQueue: (slotId) => api.request(`/queues/get_queue.php?slotId=${slotId}`),

    // ── Feedback
    addFeedback: (centerId, userId, rating, comment) =>
        api.request('/feedback/add_feedback.php', 'POST', { centerId, userId, rating, comment }),

    // ── Provider Center/Service
    addCenter: (data) => api.request('/service_centers/add_center.php', 'POST', data),
    addService: (data) => api.request('/services/add_service.php', 'POST', data),
};

const session = {
    setUser: (user) => localStorage.setItem('smartq_user', JSON.stringify(user)),
    getUser: () => {
        try { return JSON.parse(localStorage.getItem('smartq_user')); } catch (e) { return null; }
    },
    setProvider: (p) => localStorage.setItem('smartq_provider', JSON.stringify(p)),
    getProvider: () => {
        try { return JSON.parse(localStorage.getItem('smartq_provider')); } catch (e) { return null; }
    },
    clearAll: () => {
        localStorage.removeItem('smartq_user');
        localStorage.removeItem('smartq_provider');
    },
    logout: () => {
        localStorage.removeItem('smartq_user');
        localStorage.removeItem('smartq_provider');
        window.location.href = 'index.html';
    }
};
