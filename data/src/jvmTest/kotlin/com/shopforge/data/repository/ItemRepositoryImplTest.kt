package com.shopforge.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for [ItemRepositoryImpl] using an in-memory SQLite driver.
 */
class ItemRepositoryImplTest {

    private fun createTestDatabase(): ShopForgeDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ShopForgeDatabase.Schema.create(driver)
        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        return ShopForgeDatabase(driver)
    }

    private fun createRepository(db: ShopForgeDatabase) =
        ItemRepositoryImpl(db, Dispatchers.Unconfined)

    private fun customItem(
        name: String = "Custom Blade",
        category: ItemCategory = ItemCategory.Weapon,
        price: Price = Price.ofGold(100),
        rarity: Rarity = Rarity.Rare,
    ) = Item(
        id = 0,
        name = name,
        description = "A custom creation",
        category = category,
        price = price,
        rarity = rarity,
        isCustom = true,
    )

    // ---- Catalog queries ----

    @Test
    fun `getAllItems emits items reactively`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.getAllItems().test {
            // Initially empty.
            assertEquals(0, awaitItem().size)

            repo.createCustomItem(customItem())

            // Should emit updated list.
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Custom Blade", items.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getItemsByCategory returns only matching items`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.createCustomItem(customItem(name = "Sword", category = ItemCategory.Weapon))
        repo.createCustomItem(customItem(name = "Shield", category = ItemCategory.Armor))

        val weapons = repo.getItemsByCategory(ItemCategory.Weapon)
        assertEquals(1, weapons.size)
        assertEquals("Sword", weapons.first().name)
    }

    @Test
    fun `getItemsByRarity returns only matching items`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.createCustomItem(customItem(name = "Common Sword", rarity = Rarity.Common))
        repo.createCustomItem(customItem(name = "Rare Blade", rarity = Rarity.Rare))

        val rareItems = repo.getItemsByRarity(Rarity.Rare)
        assertEquals(1, rareItems.size)
        assertEquals("Rare Blade", rareItems.first().name)
    }

    @Test
    fun `searchItems returns items matching query case-insensitively`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.createCustomItem(customItem(name = "Flame Sword"))
        repo.createCustomItem(customItem(name = "Ice Shield"))

        val results = repo.searchItems("sword")
        assertEquals(1, results.size)
        assertEquals("Flame Sword", results.first().name)
    }

    @Test
    fun `searchItems returns empty list for no matches`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.createCustomItem(customItem(name = "Flame Sword"))

        val results = repo.searchItems("potion")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `getItemById returns item when it exists`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createCustomItem(customItem())
        val item = repo.getItemById(id)

        assertNotNull(item)
        assertEquals("Custom Blade", item.name)
    }

    @Test
    fun `getItemById returns null for nonexistent id`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        assertNull(repo.getItemById(999L))
    }

    // ---- Custom item CRUD ----

    @Test
    fun `createCustomItem inserts item and returns generated id`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createCustomItem(customItem())
        assertTrue(id > 0)

        val item = repo.getItemById(id)
        assertNotNull(item)
        assertTrue(item.isCustom)
    }

    @Test
    fun `updateCustomItem modifies existing custom item`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createCustomItem(customItem())
        val updated = customItem(name = "Updated Blade").copy(id = id)
        repo.updateCustomItem(updated)

        val item = repo.getItemById(id)
        assertNotNull(item)
        assertEquals("Updated Blade", item.name)
    }

    @Test
    fun `deleteCustomItem removes the item`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        val id = repo.createCustomItem(customItem())
        repo.deleteCustomItem(id)

        assertNull(repo.getItemById(id))
    }

    // ---- Catalog seeding ----

    @Test
    fun `seedCatalogIfEmpty inserts catalog items into empty database`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.seedCatalogIfEmpty()

        repo.getAllItems().test {
            val items = awaitItem()
            assertEquals(CatalogItems.all.size, items.size)
            // Verify items are not marked as custom.
            assertTrue(items.none { it.isCustom })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `seedCatalogIfEmpty is idempotent`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.seedCatalogIfEmpty()
        val countAfterFirst = db.itemQueries.countAll().executeAsOne()

        repo.seedCatalogIfEmpty()
        val countAfterSecond = db.itemQueries.countAll().executeAsOne()

        assertEquals(countAfterFirst, countAfterSecond)
    }

    @Test
    fun `seedCatalogIfEmpty does not overwrite existing items`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        // Add a custom item first.
        repo.createCustomItem(customItem())

        // Seeding should be skipped since items already exist.
        repo.seedCatalogIfEmpty()

        repo.getAllItems().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertTrue(items.first().isCustom)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `seeded catalog contains items from all categories`() = runTest {
        val db = createTestDatabase()
        val repo = createRepository(db)

        repo.seedCatalogIfEmpty()

        // Every ItemCategory should have at least one item in the catalog.
        ItemCategory.entries.forEach { category ->
            val items = repo.getItemsByCategory(category)
            assertTrue(items.isNotEmpty(), "Expected at least one item in category $category")
        }
    }
}
