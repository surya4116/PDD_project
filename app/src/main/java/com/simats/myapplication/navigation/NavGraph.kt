package com.simats.myapplication.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.myapplication.ui.screens.auth.ForgotPasswordScreen
import com.simats.myapplication.ui.screens.auth.RegisterScreen
import com.simats.myapplication.ui.screens.auth.RoleSelectionScreen
import com.simats.myapplication.ui.screens.auth.SignInScreen
import com.simats.myapplication.ui.screens.auth.SplashScreen
import com.simats.myapplication.ui.screens.auth.NetworkDiagnosticsScreen
import com.simats.myapplication.ui.screens.user.UserDashboardScreen
import com.simats.myapplication.ui.screens.user.UserTokensScreen
import com.simats.myapplication.ui.screens.user.ServiceCenterDetailsScreen
import com.simats.myapplication.ui.screens.user.SlotBookingScreen
import com.simats.myapplication.ui.screens.user.BookingConfirmationScreen
import com.simats.myapplication.ui.screens.user.LiveQueueTrackingScreen
import com.simats.myapplication.ui.screens.user.NotificationsScreen
import com.simats.myapplication.ui.screens.user.UserProfileScreen
import com.simats.myapplication.ui.screens.user.EditProfileScreen
import com.simats.myapplication.ui.screens.qr.QRScannerScreen
import com.simats.myapplication.ui.screens.qr.CheckInStatusScreen
import com.simats.myapplication.ui.screens.qr.CheckingVerificationScreen
import com.simats.myapplication.ui.screens.qr.CheckInSuccessfulScreen
import com.simats.myapplication.ui.screens.qr.CheckInFailedScreen
import com.simats.myapplication.ui.screens.qr.AlternativeSlotSuggestionScreen

import com.simats.myapplication.ui.screens.provider.ProviderLoginScreen
import com.simats.myapplication.ui.screens.provider.ProviderRegisterScreen
import com.simats.myapplication.ui.screens.provider.ProviderDashboardScreen
import com.simats.myapplication.ui.screens.provider.ProviderProfileScreen
import com.simats.myapplication.ui.screens.provider.ProviderSettingsScreen
import com.simats.myapplication.ui.screens.provider.ServiceCentersManagementScreen
import com.simats.myapplication.ui.screens.provider.AddServiceCenterScreen
import com.simats.myapplication.ui.screens.provider.ServicesManagementScreen
import com.simats.myapplication.ui.screens.provider.SlotScheduleListScreen
import com.simats.myapplication.ui.screens.provider.SlotAndTokenManagementScreen
import com.simats.myapplication.ui.screens.provider.BookingsQueueManagementScreen
import com.simats.myapplication.ui.screens.provider.MarkCompletedScreen
import com.simats.myapplication.ui.screens.provider.ReportsAnalyticsScreen
import com.simats.myapplication.ui.screens.provider.TokenControlPanelScreen
import com.simats.myapplication.ui.screens.provider.BookingManagementScreen
import com.simats.myapplication.ui.screens.provider.QRCheckInControlScreen
import com.simats.myapplication.ui.screens.provider.NotificationDispatcherScreen

