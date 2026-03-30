package com.shopforge.data

import com.shopforge.data.db.SelectByShopId
import com.shopforge.data.mapper.toDomain
import com.shopforge.data.mapper.toDbString
import com.shopforge.data.mapper.toDbIsCustom
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopType
import com.shopforge.data.db.migrations.Shop as DbShop
import com.shopforge.data.db.migrations.Item as DbItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for data mappers that convert between SQLDelight-generated models
 * and domain entities.
 */
class MapperTest {

    // ---- ShopMapper ----

    @Test
    fun `ShopMapper toDomain maps all fields correctly`() {
        val dbShop = DbShop(
            id = 1L,
            name = "The Gilded Anvil",
            type = "Blacksmith",
            description = "A fine smithy",
            createdAt = 1000L,
            updatedAt = 2000L,
        )

        val shop = dbShop.toDomain()

        assertEquals(1L, shop.id)
        assertEquals("The Gilded Anvil", shop.name)
        assertEquals(ShopType.Blacksmith, shop.type)
        assertEquals("A fine smithy", shop.description)
        assertEquals(1000L, shop.createdAt)
        assertEquals(2000L, shop.updatedAt)
    }

    @Test
    fun `ShopMapper toDomain handles null description`() {
        val dbShop = DbShop(
            id = 1L,
            name = "Shop",
            type = "Tavern",
            description = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val shop = dbShop.toDomain()
        assertNull(shop.description)
    }

    @Test
    fun `ShopType toDbString converts all ShopType values`() {
        ShopType.entries.forEach { shopType ->
            assertEquals(shopType.name, shopType.toDbString())
        }
    }

    // ---- ItemMapper ----

    @Test
    fun `ItemMapper toDomain maps all fields correctly`() {
        val dbItem = DbItem(
            id = 42L,
            name = "Longsword",
            description = "A standard longsword",
            type = "Weapon",
            price = 1500L,
            rarity = "Common",
            isCustom = 0L,
        )

        val item = dbItem.toDomain()

        assertEquals(42L, item.id)
        assertEquals("Longsword", item.name)
        assertEquals("A standard longsword", item.description)
        assertEquals(ItemCategory.Weapon, item.category)
        assertEquals(Price(1500L), item.price)
        assertEquals(Rarity.Common, item.rarity)
        assertFalse(item.isCustom)
    }

    @Test
    fun `ItemMapper toDomain maps custom item correctly`() {
        val dbItem = DbItem(
            id = 1L,
            name = "Custom Blade",
            description = null,
            type = "Weapon",
            price = 5000L,
            rarity = "Rare",
            isCustom = 1L,
        )

        val item = dbItem.toDomain()
        assertTrue(item.isCustom)
        assertNull(item.description)
    }

    @Test
    fun `ItemCategory toDbString converts all ItemCategory values`() {
        ItemCategory.entries.forEach { category ->
            assertEquals(category.name, category.toDbString())
        }
    }

    @Test
    fun `Rarity toDbString converts all Rarity values`() {
        Rarity.entries.forEach { rarity ->
            assertEquals(rarity.name, rarity.toDbString())
        }
    }

    @Test
    fun `Boolean toDbIsCustom converts boolean correctly`() {
        assertEquals(1L, true.toDbIsCustom())
        assertEquals(0L, false.toDbIsCustom())
    }

    // ---- ShopInventoryMapper ----

    @Test
    fun `ShopInventoryMapper toDomain maps joined row correctly`() {
        val row = SelectByShopId(
            shopId = 1L,
            itemId = 42L,
            quantity = 5L,
            adjustedPrice = 1600L,
            id = 42L,
            name = "Longsword",
            description = "A fine blade",
            type = "Weapon",
            price = 1500L,
            rarity = "Common",
            isCustom = 0L,
        )

        val inventoryItem = row.toDomain()

        assertEquals(42L, inventoryItem.item.id)
        assertEquals("Longsword", inventoryItem.item.name)
        assertEquals(ItemCategory.Weapon, inventoryItem.item.category)
        assertEquals(Price(1500L), inventoryItem.item.price)
        assertEquals(Rarity.Common, inventoryItem.item.rarity)
        assertFalse(inventoryItem.item.isCustom)
        assertEquals(5, inventoryItem.quantity)
        assertEquals(Price(1600L), inventoryItem.adjustedPrice)
    }

    @Test
    fun `ShopInventoryMapper toDomain handles null quantity as unlimited stock`() {
        val row = SelectByShopId(
            shopId = 1L,
            itemId = 42L,
            quantity = null,
            adjustedPrice = 1500L,
            id = 42L,
            name = "Rations",
            description = null,
            type = "Food",
            price = 1500L,
            rarity = "Common",
            isCustom = 0L,
        )

        val inventoryItem = row.toDomain()
        assertNull(inventoryItem.quantity)
        assertTrue(inventoryItem.isUnlimitedStock)
        assertFalse(inventoryItem.isSoldOut)
    }

    @Test
    fun `ShopInventoryMapper toDomain handles zero quantity as sold out`() {
        val row = SelectByShopId(
            shopId = 1L,
            itemId = 42L,
            quantity = 0L,
            adjustedPrice = 1500L,
            id = 42L,
            name = "Rare Gem",
            description = null,
            type = "ExoticItem",
            price = 1500L,
            rarity = "Rare",
            isCustom = 0L,
        )

        val inventoryItem = row.toDomain()
        assertEquals(0, inventoryItem.quantity)
        assertTrue(inventoryItem.isSoldOut)
        assertFalse(inventoryItem.isUnlimitedStock)
    }
}
