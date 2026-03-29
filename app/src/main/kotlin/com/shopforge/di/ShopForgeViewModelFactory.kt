package com.shopforge.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * Metro-backed [androidx.lifecycle.ViewModelProvider.Factory] that delegates to the
 * DI graph for ViewModel creation.
 *
 * Contributed to [AppScope] so that every Activity/Fragment/Composable that uses
 * [dev.zacsweers.metrox.viewmodel.compose.metroViewModel] picks up the correct factory.
 */
@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class ShopForgeViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
) : MetroViewModelFactory()
