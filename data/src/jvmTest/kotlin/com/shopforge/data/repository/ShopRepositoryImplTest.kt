package com.shopforge.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for [ShopRepositoryImpl] using an in-memory SQLite driver.
 */
class ShopRepositoryImplTest {

    private fun createTestDatabase(): ShopForgeDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ShopForgeDatabase.Schema.create(driver)
        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        return ShopForgeDatabase(driver)
    }

    private fun createRepository(db: ShopForgeDatabase) =
        ShopRepositoryImpl(db, Dispatchers.Unconfined)

    private fun testShop(
        name: String = "The Gilded Anvil",
        type: ShopType = ShopType.Blacksmith,
        description: String? = "A fine smithy",
    ) = Shop(
        id = 0,
        name = name,
        type = type,
        description = description,
        createdAt = 1000L,
        updatedAt = 1000L,
    )

    private fun insertTestItem(db: ShopForgeDatabase, name: String = "Longsword", price: Long = 1500L): Long {
        db.itemQueries.insert(name, "A blade", "Weapon", price, "Common", 0L)
        return db.itemQueries.selectAll().executeAsList().first { it.name == name }.id
    }

    // ---- Shop CRUD ----

    @Test
    fun `createShop inserts shop and returns generated id`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createShop(testShop())
        assertTrue(id > 0)
    }

    @Test
    fun `getAllShops emits list of all shops`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.createShop(testShop(name = "Shop A"))
        repo.createShop(testShop(name = "Shop B"))

        repo.getAllShops().test {
            val shops = awaitItem()
            assertEquals(2, shops.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllShops emits reactively when shop is added`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.getAllShops().test {
            // Initial emission: empty list.
            assertEquals(0, awaitItem().size)

            repo.createShop(testShop(name = "New Shop"))

            // Second emission after insert.
            val shops = awaitItem()
            assertEquals(1, shops.size)
            assertEquals("New Shop", shops.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getShopById returns shop when it exists`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createShop(testShop())

        repo.getShopById(id).test {
            val shop = awaitItem()
            assertNotNull(shop)
            assertEquals("The Gilded Anvil", shop.name)
            assertEquals(ShopType.Blacksmith, shop.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getShopById emits null for nonexistent id`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.getShopById(999L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateShop modifies existing shop`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createShop(testShop())

        repo.updateShop(
            Shop(
                id = id,
                name = "Updated Name",
                type = ShopType.MagicShop,
                description = "Updated desc",
                createdAt = 1000L,
                updatedAt = 2000L,
            )
        )

        repo.getShopById(id).test {
            val shop = awaitItem()
            assertNotNull(shop)
            assertEquals("Updated Name", shop.name)
            assertEquals(ShopType.MagicShop, shop.type)
            assertEquals("Updated desc", shop.description)
            assertEquals(2000L, shop.updatedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteShop removes shop`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createShop(testShop())
        repo.deleteShop(id)

        repo.getShopById(id).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- Inventory management ----

    @Test
    fun `addItemToShop and getInventory returns inventory items`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val itemId = insertTestItem(db)

        val item = Item(
            id = itemId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, item, quantity = 5, adjustedPrice = Price.ofGold(16))

        repo.getInventory(shopId).test {
            val inventory = awaitItem()
            assertEquals(1, inventory.size)
            assertEquals("Longsword", inventory.first().item.name)
            assertEquals(5, inventory.first().quantity)
            assertEquals(Price.ofGold(16), inventory.first().adjustedPrice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addItemToShop with null quantity represents unlimited stock`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val itemId = insertTestItem(db)

        val item = Item(
            id = itemId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, item, quantity = null, adjustedPrice = Price.ofGold(15))

        repo.getInventory(shopId).test {
            val inventory = awaitItem()
            assertTrue(inventory.first().isUnlimitedStock)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeItemFromShop removes item from inventory`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val itemId = insertTestItem(db)

        val item = Item(
            id = itemId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, item, quantity = 5, adjustedPrice = Price.ofGold(15))
        repo.removeItemFromShop(shopId, itemId)

        repo.getInventory(shopId).test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateItemQuantity changes quantity`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val itemId = insertTestItem(db)

        val item = Item(
            id = itemId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, item, quantity = 5, adjustedPrice = Price.ofGold(15))
        repo.updateItemQuantity(shopId, itemId, quantity = 3)

        repo.getInventory(shopId).test {
            val inventory = awaitItem()
            assertEquals(3, inventory.first().quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceInventory clears and replaces all items`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val swordId = insertTestItem(db, "Longsword", 1500L)
        val daggerId = insertTestItem(db, "Dagger", 200L)

        val sword = Item(
            id = swordId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, sword, quantity = 5, adjustedPrice = Price.ofGold(15))

        val dagger = Item(
            id = daggerId, name = "Dagger", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(2),
            rarity = Rarity.Common, isCustom = false,
        )
        val newInventory = listOf(
            ShopInventoryItem(item = dagger, quantity = 10, adjustedPrice = Price.ofGold(2)),
        )
        repo.replaceInventory(shopId, newInventory)

        repo.getInventory(shopId).test {
            val inventory = awaitItem()
            assertEquals(1, inventory.size)
            assertEquals("Dagger", inventory.first().item.name)
            assertEquals(10, inventory.first().quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cascade delete removes inventory when shop is deleted`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val shopId = repo.createShop(testShop())
        val itemId = insertTestItem(db)

        val item = Item(
            id = itemId, name = "Longsword", description = "A blade",
            category = ItemCategory.Weapon, price = Price.ofGold(15),
            rarity = Rarity.Common, isCustom = false,
        )
        repo.addItemToShop(shopId, item, quantity = 5, adjustedPrice = Price.ofGold(15))

        // Verify inventory exists before delete.
        repo.getInventory(shopId).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        repo.deleteShop(shopId)

        // Inventory should be empty after cascade delete.
        repo.getInventory(shopId).test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
