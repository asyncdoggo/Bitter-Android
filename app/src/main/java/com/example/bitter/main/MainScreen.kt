package com.example.bitter.main

import LoginScreenSetup
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitter.data.Routes
import com.example.bitter.home.NewPostScreen
import com.example.bitter.home.StartHomeScreen
import com.example.bitter.passwordReset.ForgotPassScreen
import com.example.bitter.profile.EditProfileScreenSetup
import com.example.bitter.register.RegisterScreen
import com.example.bitter.userprofile.UserProfileScreen

@Composable
fun MainScreen() {
    val outerNavController = rememberNavController()

    NavHost(
        navController = outerNavController,
        startDestination = Routes.LoginScreen.route + "/{error}"
    ) {
        composable(
            route = Routes.LoginScreen.route + "/{error}",
            arguments = listOf(
                navArgument("error") {
                    type = NavType.StringType
                }
            )
        ) {
            LoginScreenSetup(
                navController = outerNavController,
                it.arguments?.getString("error")
            )
        }

        composable(Routes.RegisterScreen.route) {
            RegisterScreen(navController = outerNavController)
        }

        composable(Routes.ForgotPassScreen.route) {
            ForgotPassScreen(navController = outerNavController)
        }

        composable(Routes.MainScreen.route) {
            BottomNavHost(outerNavController)
        }

        composable(Routes.EditUserProfileScreen.route) {
            EditProfileScreenSetup(navController = outerNavController)
        }
        composable(Routes.NewPostScreen.route){
            NewPostScreen(navController = outerNavController)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BottomNavHost(outerNavController: NavController) {
    val innerNavController:NavHostController = rememberNavController()

    BackHandler {
        outerNavController.popBackStack()
    }

    Scaffold(
        bottomBar = { BottomNav(navController = innerNavController) }
    ) {
        NavHost(innerNavController, startDestination = Routes.Home.route) {
            composable(Routes.Home.route) {
                StartHomeScreen(navController = outerNavController)
            }
            composable(Routes.Profile.route) {
                UserProfileScreen(outerNavController = outerNavController, innerNavController = innerNavController)
            }
            composable(Routes.Chat.route) {
                Text(text = "Coming soon") // TODO: Chatting, fullname
            }
        }
    }
}


