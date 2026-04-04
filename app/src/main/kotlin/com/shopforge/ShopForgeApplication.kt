package com.shopforge

import android.app.Application
import com.shopforge.di.appModule
import com.shopforge.di.dataModule
import com.shopforge.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShopForgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShopForgeApplication)
            modules(appModule, dataModule, domainModule)
        }
    }
}
