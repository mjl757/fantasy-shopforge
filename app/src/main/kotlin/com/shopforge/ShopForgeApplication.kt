package com.shopforge

import android.app.Application
import com.shopforge.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class ShopForgeApplication : Application() {

    /**
     * Root DI graph. Lazily initialized on first access so that the graph is
     * only created once the application is fully initialized.
     *
     * Downstream components (Activities, Composables) can access this via
     * `(application as ShopForgeApplication).appGraph`.
     */
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Eagerly initialize the graph so Metro's compile-time validation is
        // exercised on startup rather than on first use.
        appGraph
    }
}
