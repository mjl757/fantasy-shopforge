package com.shopforge.domain.model

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ShopTypeTest {

    @Test
    fun `all 8 shop types are defined`() {
        val types = ShopType.entries
        assertEquals(8, types.size)
    }

    @Test
    fun `Blacksmith default categories include Weapon and Armor`() {
        val categories = ShopType.Blacksmith.defaultCategories
        assertContains(categories, ItemCategory.Weapon)
        assertContains(categories, ItemCategory.Armor)
    }

    @Test
    fun `MagicShop default categories include MagicItem and Potion`() {
        val categories = ShopType.MagicShop.defaultCategories
        assertContains(categories, ItemCategory.MagicItem)
        assertContains(categories, ItemCategory.Potion)
    }

    @Test
    fun `GeneralStore default categories include AdventuringGear and Food`() {
        val categories = ShopType.GeneralStore.defaultCategories
        assertContains(categories, ItemCategory.AdventuringGear)
        assertContains(categories, ItemCategory.Food)
    }

    @Test
    fun `Alchemist default categories include Potion and AlchemicalSupply`() {
        val categories = ShopType.Alchemist.defaultCategories
        assertContains(categories, ItemCategory.Potion)
        assertContains(categories, ItemCategory.AlchemicalSupply)
    }

    @Test
    fun `Fletcher default categories include Weapon and Ammunition`() {
        val categories = ShopType.Fletcher.defaultCategories
        assertContains(categories, ItemCategory.Weapon)
        assertContains(categories, ItemCategory.Ammunition)
    }

    @Test
    fun `Tavern default categories include Food`() {
        val categories = ShopType.Tavern.defaultCategories
        assertContains(categories, ItemCategory.Food)
    }

    @Test
    fun `Temple default categories include HolyItem and Potion`() {
        val categories = ShopType.Temple.defaultCategories
        assertContains(categories, ItemCategory.HolyItem)
        assertContains(categories, ItemCategory.Potion)
    }

    @Test
    fun `ExoticGoods default categories include ExoticItem and MagicItem`() {
        val categories = ShopType.ExoticGoods.defaultCategories
        assertContains(categories, ItemCategory.ExoticItem)
        assertContains(categories, ItemCategory.MagicItem)
    }

    @Test
    fun `each shop type has at least one default category`() {
        ShopType.entries.forEach { shopType ->
            assertFalse(
                shopType.defaultCategories.isEmpty(),
                "${shopType.name} must have at least one default category",
            )
        }
    }

    @Test
    fun `no shop type has duplicate default categories`() {
        ShopType.entries.forEach { shopType ->
            val categories = shopType.defaultCategories
            val unique = categories.toSet()
            assertEquals(categories.size, unique.size, "${shopType.name} has duplicate default categories")
        }
    }
}
