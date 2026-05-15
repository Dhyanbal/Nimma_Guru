package com.example.nimmaguru.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

import com.example.nimmaguru.presentation.screens.*

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("onboarding") {
            OnboardingScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("signup") {
            SignupScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("search") {
            SearchScreen(navController)
        }

        composable("fame") {
            FameScreen(navController)
        }

        composable("guruDashboard") {
            GuruDashboardScreen(navController)
        }

        composable("adminPanel") {
            AdminScreen(navController)
        }

        composable("chatList") {
            ChatListScreen(navController)
        }

        composable(
            route = "chat/{receiverId}/{receiverName}",
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
            ChatScreen(receiverId, receiverName, navController)
        }

        composable("favorites") {
            FavoritesScreen(navController)
        }

        composable("settings") {
            SettingsScreen(navController)
        }

        composable("editProfile") {
            EditProfileScreen(navController)
        }

        composable("notifications") {
            NotificationScreen(navController)
        }

        composable("myBookings") {
            CalendarScreen(navController)
        }

        composable("helpSupport") {
            HelpSupportScreen(navController)
        }

        composable("privacyPolicy") {
            PrivacyPolicyScreen(navController)
        }

        composable("termsConditions") {
            TermsConditionsScreen(navController)
        }

        composable(
            route = "profile/{guruId}",
            arguments = listOf(
                navArgument("guruId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val guruId =
                backStackEntry.arguments
                    ?.getString("guruId")
                    ?: ""

            GuruProfileScreen(
                guruId = guruId,
                navController = navController
            )
        }

        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        composable("registerGuru") {
            GuruRegistrationScreen(navController)
        }

        composable("createSession") {
            CreateSessionScreen(navController)
        }

        composable(
            route = "booking/{guruId}/{guruName}",
            arguments = listOf(
                navArgument("guruId") {
                    type = NavType.StringType
                },
                navArgument("guruName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val guruId =
                backStackEntry.arguments
                    ?.getString("guruId")
                    ?: ""

            val guruName =
                backStackEntry.arguments
                    ?.getString("guruName")
                    ?: ""

            BookingScreen(
                guruId = guruId,
                guruName = guruName,
                navController = navController
            )
        }
    }
}