package com.shopforge.ui.shopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.usecase.DecrementQuantityUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
import com.shopforge.domain.usecase.IncrementQuantityUseCase
import com.shopforge.domain.usecase.RemoveItemFromShopUseCase
import com.shopforge.domain.usecase.UpdateItemAdjustedPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Shop Detail screen.
 * Loads shop info and inventory reactively, supports search filtering,
 * expandable item rows, quantity controls, price editing, and item removal.
 */
class ShopDetailViewModel(
    private val shopId: Long,
    getShopWithInventory: GetShopWithInventoryUseCase,
    private val decrementQuantity: DecrementQuantityUseCase,
    private val incrementQuantity: IncrementQuantityUseCase,
    private val removeItemFromShop: RemoveItemFromShopUseCase,
    private val updateItemAdjustedPrice: UpdateItemAdjustedPriceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShopDetailUiState>(ShopDetailUiState.Loading)
    val uiState: StateFlow<ShopDetailUiState> = _uiState.asStateFlow()

    // Tracks the full unfiltered inventory so quantity operations work correctly
    // even when a search filter is active and hides the target item.
    private var allInventory: List<ShopInventoryItem> = emptyList()

    init {
        getShopWithInventory(shopId)
            .onEach { shopWithInventory ->
                if (shopWithInventory == null) {
                    allInventory = emptyList()
                    _uiState.value = ShopDetailUiState.NotFound
                } else {
                    allInventory = shopWithInventory.inventory
                    _uiState.update { current ->
                        val query = (current as? ShopDetailUiState.Loaded)?.searchQuery.orEmpty()
                        val expandedId = (current as? ShopDetailUiState.Loaded)?.expandedItemId
                        val editingId = (current as? ShopDetailUiState.Loaded)?.editingItem?.item?.id
                        val showDelete = (current as? ShopDetailUiState.Loaded)?.showDeleteConfirmation ?: false
                        val priceError = (current as? ShopDetailUiState.Loaded)?.priceEditError

                        ShopDetailUiState.Loaded(
                            shop = shopWithInventory.shop,
                            inventory = filterInventory(shopWithInventory.inventory, query),
                            searchQuery = query,
                            expandedItemId = expandedId,
                            editingItem = shopWithInventory.inventory.find { it.item.id == editingId },
                            showDeleteConfirmation = showDelete,
                            priceEditError = priceError,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun savePrice(itemId: Long, rawInput: String) {
        val gp = rawInput.trim().toDoubleOrNull()
        if (gp == null || gp < 0) {
            updateLoaded { it.copy(priceEditError = "Please enter a valid non-negative number") }
            return
        }
        viewModelScope.launch {
            updateItemAdjustedPrice(shopId, itemId, Price((gp * Price.CP_PER_GP).toLong()))
            dismissBottomSheet()
        }
    }

    fun clearPriceEditError() {
        updateLoaded { it.copy(priceEditError = null) }
    }

    fun searchInventory(query: String) {
        updateLoaded {
            it.copy(
                searchQuery = query,
                inventory = filterInventory(allInventory, query),
            )
        }
    }

    fun decrementQuantity(itemId: Long) {
        val inventoryItem = allInventory.find { it.item.id == itemId } ?: return
        if (inventoryItem.isUnlimitedStock || inventoryItem.isSoldOut) return
        viewModelScope.launch {
            decrementQuantity(shopId, itemId, inventoryItem.quantity)
        }
    }

    fun incrementQuantity(itemId: Long) {
        val inventoryItem = allInventory.find { it.item.id == itemId } ?: return
        if (inventoryItem.isUnlimitedStock) return
        viewModelScope.launch {
            incrementQuantity(shopId, itemId, inventoryItem.quantity)
        }
    }

    fun toggleExpandItem(itemId: Long) {
        updateLoaded { it.copy(expandedItemId = if (it.expandedItemId == itemId) null else itemId) }
    }

    fun openEditSheet(itemId: Long) {
        updateLoaded {
            it.copy(
                editingItem = allInventory.find { inv -> inv.item.id == itemId },
                priceEditError = null,
            )
        }
    }

    fun dismissBottomSheet() {
        updateLoaded {
            it.copy(
                editingItem = null,
                showDeleteConfirmation = false,
                priceEditError = null,
            )
        }
    }

    fun requestDeleteItem(itemId: Long) {
        updateLoaded { it.copy(expandedItemId = itemId, showDeleteConfirmation = true) }
    }

    fun dismissDeleteConfirmation() {
        updateLoaded { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDeleteItem() {
        val state = _uiState.value as? ShopDetailUiState.Loaded ?: return
        val itemId = state.expandedItemId ?: return
        viewModelScope.launch {
            removeItemFromShop(shopId, itemId)
            updateLoaded {
                it.copy(
                    expandedItemId = null,
                    showDeleteConfirmation = false,
                    editingItem = if (it.editingItem?.item?.id == itemId) null else it.editingItem,
                    priceEditError = if (it.editingItem?.item?.id == itemId) null else it.priceEditError,
                )
            }
        }
    }

    private inline fun updateLoaded(block: (ShopDetailUiState.Loaded) -> ShopDetailUiState.Loaded) {
        _uiState.update { if (it is ShopDetailUiState.Loaded) block(it) else it }
    }

    private fun filterInventory(inventory: List<ShopInventoryItem>, query: String): List<ShopInventoryItem> {
        if (query.isBlank()) return inventory
        return inventory.filter { it.item.name.contains(query, ignoreCase = true) }
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
        val expandedItemId: Long? = null,
        val editingItem: ShopInventoryItem? = null,
        val showDeleteConfirmation: Boolean = false,
        val priceEditError: String? = null,
    ) : ShopDetailUiState
}
