package com.shopforge.di

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Root DI graph for the application.
 *
 * Bindings for the database, repositories, and use cases will be contributed
 * automatically via @ContributesBinding / @ContributesTo annotations defined in
 * the :data and :domain modules as those issues are implemented.
 *
 * The Application instance is provided via the [Factory] so that any binding
 * that requires an Android [Application] context can depend on it.
 */
@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
