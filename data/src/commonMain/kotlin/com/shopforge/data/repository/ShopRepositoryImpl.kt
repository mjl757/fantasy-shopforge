package com.shopforge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.shopforge.data.db.ShopForgeDatabase
import com.shopforge.data.mapper.toDbString
import com.shopforge.data.mapper.toDomain
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

/**
 * SQLDelight-backed implementation of [ShopRepository].
 *
 * @param database The SQLDelight-generated database instance.
 * @param context The coroutine context used for Flow emissions (defaults to [Dispatchers.Default]).
 */
class ShopRepositoryImpl(
    private val database: ShopForgeDatabase,
    private val context: CoroutineContext = Dispatchers.Default,
) : ShopRepository {

    private val shopQueries get() = database.shopQueries
    private val inventoryQueries get() = database.shopInventoryQueries

    // ---- Shop CRUD ----

    override fun getAllShops(): Flow<List<Shop>> =
        shopQueries.selectAll()
            .asFlow()
            .mapToList(context)
            .map { list -> list.map { it.toDomain() } }

    override fun getShopById(id: Long): Flow<Shop?> =
        shopQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(context)
            .map { dbShop -> dbShop?.toDomain() }

    override suspend fun createShop(shop: Shop): Long {
        shopQueries.insert(
            name = shop.name,
            type = shop.type.toDbString(),
            description = shop.description,
            createdAt = shop.createdAt,
            updatedAt = shop.updatedAt,
        )
        return shopQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateShop(shop: Shop) {
        shopQueries.update(
            name = shop.name,
            type = shop.type.toDbString(),
            description = shop.description,
            updatedAt = shop.updatedAt,
            id = shop.id,
        )
    }

    override suspend fun deleteShop(id: Long) {
        database.transaction {
            // Foreign key cascade handles inventory deletion,
            // but we explicitly enable PRAGMA foreign_keys at driver level.
            shopQueries.delete(id)
        }
    }

    // ---- Inventory management ----

    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> =
        inventoryQueries.selectByShopId(shopId)
            .asFlow()
            .mapToList(context)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {
        inventoryQueries.insertItem(
            shopId = shopId,
            itemId = item.id,
            quantity = quantity?.toLong(),
            adjustedPrice = adjustedPrice.copperPieces,
        )
    }

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {
        inventoryQueries.removeItem(shopId = shopId, itemId = itemId)
    }

    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
        inventoryQueries.updateQuantity(
            quantity = quantity?.toLong(),
            shopId = shopId,
            itemId = itemId,
        )
    }

    override suspend fun updateItemAdjustedPrice(shopId: Long, itemId: Long, adjustedPrice: Price) {
        inventoryQueries.updateAdjustedPrice(
            adjustedPrice = adjustedPrice.copperPieces,
            shopId = shopId,
            itemId = itemId,
        )
    }

    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
        database.transaction {
            inventoryQueries.deleteByShopId(shopId)
            items.forEach { inventoryItem ->
                inventoryQueries.insertItem(
                    shopId = shopId,
                    itemId = inventoryItem.item.id,
                    quantity = inventoryItem.quantity?.toLong(),
                    adjustedPrice = inventoryItem.adjustedPrice.copperPieces,
                )
            }
        }
    }
}
