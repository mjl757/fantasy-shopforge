package com.shopforge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shopforge.ui.additem.AddItemToShopScreen
import com.shopforge.ui.additem.AddItemToShopViewModel
import com.shopforge.ui.generate.GenerateShopScreen
import com.shopforge.ui.generate.GenerateShopViewModel
import com.shopforge.ui.screen.createshop.CreateShopScreen
import com.shopforge.ui.screen.createshop.CreateShopViewModel
import com.shopforge.ui.screen.editshop.EditShopScreen
import com.shopforge.ui.screen.editshop.EditShopViewModel
import com.shopforge.ui.screens.ShopListScreen
import com.shopforge.ui.shopdetail.ShopDetailScreen
import com.shopforge.ui.shopdetail.ShopDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            val viewModel: ShopDetailViewModel = koinViewModel(
                parameters = { parametersOf(route.shopId.toLong()) }
            )
            ShopDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditShop = { navController.navigate(AppRoute.EditShop(route.shopId)) },
                onAddItem = { navController.navigate(AppRoute.AddItemToShop(route.shopId.toLong())) },
            )
        }

        composable<AppRoute.CreateShop> {
            val viewModel: CreateShopViewModel = koinViewModel()
            CreateShopScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onShopCreated = { shopId ->
                    navController.navigate(AppRoute.ShopDetail(shopId.toString())) {
                        popUpTo(AppRoute.ShopList) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<AppRoute.EditShop> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.EditShop>()
            val shopId = route.shopId.toLongOrNull() ?: return@composable
            val viewModel: EditShopViewModel = koinViewModel(
                parameters = { parametersOf(shopId) }
            )
            EditShopScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onShopDeleted = { navController.popBackStack(AppRoute.ShopList, inclusive = false) },
            )
        }

        composable<AppRoute.GenerateShop> {
            val viewModel: GenerateShopViewModel = koinViewModel()
            GenerateShopScreen(
                viewModel = viewModel,
                onShopGenerated = { shopId ->
                    navController.navigate(AppRoute.ShopDetail(shopId.toString())) {
                        popUpTo(AppRoute.ShopList) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.AddItemToShop> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.AddItemToShop>()
            val viewModel: AddItemToShopViewModel = koinViewModel(
                parameters = { parametersOf(route.shopId) }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AddItemToShopScreen(
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onCategorySelected = viewModel::onCategorySelected,
                onItemTap = viewModel::onItemSelected,
                onAddConfirmed = viewModel::onAddConfirmed,
                onAddDismissed = viewModel::onAddDismissed,
                onClearError = viewModel::clearError,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
