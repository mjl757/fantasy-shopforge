package com.shopforge.domain.model

/**
 * An item as it appears in a specific shop's inventory.
 *
 * @param item The underlying item definition.
 * @param quantity How many are currently in stock.
 *                 Null represents unlimited stock (displayed as ∞ in the UI).
 *                 Zero means the item is sold out.
 * @param adjustedPrice The price for this item in this shop, which may differ
 *                      from the base catalog price due to generation variance (±10%).
 */
data class ShopInventoryItem(
    val item: Item,
    val quantity: Int?,
    val adjustedPrice: Price,
) {
    init {
        require(quantity == null || quantity >= 0) { "Quantity cannot be negative. Got: $quantity" }
    }
    /** True when the item has a finite quantity of zero. */
    val isSoldOut: Boolean get() = quantity != null && quantity == 0

    /** True when the item has unlimited stock. */
    val isUnlimitedStock: Boolean get() = quantity == null
}
