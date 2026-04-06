package com.shopforge.ui.shoplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.GetAllShopsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * ViewModel for the Shop List screen.
 *
 * Collects the reactive shop list from [GetAllShopsUseCase], applies filtering
 * and sorting, and exposes the result as [ShopListUiState].
 */
class ShopListViewModel(
    private val getAllShopsUseCase: GetAllShopsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShopListUiState>(ShopListUiState.Loading)
    val uiState: StateFlow<ShopListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ShopListEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _selectedFilter = MutableStateFlow<ShopType?>(null)
    private val _sortOrder = MutableStateFlow(ShopSortOrder.Name)

    init {
        observeShops()
    }

    private fun observeShops() {
        combine(
            getAllShopsUseCase(),
            _selectedFilter,
            _sortOrder,
        ) { shops, filter, sortOrder ->
            val filtered = if (filter != null) {
                shops.filter { it.type == filter }
            } else {
                shops
            }

            val sorted = when (sortOrder) {
                ShopSortOrder.Name -> filtered.sortedBy { it.name.lowercase() }
                ShopSortOrder.CreatedDate -> filtered.sortedByDescending { it.createdAt }
            }

            if (shops.isEmpty() && filter == null) {
                ShopListUiState.Empty
            } else {
                ShopListUiState.Content(
                    shops = sorted,
                    selectedFilter = filter,
                    sortOrder = sortOrder,
                )
            }
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun onFilterSelected(shopType: ShopType?) {
        _selectedFilter.value = shopType
    }

    fun onSortOrderChanged(sortOrder: ShopSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun onShopClicked(shopId: Long) {
        _events.trySend(ShopListEvent.NavigateToShopDetail(shopId))
    }

    fun onCreateShopClicked() {
        _events.trySend(ShopListEvent.NavigateToCreateShop)
    }

    fun onGenerateShopClicked() {
        _events.trySend(ShopListEvent.NavigateToGenerateShop)
    }
}
