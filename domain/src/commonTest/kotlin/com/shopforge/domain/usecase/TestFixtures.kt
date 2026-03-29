package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem

/**
 * Shared test fixtures for use case tests.
 */
object TestFixtures {

    const val FIXED_TIME = 1_000_000L

    fun sampleItem(
        id: Long = 1L,
        name: String = "Longsword",
        category: ItemCategory = ItemCategory.Weapon,
        price: Price = Price.ofGold(15),
        rarity: Rarity = Rarity.Common,
        isCustom: Boolean = false,
    ) = Item(
        id = id,
        name = name,
        category = category,
        price = price,
        rarity = rarity,
        isCustom = isCustom,
    )

    fun sampleInventoryItem(
        item: Item = sampleItem(),
        quantity: Int? = 5,
        adjustedPrice: Price = item.price,
    ) = ShopInventoryItem(
        item = item,
        quantity = quantity,
        adjustedPrice = adjustedPrice,
    )
}
