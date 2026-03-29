package com.shopforge.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShopInventoryItemTest {

    private val sampleItem = Item(
        id = 1L,
        name = "Longsword",
        category = ItemCategory.Weapon,
        price = Price.ofGold(15),
        rarity = Rarity.Common,
        isCustom = false,
    )

    @Test
    fun `null quantity means unlimited stock`() {
        val inventoryItem = ShopInventoryItem(
            item = sampleItem,
            quantity = null,
            adjustedPrice = sampleItem.price,
        )
        assertTrue(inventoryItem.isUnlimitedStock)
        assertFalse(inventoryItem.isSoldOut)
    }

    @Test
    fun `zero quantity means sold out`() {
        val inventoryItem = ShopInventoryItem(
            item = sampleItem,
            quantity = 0,
            adjustedPrice = sampleItem.price,
        )
        assertTrue(inventoryItem.isSoldOut)
        assertFalse(inventoryItem.isUnlimitedStock)
    }

    @Test
    fun `positive quantity is neither sold out nor unlimited`() {
        val inventoryItem = ShopInventoryItem(
            item = sampleItem,
            quantity = 5,
            adjustedPrice = sampleItem.price,
        )
        assertFalse(inventoryItem.isSoldOut)
        assertFalse(inventoryItem.isUnlimitedStock)
    }
}
