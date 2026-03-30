package com.shopforge.domain.model

/**
 * A shop paired with its current inventory.
 */
data class ShopWithInventory(
    val shop: Shop,
    val inventory: List<ShopInventoryItem>,
)
