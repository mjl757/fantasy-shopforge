package com.shopforge.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.usecase.AddItemToShopUseCase
import com.shopforge.domain.usecase.GetAllItemsUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    val filteredItems: List<Item> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ItemCategory? = null,
    val addedItemIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: UiError? = null,
    /** Non-null when the quantity-picker dialog should be shown for this item. */
    val pendingAddItem: Item? = null,
)

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

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ItemCategory?>(null)
    private val addedItemIds = MutableStateFlow<Set<Long>>(emptySet())
    private val error = MutableStateFlow<UiError?>(null)
    private val pendingAddItem = MutableStateFlow<Item?>(null)

    val uiState: StateFlow<AddItemToShopUiState> = combine(
        combine(getAllItemsUseCase(), searchQuery, selectedCategory) { items, query, category ->
            Triple(items, query, category)
        },
        addedItemIds,
        error,
        pendingAddItem,
    ) { (items, query, category), added, err, pending ->
        val filtered = items.filter { item ->
            (query.isBlank() || item.name.contains(query, ignoreCase = true)) &&
                (category == null || item.category == category)
        }
        AddItemToShopUiState(
            allItems = items,
            filteredItems = filtered,
            searchQuery = query,
            selectedCategory = category,
            addedItemIds = added,
            isLoading = false,
            error = err,
            pendingAddItem = pending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddItemToShopUiState(),
    )

    init {
        // Seed addedItemIds with items already present in this shop so the check
        // marks are correct even if the user navigated away and back.
        viewModelScope.launch {
            val existing = getShopWithInventoryUseCase(shopId).first()?.inventory
            if (!existing.isNullOrEmpty()) {
                addedItemIds.update { existing.map { it.item.id }.toSet() }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.update { query }
    }

    fun onCategorySelected(category: ItemCategory?) {
        selectedCategory.update { category }
    }

    /**
     * Called when the user taps an item in the list.
     * Opens the quantity-picker dialog for that item.
     */
    fun onItemSelected(item: Item) {
        pendingAddItem.update { item }
    }

    /**
     * Called when the user confirms the quantity-picker dialog.
     * A null [quantity] represents unlimited stock.
     */
    fun onAddConfirmed(quantity: Int?) {
        val item = pendingAddItem.value ?: return
        pendingAddItem.update { null }
        viewModelScope.launch {
            try {
                addItemToShopUseCase(shopId, item, quantity, item.price)
                addedItemIds.update { it + item.id }
                error.update { null }
            } catch (e: Exception) {
                error.update { UiError(message = e.message ?: "Failed to add item") }
            }
        }
    }

    /** Called when the user dismisses the quantity-picker dialog without confirming. */
    fun onAddDismissed() {
        pendingAddItem.update { null }
    }

    fun clearError() {
        error.update { null }
    }
}
