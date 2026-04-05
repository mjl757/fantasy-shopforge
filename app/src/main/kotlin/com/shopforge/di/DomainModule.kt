package com.shopforge.di

import com.shopforge.domain.usecase.AddItemToShopUseCase
import com.shopforge.domain.usecase.CreateShopUseCase
import com.shopforge.domain.usecase.DecrementQuantityUseCase
import com.shopforge.domain.usecase.DeleteShopUseCase
import com.shopforge.domain.usecase.GenerateInventoryUseCase
import com.shopforge.domain.usecase.GenerateShopUseCase
import com.shopforge.domain.usecase.GetAllShopsUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
import com.shopforge.domain.usecase.RegenerateInventoryUseCase
import com.shopforge.domain.usecase.RemoveItemFromShopUseCase
import com.shopforge.domain.usecase.UpdateShopUseCase
import org.koin.dsl.module

val domainModule = module {
    single { GetAllShopsUseCase(get()) }
    single { GetShopWithInventoryUseCase(get()) }
    single { CreateShopUseCase(get(), clock = System::currentTimeMillis) }
    single { UpdateShopUseCase(get(), clock = System::currentTimeMillis) }
    single { DeleteShopUseCase(get()) }
    single { AddItemToShopUseCase(get()) }
    single { RemoveItemFromShopUseCase(get()) }
    single { DecrementQuantityUseCase(get()) }
    single { GenerateInventoryUseCase(get()) }
    single { GenerateShopUseCase(get(), get(), clock = System::currentTimeMillis) }
    single { RegenerateInventoryUseCase(get(), get()) }
}
