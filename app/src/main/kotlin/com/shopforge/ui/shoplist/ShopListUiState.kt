package com.shopforge.ui.shoplist

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType

/**
 * Represents the UI state for the Shop List screen.
 */
sealed interface ShopListUiState {
    /** Initial loading state before data is available. */
    data object Loading : ShopListUiState

    /** No shops exist yet. */
    data object Empty : ShopListUiState

    /** Shops are available to display. */
    data class Content(
        val shops: List<Shop>,
        val selectedFilter: ShopType?,
        val sortOrder: ShopSortOrder,
    ) : ShopListUiState
}

/**
 * Sorting options for the shop list.
 */
enum class ShopSortOrder {
    Name,
    CreatedDate,
}

/**
 * One-shot navigation events emitted by the ViewModel.
 */
sealed interface ShopListEvent {
    data class NavigateToShopDetail(val shopId: Long) : ShopListEvent
    data object NavigateToCreateShop : ShopListEvent
    data object NavigateToGenerateShop : ShopListEvent
}
