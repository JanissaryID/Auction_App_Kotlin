package com.polytron.auctionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

//@Composable
//fun AppNavHost(navController: NavHostController = rememberNavController()) {
//    NavHost(navController = navController, startDestination = Screen.Home.route) {
//        composable(Screen.Home.route) {
//            val vm: HomeViewModel = koinViewModel()
//            HomeScreen(vm) { itemId ->
//                navController.navigate(Screen.Detail.createRoute(itemId))
//            }
//        }
//        composable(
//            route = Screen.Detail.route,
//            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
//            val vm: DetailViewModel = koinViewModel()
//            DetailScreen(vm, itemId)
//        }
//    }
//}