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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Shop Detail screen.
 * Loads shop info and inventory reactively, supports search filtering,
 * expandable item rows, quantity controls, price editing, and item removal.
 */
class ShopDetailViewModel(
    private val shopId: Long,
    private val getShopWithInventory: GetShopWithInventoryUseCase,
    private val decrementQuantity: DecrementQuantityUseCase,
    private val incrementQuantity: IncrementQuantityUseCase,
    private val removeItemFromShop: RemoveItemFromShopUseCase,
    private val updateItemAdjustedPrice: UpdateItemAdjustedPriceUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    // Tracks the full unfiltered inventory so quantity operations work correctly
    // even when a search filter is active and hides the target item.
    private val _allInventory = MutableStateFlow<List<ShopInventoryItem>>(emptyList())

    private val _expandedItemId = MutableStateFlow<Long?>(null)
    private val _editingItemId = MutableStateFlow<Long?>(null)
    private val _showDeleteConfirmation = MutableStateFlow(false)
    private val _priceEditError = MutableStateFlow<String?>(null)
    private val _priceEditInput = MutableStateFlow<Pair<Long, String>?>(null)

    private val _editState = combine(
        _editingItemId,
        _showDeleteConfirmation,
        _priceEditError,
    ) { editingId, showDelete, priceError -> Triple(editingId, showDelete, priceError) }

    val uiState: StateFlow<ShopDetailUiState> = combine(
        getShopWithInventory(shopId),
        _searchQuery,
        _expandedItemId,
        _editState,
    ) { shopWithInventory, query, expandedId, (editingId, showDeleteConfirmation, priceEditError) ->
        if (shopWithInventory == null) {
            _allInventory.value = emptyList()
            ShopDetailUiState.NotFound
        } else {
            _allInventory.value = shopWithInventory.inventory
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
                expandedItemId = expandedId,
                editingItem = shopWithInventory.inventory.find { it.item.id == editingId },
                showDeleteConfirmation = showDeleteConfirmation,
                priceEditError = priceEditError,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopDetailUiState.Loading,
    )

    init {
        @OptIn(kotlinx.coroutines.FlowPreview::class)
        _priceEditInput
            .debounce(500L)
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { (itemId, rawInput) ->
                if (_editingItemId.value != itemId) return@onEach
                val gp = rawInput.trim().toDoubleOrNull()
                if (gp == null || gp < 0) {
                    _priceEditError.value = "Please enter a valid non-negative number"
                } else {
                    _priceEditError.value = null
                    updateItemAdjustedPrice(shopId, itemId, Price((gp * Price.CP_PER_GP).toLong()))
                }
            }
            .launchIn(viewModelScope)
    }

    fun searchInventory(query: String) {
        _searchQuery.value = query
    }

    fun decrementQuantity(itemId: Long) {
        val inventoryItem = _allInventory.value.find { it.item.id == itemId } ?: return
        if (inventoryItem.isUnlimitedStock || inventoryItem.isSoldOut) return
        viewModelScope.launch {
            decrementQuantity(shopId, itemId, inventoryItem.quantity)
        }
    }

    fun incrementQuantity(itemId: Long) {
        val inventoryItem = _allInventory.value.find { it.item.id == itemId } ?: return
        if (inventoryItem.isUnlimitedStock) return
        viewModelScope.launch {
            incrementQuantity(shopId, itemId, inventoryItem.quantity)
        }
    }

    fun toggleExpandItem(itemId: Long) {
        _expandedItemId.value = if (_expandedItemId.value == itemId) null else itemId
    }

    fun openEditSheet(itemId: Long) {
        _priceEditError.value = null
        _priceEditInput.value = null
        _editingItemId.value = itemId
    }

    fun dismissBottomSheet() {
        _editingItemId.value = null
        _showDeleteConfirmation.value = false
        _priceEditError.value = null
        _priceEditInput.value = null
    }

    fun requestDeleteItem(itemId: Long) {
        _expandedItemId.value = itemId
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun confirmDeleteItem() {
        val itemId = _expandedItemId.value ?: return
        viewModelScope.launch {
            removeItemFromShop(shopId, itemId)
            _expandedItemId.value = null
            _showDeleteConfirmation.value = false
            // Also close the bottom sheet if it was open for this item
            if (_editingItemId.value == itemId) dismissBottomSheet()
        }
    }

    fun onPriceInputChanged(itemId: Long, rawInput: String) {
        _priceEditInput.value = Pair(itemId, rawInput)
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
