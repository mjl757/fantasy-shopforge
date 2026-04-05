package com.shopforge.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.usecase.AddItemToShopUseCase
import com.shopforge.domain.usecase.GetAllItemsUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Wraps an error message with a unique ID so that consecutive identical errors
 * each produce a new key for [LaunchedEffect], ensuring the snackbar always fires.
 */
data class UiError(val id: Long = System.nanoTime(), val message: String)

/**
 * UI state for the Add Item to Shop screen.
 */
data class AddItemToShopUiState(
    val allItems: List<Item> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ItemCategory? = null,
    val addedItemIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: UiError? = null,
    /** Non-null when the quantity-picker dialog should be shown for this item. */
    val pendingAddItem: Item? = null,
) {
    val filteredItems: List<Item> = allItems.filter { item ->
        (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)) &&
            (selectedCategory == null || item.category == selectedCategory)
    }
}

/**
 * ViewModel for browsing the item catalog and adding items to a shop's inventory.
 *
 * Loads all catalog items via [GetAllItemsUseCase], supports search and category
 * filtering, and delegates item addition to [AddItemToShopUseCase].
 * Existing inventory is loaded on init via [GetShopWithInventoryUseCase] so that
 * items already in the shop display with a check mark immediately.
 */
class AddItemToShopViewModel(
    private val shopId: Long,
    private val getAllItemsUseCase: GetAllItemsUseCase,
    private val addItemToShopUseCase: AddItemToShopUseCase,
    private val getShopWithInventoryUseCase: GetShopWithInventoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemToShopUiState())
    val uiState: StateFlow<AddItemToShopUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = getShopWithInventoryUseCase(shopId).first()?.inventory
            if (!existing.isNullOrEmpty()) {
                _uiState.update { it.copy(addedItemIds = existing.map { inv -> inv.item.id }.toSet()) }
            }
        }

        viewModelScope.launch {
            getAllItemsUseCase().collect { items ->
                _uiState.update { it.copy(allItems = items, isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: ItemCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Called when the user taps an item in the list.
     * Opens the quantity-picker dialog for that item.
     */
    fun onItemSelected(item: Item) {
        _uiState.update { it.copy(pendingAddItem = item) }
    }

    /**
     * Called when the user confirms the quantity-picker dialog.
     * A null [quantity] represents unlimited stock.
     */
    fun onAddConfirmed(quantity: Int?) {
        val item = _uiState.value.pendingAddItem ?: return
        _uiState.update { it.copy(pendingAddItem = null) }
        viewModelScope.launch {
            try {
                addItemToShopUseCase(shopId, item, quantity, item.price)
                _uiState.update { it.copy(addedItemIds = it.addedItemIds + item.id, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = UiError(message = e.message ?: "Failed to add item")) }
            }
        }
    }

    /** Called when the user dismisses the quantity-picker dialog without confirming. */
    fun onAddDismissed() {
        _uiState.update { it.copy(pendingAddItem = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
