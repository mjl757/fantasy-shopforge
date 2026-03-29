package com.shopforge.ui.shopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.usecase.DecrementQuantityUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Shop Detail screen.
 * Loads shop info and inventory reactively, supports search filtering
 * and tap-to-decrement for session reference.
 */
class ShopDetailViewModel(
    private val shopId: Long,
    private val getShopWithInventory: GetShopWithInventoryUseCase,
    private val decrementQuantity: DecrementQuantityUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ShopDetailUiState> = combine(
        getShopWithInventory(shopId),
        _searchQuery,
    ) { shopWithInventory, query ->
        if (shopWithInventory == null) {
            ShopDetailUiState.NotFound
        } else {
            val filteredInventory = if (query.isBlank()) {
                shopWithInventory.inventory
            } else {
                shopWithInventory.inventory.filter { inventoryItem ->
                    inventoryItem.item.name.contains(query, ignoreCase = true)
                }
            }
            ShopDetailUiState.Loaded(
                shop = shopWithInventory.shop,
                inventory = filteredInventory,
                searchQuery = query,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopDetailUiState.Loading,
    )

    fun searchInventory(query: String) {
        _searchQuery.value = query
    }

    fun decrementQuantity(itemId: Long) {
        val state = uiState.value
        if (state !is ShopDetailUiState.Loaded) return

        val inventoryItem = state.inventory.find { it.item.id == itemId } ?: return

        // No-op for unlimited stock or sold-out items
        if (inventoryItem.isUnlimitedStock || inventoryItem.isSoldOut) return

        viewModelScope.launch {
            decrementQuantity(shopId, itemId, inventoryItem.quantity)
        }
    }
}

/**
 * UI state for the Shop Detail screen.
 */
sealed interface ShopDetailUiState {
    data object Loading : ShopDetailUiState
    data object NotFound : ShopDetailUiState
    data class Loaded(
        val shop: Shop,
        val inventory: List<ShopInventoryItem>,
        val searchQuery: String,
    ) : ShopDetailUiState
}
