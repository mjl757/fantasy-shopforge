package com.shopforge.data.catalog

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.data.mapper.ItemMapper
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for the built-in item catalog and database seeding.
 */
class CatalogTest {

    private fun createTestDatabase(): ShopForgeDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ShopForgeDatabase.Schema.create(driver)
        return ShopForgeDatabase(driver)
    }

    // ---- ItemCatalog tests ----

    @Test
    fun `catalog contains between 30 and 40 items`() {
        val size = ItemCatalog.size
        assertTrue(size in 30..40, "Catalog should have 30-40 items, but has $size")
    }

    @Test
    fun `every catalog item has a non-blank name`() {
        ItemCatalog.items.forEach { item ->
            assertTrue(item.name.isNotBlank(), "Item name should not be blank")
        }
    }

    @Test
    fun `every catalog item has a non-blank description`() {
        ItemCatalog.items.forEach { item ->
            assertTrue(item.description.isNotBlank(), "Description for '${item.name}' should not be blank")
        }
    }

    @Test
    fun `every catalog item has a positive price`() {
        ItemCatalog.items.forEach { item ->
            assertTrue(item.priceCopper > 0, "Price for '${item.name}' should be positive, got ${item.priceCopper}")
        }
    }

    @Test
    fun `catalog item names are unique`() {
        val names = ItemCatalog.items.map { it.name }
        assertEquals(names.size, names.toSet().size, "Catalog item names must be unique")
    }

    @Test
    fun `catalog covers all item categories`() {
        val coveredCategories = ItemCatalog.items.map { it.category }.toSet()
        ItemCategory.entries.forEach { category ->
            assertTrue(
                category in coveredCategories,
                "Category $category is not represented in the catalog"
            )
        }
    }

    @Test
    fun `catalog has items across multiple rarities`() {
        val coveredRarities = ItemCatalog.items.map { it.rarity }.toSet()
        // At minimum we should have Common, Uncommon, and Rare
        assertTrue(Rarity.Common in coveredRarities, "Catalog should have Common items")
        assertTrue(Rarity.Uncommon in coveredRarities, "Catalog should have Uncommon items")
        assertTrue(Rarity.Rare in coveredRarities, "Catalog should have Rare items")
    }

    @Test
    fun `byCategory returns correct items`() {
        val weapons = ItemCatalog.byCategory(ItemCategory.Weapon)
        assertTrue(weapons.isNotEmpty(), "Should have weapon items")
        assertTrue(weapons.all { it.category == ItemCategory.Weapon })
    }

    @Test
    fun `byRarity returns correct items`() {
        val common = ItemCatalog.byRarity(Rarity.Common)
        assertTrue(common.isNotEmpty(), "Should have common items")
        assertTrue(common.all { it.rarity == Rarity.Common })
    }

    @Test
    fun `common items are priced within guideline range`() {
        val commonItems = ItemCatalog.byRarity(Rarity.Common)
        commonItems.forEach { item ->
            assertTrue(
                item.priceCopper in 1..5_000,
                "Common item '${item.name}' price ${item.priceCopper} CP is outside range 1-5000 CP"
            )
        }
    }

    @Test
    fun `uncommon items are priced within guideline range`() {
        val uncommonItems = ItemCatalog.byRarity(Rarity.Uncommon)
        uncommonItems.forEach { item ->
            assertTrue(
                item.priceCopper in 5_000..50_000,
                "Uncommon item '${item.name}' price ${item.priceCopper} CP is outside range 5000-50000 CP"
            )
        }
    }

    @Test
    fun `rare items are priced within guideline range`() {
        val rareItems = ItemCatalog.byRarity(Rarity.Rare)
        rareItems.forEach { item ->
            assertTrue(
                item.priceCopper in 50_000..500_000,
                "Rare item '${item.name}' price ${item.priceCopper} CP is outside range 50000-500000 CP"
            )
        }
    }

    @Test
    fun `very rare items are priced within guideline range`() {
        val veryRareItems = ItemCatalog.byRarity(Rarity.VeryRare)
        veryRareItems.forEach { item ->
            assertTrue(
                item.priceCopper in 500_000..5_000_000,
                "Very Rare item '${item.name}' price ${item.priceCopper} CP is outside range 500000-5000000 CP"
            )
        }
    }

    @Test
    fun `legendary items are priced at or above guideline minimum`() {
        val legendaryItems = ItemCatalog.byRarity(Rarity.Legendary)
        legendaryItems.forEach { item ->
            assertTrue(
                item.priceCopper >= 5_000_000,
                "Legendary item '${item.name}' price ${item.priceCopper} CP is below 5000000 CP minimum"
            )
        }
    }

    // ---- CatalogSeeder tests ----

    @Test
    fun `seeder inserts all catalog items into empty database`() {
        val db = createTestDatabase()

        val inserted = CatalogSeeder.seed(db)

        assertEquals(ItemCatalog.size, inserted)
        val allItems = db.itemQueries.selectAll().executeAsList()
        assertEquals(ItemCatalog.size, allItems.size)
    }

    @Test
    fun `seeder is idempotent - second seed inserts zero items`() {
        val db = createTestDatabase()

        val firstInsert = CatalogSeeder.seed(db)
        assertEquals(ItemCatalog.size, firstInsert)

        val secondInsert = CatalogSeeder.seed(db)
        assertEquals(0, secondInsert)

        // Total count should still equal catalog size.
        val allItems = db.itemQueries.selectAll().executeAsList()
        assertEquals(ItemCatalog.size, allItems.size)
    }

    @Test
    fun `seeded items are all marked as non-custom`() {
        val db = createTestDatabase()
        CatalogSeeder.seed(db)

        val allItems = db.itemQueries.selectAll().executeAsList()
        allItems.forEach { dbItem ->
            assertEquals(0L, dbItem.isCustom, "Catalog item '${dbItem.name}' should not be custom")
        }
    }

    @Test
    fun `seeded items can be mapped to domain model`() {
        val db = createTestDatabase()
        CatalogSeeder.seed(db)

        val allDbItems = db.itemQueries.selectAll().executeAsList()
        val domainItems = allDbItems.map { ItemMapper.toDomain(it) }

        assertEquals(ItemCatalog.size, domainItems.size)
        domainItems.forEach { item ->
            assertFalse(item.isCustom)
            assertTrue(item.name.isNotBlank())
            assertTrue(item.price.copperPieces > 0)
        }
    }

    @Test
    fun `seeded items can be queried by category`() {
        val db = createTestDatabase()
        CatalogSeeder.seed(db)

        val weapons = db.itemQueries.selectByCategory("Weapon").executeAsList()
        val catalogWeapons = ItemCatalog.byCategory(ItemCategory.Weapon)
        assertEquals(catalogWeapons.size, weapons.size)
    }

    @Test
    fun `seeded items can be queried by rarity`() {
        val db = createTestDatabase()
        CatalogSeeder.seed(db)

        val common = db.itemQueries.selectByRarity("Common").executeAsList()
        val catalogCommon = ItemCatalog.byRarity(Rarity.Common)
        assertEquals(catalogCommon.size, common.size)
    }

    @Test
    fun `catalog count query returns correct number after seeding`() {
        val db = createTestDatabase()

        assertEquals(0L, db.itemQueries.countCatalogItems().executeAsOne())

        CatalogSeeder.seed(db)

        assertEquals(ItemCatalog.size.toLong(), db.itemQueries.countCatalogItems().executeAsOne())
    }
}
