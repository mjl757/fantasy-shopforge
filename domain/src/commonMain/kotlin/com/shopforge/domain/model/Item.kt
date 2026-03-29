package com.shopforge.domain.model

/**
 * Represents an item in the game world, either from the built-in catalog
 * or created by the Game Master.
 *
 * @param id Unique identifier for the item.
 * @param name Display name (e.g. "Longsword", "Potion of Healing").
 * @param description Optional flavor text or mechanical description.
 * @param category The item's type/category, used for filtering and shop generation.
 * @param price Base price of the item in the catalog.
 * @param rarity How rare this item is; primarily relevant for magic items.
 * @param isCustom True if this item was created by the GM, false if from the built-in catalog.
 */
data class Item(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: ItemCategory,
    val price: Price,
    val rarity: Rarity,
    val isCustom: Boolean,
)
