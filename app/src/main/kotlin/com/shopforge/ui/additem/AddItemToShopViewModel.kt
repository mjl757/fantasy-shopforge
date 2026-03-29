package com.shopforge.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val errorMessage: String? = null,
)

/**
 * ViewModel for browsing the item catalog and adding items to a shop's inventory.
 *
 * Loads all catalog items via [ItemRepository], supports search and category filtering,
 * and delegates item addition to [ShopRepository.addItemToShop].
 */
class AddItemToShopViewModel(
    private val shopId: Long,
    private val itemRepository: ItemRepository,
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ItemCategory?>(null)
    private val addedItemIds = MutableStateFlow<Set<Long>>(emptySet())
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AddItemToShopUiState> = combine(
        itemRepository.getAllItems(),
        searchQuery,
        selectedCategory,
        addedItemIds,
        errorMessage,
    ) { items, query, category, added, error ->
        val filtered = items.filter { item ->
            val matchesSearch = query.isBlank() ||
                item.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category == category
            matchesSearch && matchesCategory
        }
        AddItemToShopUiState(
            allItems = items,
            filteredItems = filtered,
            searchQuery = query,
            selectedCategory = category,
            addedItemIds = added,
            isLoading = false,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddItemToShopUiState(),
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: ItemCategory?) {
        selectedCategory.value = category
    }

    /**
     * Adds an item to the shop's inventory with the given [quantity].
     * A null [quantity] represents unlimited stock.
     * Uses the item's base price as the adjusted price.
     */
    fun addItem(itemId: Long, quantity: Int?) {
        viewModelScope.launch {
            try {
                val item = itemRepository.getItemById(itemId) ?: return@launch
                shopRepository.addItemToShop(
                    shopId = shopId,
                    item = item,
                    quantity = quantity,
                    adjustedPrice = item.price,
                )
                addedItemIds.value = addedItemIds.value + itemId
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = "Failed to add item: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}
