package com.shopforge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.data.mapper.toDbIsCustom
import com.shopforge.data.mapper.toDbString
import com.shopforge.data.mapper.toDomain
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

/**
 * SQLDelight-backed implementation of [ItemRepository].
 *
 * @param database The SQLDelight-generated database instance.
 * @param context The coroutine context used for Flow emissions (defaults to [Dispatchers.Default]).
 */
class ItemRepositoryImpl(
    private val database: ShopForgeDatabase,
    private val context: CoroutineContext = Dispatchers.Default,
) : ItemRepository {

    private val itemQueries get() = database.itemQueries

    // ---- Catalog queries ----

    override fun getAllItems(): Flow<List<Item>> =
        itemQueries.selectAll()
            .asFlow()
            .mapToList(context)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
        itemQueries.selectByCategory(category.toDbString())
            .executeAsList()
            .map { it.toDomain() }

    override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
        itemQueries.selectByRarity(rarity.toDbString())
            .executeAsList()
            .map { it.toDomain() }

    override suspend fun searchItems(query: String): List<Item> =
        itemQueries.searchByName(query, query)
            .executeAsList()
            .map { it.toDomain() }

    override suspend fun getItemById(id: Long): Item? =
        itemQueries.selectById(id)
            .executeAsOneOrNull()
            ?.toDomain()

    // ---- Custom item CRUD ----

    override suspend fun createCustomItem(item: Item): Long {
        require(item.isCustom) { "Only custom items can be created via createCustomItem" }
        itemQueries.insert(
            name = item.name,
            description = item.description,
            type = item.category.toDbString(),
            price = item.price.copperPieces,
            rarity = item.rarity.toDbString(),
            isCustom = true.toDbIsCustom(),
        )
        return itemQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateCustomItem(item: Item) {
        require(item.isCustom) { "Only custom items can be updated via updateCustomItem" }
        itemQueries.update(
            name = item.name,
            description = item.description,
            type = item.category.toDbString(),
            price = item.price.copperPieces,
            rarity = item.rarity.toDbString(),
            id = item.id,
        )
    }

    override suspend fun deleteCustomItem(id: Long) {
        itemQueries.deleteCustom(id)
    }

    // ---- Catalog seeding ----

    /**
     * Seeds the catalog with built-in items if the database is empty.
     * This operation is idempotent — if items already exist, nothing happens.
     */
    fun seedCatalogIfEmpty() {
        database.transaction {
            val count = itemQueries.countAll().executeAsOne()
            if (count > 0L) return@transaction

            CatalogItems.all.forEach { item ->
                itemQueries.insert(
                    name = item.name,
                    description = item.description,
                    type = item.category.toDbString(),
                    price = item.price.copperPieces,
                    rarity = item.rarity.toDbString(),
                    isCustom = false.toDbIsCustom(),
                )
            }
        }
    }
}
