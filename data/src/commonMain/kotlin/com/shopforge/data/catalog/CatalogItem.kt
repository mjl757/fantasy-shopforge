package com.shopforge.data.catalog

import com.shopforge.domain.model.Denomination
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity

/**
 * A lightweight data holder for a built-in catalog item definition.
 * Used to seed the database on first launch.
 *
 * @param name Display name of the item.
 * @param description Flavor text describing the item.
 * @param category The item's category.
 * @param priceAmount Base price amount.
 * @param priceDenomination Denomination for the price.
 * @param rarity How rare the item is.
 */
data class CatalogItem(
    val name: String,
    val description: String,
    val category: ItemCategory,
    val priceAmount: Int,
    val priceDenomination: Denomination,
    val rarity: Rarity,
)
