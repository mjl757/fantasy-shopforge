package com.shopforge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shopforge.ui.screens.AddItemToShopScreen
import com.shopforge.ui.screens.CreateShopScreen
import com.shopforge.ui.screens.EditShopScreen
import com.shopforge.ui.screens.GenerateShopScreen
import com.shopforge.ui.screens.ShopDetailScreen
import com.shopforge.ui.screens.ShopListScreen

/**
 * Single [NavHost] for the entire app.
 *
 * Each composable destination maps to one of the [AppRoute] entries.
 * Navigation callbacks are passed down to screens so they remain
 * navigation-unaware (better testability).
 */
@Composable
fun ShopForgeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.ShopList,
        modifier = modifier,
    ) {
        composable<AppRoute.ShopList> {
            ShopListScreen(
                onShopClick = { shopId ->
                    navController.navigate(AppRoute.ShopDetail(shopId))
                },
                onCreateShop = { navController.navigate(AppRoute.CreateShop) },
                onGenerateShop = { navController.navigate(AppRoute.GenerateShop) },
            )
        }

        composable<AppRoute.ShopDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.ShopDetail>()
            ShopDetailScreen(
                shopId = route.shopId,
                onEditShop = { navController.navigate(AppRoute.EditShop(route.shopId)) },
                onAddItem = { navController.navigate(AppRoute.AddItemToShop(route.shopId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.CreateShop> {
            CreateShopScreen(
                onShopCreated = { shopId ->
                    navController.navigate(AppRoute.ShopDetail(shopId)) {
                        popUpTo(AppRoute.ShopList)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.EditShop> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.EditShop>()
            EditShopScreen(
                shopId = route.shopId,
                onSaved = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(AppRoute.ShopList, inclusive = false)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.GenerateShop> {
            GenerateShopScreen(
                onShopGenerated = { shopId ->
                    navController.navigate(AppRoute.ShopDetail(shopId)) {
                        popUpTo(AppRoute.ShopList)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.AddItemToShop> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.AddItemToShop>()
            AddItemToShopScreen(
                shopId = route.shopId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
