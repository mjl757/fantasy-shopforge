package com.shopforge.di

import com.shopforge.domain.repository.ShopRepository
import com.shopforge.ui.additem.AddItemToShopViewModel
import com.shopforge.ui.generate.GenerateShopViewModel
import com.shopforge.ui.screen.createshop.CreateShopViewModel
import com.shopforge.ui.screen.editshop.EditShopViewModel
import com.shopforge.ui.shopdetail.ShopDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { (shopId: Long) -> ShopDetailViewModel(shopId, get(), get()) }
    viewModel { GenerateShopViewModel(get()) }
    viewModel { (shopId: Long) -> AddItemToShopViewModel(shopId, get(), get(), get()) }
    viewModel { CreateShopViewModel(get(), get()) }
    viewModel { (shopId: Long) ->
        EditShopViewModel(
            shopId = shopId,
            updateShopUseCase = get(),
            deleteShopUseCase = get(),
            regenerateInventoryUseCase = get(),
            getShopFlow = get<ShopRepository>()::getShopById,
        )
    }
}