import com.simats.myapplication.ui.viewmodel.ProviderViewModel
import com.simats.myapplication.ui.viewmodel.AppViewModelProvider
import com.simats.myapplication.ui.viewmodel.AuthViewModel
import com.simats.myapplication.ui.viewmodel.UserViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object RoleSelection : Screen("role_selection_screen")
    object NetworkDiagnostics : Screen("network_diagnostics_screen")
    object SignIn : Screen("sign_in_screen/{role}") {
        fun createRoute(role: String) = "sign_in_screen/$role"
    }
    object Register : Screen("register_screen")
    object ProviderRegister : Screen("provider_register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object UserDashboard : Screen("user_dashboard_screen")
    object UserTokens : Screen("user_tokens_screen")
    object ProviderDashboard : Screen("provider_dashboard_screen")
    object ServiceCenterDetails : Screen("service_center_details_screen/{centerId}") {
        fun createRoute(centerId: Int) = "service_center_details_screen/$centerId"
    }
    object SlotBooking : Screen("slot_booking_screen/{centerId}") {
        fun createRoute(centerId: Int) = "slot_booking_screen/$centerId"
    }
    object BookingConfirmation : Screen("booking_confirmation_screen/{bookingId}") {
        fun createRoute(bookingId: Int) = "booking_confirmation_screen/$bookingId"
    }
    object LiveQueueTracking : Screen("live_queue_tracking_screen/{bookingId}") {
        fun createRoute(bookingId: Int) = "live_queue_tracking_screen/$bookingId"
    }
    object QRScanner : Screen("qr_scanner_screen")
    object CheckInStatus : Screen("check_in_status_screen")
    object CheckingVerification : Screen("checking_verification_screen")
    object CheckInSuccessful : Screen("check_in_successful_screen")
    object CheckInFailed : Screen("check_in_failed_screen")
    object AlternativeSlotSuggestion : Screen("alternative_slot_suggestion_screen")
    object ServiceCentersManagement : Screen("service_centers_management_screen")
    object AddServiceCenter : Screen("add_service_center_screen")
    object ServicesManagement : Screen("services_management_screen/{centerId}") {
        fun createRoute(centerId: Int) = "services_management_screen/$centerId"
    }
    object SlotScheduleList : Screen("slot_schedule_list_screen/{serviceId}") {
        fun createRoute(serviceId: Int) = "slot_schedule_list_screen/$serviceId"
    }
    object SlotAndTokenManagement : Screen("slot_and_token_management_screen/{serviceId}") {
        fun createRoute(serviceId: Int) = "slot_and_token_management_screen/$serviceId"
    }
    object BookingsQueueManagement : Screen("bookings_queue_management_screen/{slotId}") {
        fun createRoute(slotId: Int) = "bookings_queue_management_screen/$slotId"
    }
    object MarkCompleted : Screen("mark_completed_screen/{bookingId}") {
        fun createRoute(bookingId: Int) = "mark_completed_screen/$bookingId"
    }
    object ReportsAnalytics : Screen("reports_analytics_screen")
    object ProviderProfile : Screen("provider_profile_screen")
    object UserProfile : Screen("user_profile_screen")
    object EditProfile : Screen("edit_profile_screen")
    object Notifications : Screen("notifications_screen")
    object TokenControlPanel : Screen("token_control_panel_screen")
    object BookingManagement : Screen("booking_management_screen")
    object NotificationDispatcher : Screen("notification_dispatcher_screen")
    object QRCheckInControl : Screen("qr_check_in_control_screen")
    object ProviderSettings : Screen("provider_settings_screen")
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
    providerViewModel: ProviderViewModel = viewModel(factory = AppViewModelProvider.Factory),
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateNext = {
                navController.navigate(Screen.RoleSelection.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        
        // 2. Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate(Screen.SignIn.createRoute(role))
                },
                onNavigateToDiagnostics = {
                    navController.navigate(Screen.NetworkDiagnostics.route)
                }
            )
        }

        composable(Screen.NetworkDiagnostics.route) {
            NetworkDiagnosticsScreen(onBack = { navController.popBackStack() })
        }
        
        // 3. Sign In (Customer or Service Provider)
        composable(
            route = Screen.SignIn.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "User"
            if (role == "Provider" || role == "Admin") {
                ProviderLoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.ProviderDashboard.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgot = { navController.navigate(Screen.ForgotPassword.route) },
                    onNavigateToRegister = { navController.navigate(Screen.ProviderRegister.route) }
                )
            } else {
                SignInScreen(
                    viewModel = authViewModel,
                    role = role,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgot = { navController.navigate(Screen.ForgotPassword.route) },
                    onSignInSuccess = {
                        navController.navigate(Screen.UserDashboard.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        // 4. Registration Screens
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ProviderRegister.route) {
            ProviderRegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.ProviderDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 5. Forgot Password
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = { navController.popBackStack() }
            )
        }
        
        // 6. Primary Dashboards
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(
                viewModel = userViewModel,
                onCenterClick = { centerId -> navController.navigate(Screen.ServiceCenterDetails.createRoute(centerId)) },
                onTokensClick = { navController.navigate(Screen.UserTokens.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = false }
                }},
                onProfileClick = { navController.navigate(Screen.UserProfile.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = false }
                }},
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }
        
        composable(Screen.UserTokens.route) {
            UserTokensScreen(
                viewModel = userViewModel,
                onHomeClick = { navController.navigate(Screen.UserDashboard.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = true }
                }},
                onProfileClick = { navController.navigate(Screen.UserProfile.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = false }
                }},
                onTokenClick = { bookingId -> navController.navigate(Screen.LiveQueueTracking.createRoute(bookingId)) }
            )
        }
        
        composable(Screen.ProviderDashboard.route) {
            ProviderDashboardScreen(
                viewModel = providerViewModel,
                onSettingsClick = { navController.navigate(Screen.ProviderSettings.route) },
                onQueuesClick = { navController.navigate(Screen.ServiceCentersManagement.route) },
                onAnalyticsClick = { navController.navigate(Screen.ReportsAnalytics.route) },
                onTokenControlClick = { navController.navigate(Screen.TokenControlPanel.route) },
                onBookingManagementClick = { navController.navigate(Screen.BookingManagement.route) },
                onNotificationDispatcherClick = { navController.navigate(Screen.NotificationDispatcher.route) },
                onQRCheckInClick = { navController.navigate(Screen.QRCheckInControl.route) },
                onProviderProfileClick = { navController.navigate(Screen.ProviderProfile.route) }
            )
        }

        composable(Screen.ServiceCentersManagement.route) {
            ServiceCentersManagementScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() },
                onAddCenter = { navController.navigate(Screen.AddServiceCenter.route) },
                onCenterClick = { centerId -> 
                    providerViewModel.getOrCreateDefaultService(centerId) { serviceId ->
                        navController.navigate(Screen.SlotScheduleList.createRoute(serviceId))
                    }
                }
            )
        }
        
        composable(Screen.AddServiceCenter.route) {
            AddServiceCenterScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ServicesManagement.route,
            arguments = listOf(navArgument("centerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val centerId = backStackEntry.arguments?.getInt("centerId") ?: -1
            ServicesManagementScreen(
                viewModel = providerViewModel,
                centerId = centerId,
                onBack = { navController.popBackStack() },
                onServiceClick = { serviceId -> navController.navigate(Screen.SlotScheduleList.createRoute(serviceId)) }
            )
        }

        composable(
            route = Screen.SlotScheduleList.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: -1
            SlotScheduleListScreen(
                viewModel = providerViewModel,
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onAddSlot = { navController.navigate(Screen.SlotAndTokenManagement.createRoute(serviceId)) },
                onScheduleClick = { slotId -> navController.navigate(Screen.BookingsQueueManagement.createRoute(slotId)) }
            )
        }
        
        composable(
            route = Screen.SlotAndTokenManagement.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: -1
            SlotAndTokenManagementScreen(
                viewModel = providerViewModel,
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.BookingsQueueManagement.route,
            arguments = listOf(navArgument("slotId") { type = NavType.IntType })
        ) { backStackEntry ->
            val slotId = backStackEntry.arguments?.getInt("slotId") ?: -1
            BookingsQueueManagementScreen(
                viewModel = providerViewModel,
                slotId = slotId,
                onBack = { navController.popBackStack() },
                onCompleteClick = { bookingId -> navController.navigate(Screen.MarkCompleted.createRoute(bookingId)) }
            )
        }

        // 7. Customer Booking Flow
        composable(
            route = Screen.ServiceCenterDetails.route,
            arguments = listOf(navArgument("centerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val centerId = backStackEntry.arguments?.getInt("centerId") ?: -1
            ServiceCenterDetailsScreen(
                viewModel = userViewModel,
                centerId = centerId,
                onBack = { navController.popBackStack() },
                onBookSlot = { navController.navigate(Screen.SlotBooking.createRoute(centerId)) }
            )
        }
        
        composable(
            route = Screen.SlotBooking.route,
            arguments = listOf(navArgument("centerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val centerId = backStackEntry.arguments?.getInt("centerId") ?: -1
            SlotBookingScreen(
                viewModel = userViewModel,
                centerId = centerId,
                onBack = { navController.popBackStack() },
                onConfirmBooking = { bookingId ->
                    navController.navigate(Screen.BookingConfirmation.createRoute(bookingId))
                }
            )
        }
        
        composable(
            route = Screen.BookingConfirmation.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: -1
            BookingConfirmationScreen(
                viewModel = userViewModel,
                bookingId = bookingId,
                onTrackQueue = { id ->
                    navController.navigate(Screen.LiveQueueTracking.createRoute(id)) {
                        popUpTo(Screen.UserDashboard.route)
                    }
                },
                onViewTokens = {
                    navController.navigate(Screen.UserTokens.route) {
                        popUpTo(Screen.UserDashboard.route)
                    }
                },
                onBackToHome = {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.UserDashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.LiveQueueTracking.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: -1
            LiveQueueTrackingScreen(
                viewModel = userViewModel,
                bookingId = bookingId,
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        // 8. QR Scanner Flow
        composable(Screen.QRScanner.route) {
            QRScannerScreen(
                onClose = { navController.popBackStack() },
                onScanSuccess = {
                    navController.navigate(Screen.CheckingVerification.route) {
                        popUpTo(Screen.QRScanner.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.CheckingVerification.route) {
            CheckingVerificationScreen(
                viewModel = userViewModel,
                onVerificationComplete = { isSuccess ->
                    if (isSuccess) {
                        navController.navigate(Screen.CheckInSuccessful.route) {
                            popUpTo(Screen.CheckingVerification.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.CheckInFailed.route) {
                            popUpTo(Screen.CheckingVerification.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CheckInSuccessful.route) {
            CheckInSuccessfulScreen(
                onContinue = { navController.popBackStack() }
            )
        }

        composable(Screen.CheckInFailed.route) {
            CheckInFailedScreen(
                onRebook = { navController.navigate(Screen.AlternativeSlotSuggestion.route) }
            )
        }

        composable(Screen.AlternativeSlotSuggestion.route) {
            AlternativeSlotSuggestionScreen(
                onBack = { navController.popBackStack() },
                onReschedule = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CheckInStatus.route) {
            CheckInStatusScreen(
                onViewLiveQueue = { 
                    navController.navigate(Screen.LiveQueueTracking.route) {
                        popUpTo(Screen.CheckInStatus.route) { inclusive = true }
                    }
                }
            )
        }

        // 9. Service Provider Operations
        composable(
            route = Screen.MarkCompleted.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: -1
            MarkCompletedScreen(
                viewModel = providerViewModel,
                bookingId = bookingId,
                onBack = { navController.popBackStack() },
                onCompleted = { navController.popBackStack() }
            )
        }

        composable(Screen.ReportsAnalytics.route) {
            ReportsAnalyticsScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProviderProfile.route) {
            ProviderProfileScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Screen.ProviderSettings.route) },
                onShopInfo = { navController.navigate(Screen.AddServiceCenter.route) },
                onManageServices = { navController.navigate(Screen.ServiceCentersManagement.route) },
                onLogout = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                viewModel = userViewModel,
                onHomeClick = { navController.navigate(Screen.UserDashboard.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = true }
                }},
                onTokensClick = { navController.navigate(Screen.UserTokens.route) {
                    popUpTo(Screen.UserDashboard.route) { inclusive = false }
                }},
                onBookingsClick = { navController.navigate(Screen.UserTokens.route) },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TokenControlPanel.route) {
            TokenControlPanelScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BookingManagement.route) {
            BookingManagementScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QRCheckInControl.route) {
            QRCheckInControlScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificationDispatcher.route) {
            NotificationDispatcherScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProviderSettings.route) {
            ProviderSettingsScreen(
                viewModel = providerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
