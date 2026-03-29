package com.shopforge.data.catalog

import com.shopforge.data.db.ShopForgeDatabase

/**
 * Seeds the built-in item catalog into the database.
 *
 * The seeding is idempotent: if catalog items already exist in the database
 * (detected by counting non-custom items), the seeder skips insertion.
 * This prevents duplicate entries on subsequent app launches.
 */
object CatalogSeeder {

    /**
     * Seeds all items from [ItemCatalog] into the database if the catalog
     * has not been seeded yet.
     *
     * @param database The ShopForge database instance.
     * @return The number of items inserted (0 if already seeded).
     */
    fun seed(database: ShopForgeDatabase): Int {
        val existingCount = database.itemQueries.countCatalogItems().executeAsOne()
        if (existingCount > 0) {
            return 0
        }

        val catalogItems = ItemCatalog.items
        database.transaction {
            catalogItems.forEach { item ->
                database.itemQueries.insert(
                    name = item.name,
                    description = item.description,
                    type = item.category.name,
                    price = item.priceCopper,
                    rarity = item.rarity.name,
                    isCustom = 0L,
                )
            }
        }

        return catalogItems.size
    }
}
