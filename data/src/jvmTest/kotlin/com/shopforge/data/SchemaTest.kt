package com.shopforge.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.shopforge.data.db.ShopForgeDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the SQLDelight schema, CRUD queries, and foreign key cascade behavior.
 * Uses an in-memory SQLite driver.
 */
class SchemaTest {

    private fun createTestDatabase(): ShopForgeDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ShopForgeDatabase.Schema.create(driver)
        // Enable foreign keys for cascade delete support.
        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        return ShopForgeDatabase(driver)
    }

    // ---- Shop CRUD ----

    @Test
    fun `insert and select shop by id`() {
        val db = createTestDatabase()
        db.shopQueries.insert(
            name = "The Gilded Anvil",
            type = "Blacksmith",
            description = "A fine smithy",
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val shops = db.shopQueries.selectAll().executeAsList()
        assertEquals(1, shops.size)

        val shop = shops.first()
        assertEquals("The Gilded Anvil", shop.name)
        assertEquals("Blacksmith", shop.type)
        assertEquals("A fine smithy", shop.description)
        assertEquals(1000L, shop.createdAt)

        val byId = db.shopQueries.selectById(shop.id).executeAsOneOrNull()
        assertNotNull(byId)
        assertEquals(shop, byId)
    }

    @Test
    fun `update shop`() {
        val db = createTestDatabase()
        db.shopQueries.insert(
            name = "Old Name",
            type = "Blacksmith",
            description = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val id = db.shopQueries.selectAll().executeAsList().first().id

        db.shopQueries.update(
            name = "New Name",
            type = "MagicShop",
            description = "Updated desc",
            updatedAt = 2000L,
            id = id,
        )

        val updated = db.shopQueries.selectById(id).executeAsOne()
        assertEquals("New Name", updated.name)
        assertEquals("MagicShop", updated.type)
        assertEquals("Updated desc", updated.description)
        assertEquals(2000L, updated.updatedAt)
        assertEquals(1000L, updated.createdAt) // createdAt should not change
    }

    @Test
    fun `delete shop`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Shop A", "Blacksmith", null, 1000L, 1000L)
        db.shopQueries.insert("Shop B", "Tavern", null, 2000L, 2000L)

        val shops = db.shopQueries.selectAll().executeAsList()
        assertEquals(2, shops.size)

        db.shopQueries.delete(shops.first().id)
        assertEquals(1, db.shopQueries.selectAll().executeAsList().size)
    }

    @Test
    fun `selectAll returns shops ordered by updatedAt desc`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Old Shop", "Blacksmith", null, 1000L, 1000L)
        db.shopQueries.insert("New Shop", "Tavern", null, 2000L, 3000L)
        db.shopQueries.insert("Mid Shop", "Temple", null, 1500L, 2000L)

        val shops = db.shopQueries.selectAll().executeAsList()
        assertEquals("New Shop", shops[0].name)
        assertEquals("Mid Shop", shops[1].name)
        assertEquals("Old Shop", shops[2].name)
    }

    // ---- Item CRUD ----

    @Test
    fun `insert and select item`() {
        val db = createTestDatabase()
        db.itemQueries.insert(
            name = "Longsword",
            description = "A standard longsword",
            type = "Weapon",
            price = 1500L,
            rarity = "Common",
            isCustom = 0L,
        )

        val items = db.itemQueries.selectAll().executeAsList()
        assertEquals(1, items.size)

        val item = items.first()
        assertEquals("Longsword", item.name)
        assertEquals("A standard longsword", item.description)
        assertEquals("Weapon", item.type)
        assertEquals(1500L, item.price)
        assertEquals("Common", item.rarity)
        assertEquals(0L, item.isCustom)
    }

    @Test
    fun `select items by category`() {
        val db = createTestDatabase()
        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        db.itemQueries.insert("Chain Mail", null, "Armor", 7500L, "Common", 0L)
        db.itemQueries.insert("Dagger", null, "Weapon", 200L, "Common", 0L)

        val weapons = db.itemQueries.selectByCategory("Weapon").executeAsList()
        assertEquals(2, weapons.size)
        assertTrue(weapons.all { it.type == "Weapon" })
    }

    @Test
    fun `select items by rarity`() {
        val db = createTestDatabase()
        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        db.itemQueries.insert("Flame Tongue", null, "Weapon", 500000L, "Rare", 0L)

        val rare = db.itemQueries.selectByRarity("Rare").executeAsList()
        assertEquals(1, rare.size)
        assertEquals("Flame Tongue", rare.first().name)
    }

    @Test
    fun `search items by name`() {
        val db = createTestDatabase()
        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        db.itemQueries.insert("Shortsword", null, "Weapon", 1000L, "Common", 0L)
        db.itemQueries.insert("Chain Mail", null, "Armor", 7500L, "Common", 0L)

        val results = db.itemQueries.searchByName("sword").executeAsList()
        assertEquals(2, results.size)
        assertTrue(results.all { it.name.contains("sword", ignoreCase = true) })
    }

    @Test
    fun `select item by id returns null for nonexistent`() {
        val db = createTestDatabase()
        val item = db.itemQueries.selectById(999L).executeAsOneOrNull()
        assertNull(item)
    }

    // ---- ShopInventory ----

    @Test
    fun `insert and select inventory items`() {
        val db = createTestDatabase()
        // Create a shop and items.
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        db.itemQueries.insert("Dagger", null, "Weapon", 200L, "Common", 0L)
        val items = db.itemQueries.selectAll().executeAsList()
        val swordId = items.first { it.name == "Longsword" }.id
        val daggerId = items.first { it.name == "Dagger" }.id

        db.shopInventoryQueries.insertItem(shopId, swordId, quantity = 5L, adjustedPrice = 1600L)
        db.shopInventoryQueries.insertItem(shopId, daggerId, quantity = null, adjustedPrice = 200L)

        val inventory = db.shopInventoryQueries.selectByShopId(shopId).executeAsList()
        assertEquals(2, inventory.size)

        val sword = inventory.first { it.name == "Dagger" }
        assertNull(sword.quantity) // unlimited stock

        val dagger = inventory.first { it.name == "Longsword" }
        assertEquals(5L, dagger.quantity)
        assertEquals(1600L, dagger.adjustedPrice)
    }

    @Test
    fun `update inventory quantity`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        val itemId = db.itemQueries.selectAll().executeAsList().first().id

        db.shopInventoryQueries.insertItem(shopId, itemId, quantity = 5L, adjustedPrice = 1500L)

        db.shopInventoryQueries.updateQuantity(quantity = 3L, shopId = shopId, itemId = itemId)

        val inventory = db.shopInventoryQueries.selectByShopId(shopId).executeAsList()
        assertEquals(1, inventory.size)
        assertEquals(3L, inventory.first().quantity)
    }

    @Test
    fun `remove item from inventory`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        val itemId = db.itemQueries.selectAll().executeAsList().first().id

        db.shopInventoryQueries.insertItem(shopId, itemId, quantity = 5L, adjustedPrice = 1500L)
        assertEquals(1, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)

        db.shopInventoryQueries.removeItem(shopId, itemId)
        assertEquals(0, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)
    }

    @Test
    fun `delete all inventory for a shop`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        db.itemQueries.insert("Dagger", null, "Weapon", 200L, "Common", 0L)
        val items = db.itemQueries.selectAll().executeAsList()

        items.forEach { item ->
            db.shopInventoryQueries.insertItem(shopId, item.id, quantity = 1L, adjustedPrice = item.price)
        }
        assertEquals(2, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)

        db.shopInventoryQueries.deleteByShopId(shopId)
        assertEquals(0, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)
    }

    @Test
    fun `cascade delete removes inventory when shop is deleted`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        val itemId = db.itemQueries.selectAll().executeAsList().first().id

        db.shopInventoryQueries.insertItem(shopId, itemId, quantity = 5L, adjustedPrice = 1500L)
        assertEquals(1, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)

        // Deleting the shop should cascade-delete its inventory entries.
        db.shopQueries.delete(shopId)
        assertEquals(0, db.shopInventoryQueries.selectByShopId(shopId).executeAsList().size)
    }

    @Test
    fun `insert or replace updates existing inventory entry`() {
        val db = createTestDatabase()
        db.shopQueries.insert("Test Shop", "Blacksmith", null, 1000L, 1000L)
        val shopId = db.shopQueries.selectAll().executeAsList().first().id

        db.itemQueries.insert("Longsword", null, "Weapon", 1500L, "Common", 0L)
        val itemId = db.itemQueries.selectAll().executeAsList().first().id

        db.shopInventoryQueries.insertItem(shopId, itemId, quantity = 5L, adjustedPrice = 1500L)
        db.shopInventoryQueries.insertItem(shopId, itemId, quantity = 10L, adjustedPrice = 1600L)

        val inventory = db.shopInventoryQueries.selectByShopId(shopId).executeAsList()
        assertEquals(1, inventory.size)
        assertEquals(10L, inventory.first().quantity)
        assertEquals(1600L, inventory.first().adjustedPrice)
    }
}
