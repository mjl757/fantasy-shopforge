package com.shopforge.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.data.repository.ItemRepositoryImpl
import com.shopforge.data.repository.ShopRepositoryImpl
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = ShopForgeDatabase.Schema,
            context = androidContext(),
            name = "shopforge.db"
        )
    }
    single { ShopForgeDatabase(get()) }
    single<ItemRepository> { ItemRepositoryImpl(get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
}
