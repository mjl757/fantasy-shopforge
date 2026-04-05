package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory fake of [ShopRepository] for unit testing use cases.
 */
class FakeShopRepository : ShopRepository {

    private var nextShopId = 1L
    private val shops = MutableStateFlow<Map<Long, Shop>>(emptyMap())
    private val inventories = MutableStateFlow<Map<Long, List<ShopInventoryItem>>>(emptyMap())

    override fun getAllShops(): Flow<List<Shop>> =
        shops.map { it.values.toList() }

    override fun getShopById(id: Long): Flow<Shop?> =
        shops.map { it[id] }

    override suspend fun createShop(shop: Shop): Long {
        val id = nextShopId++
        val saved = shop.copy(id = id)
        shops.update { it + (id to saved) }
        return id
    }

    override suspend fun updateShop(shop: Shop) {
        shops.update { current ->
            if (shop.id !in current) throw NoSuchElementException("Shop ${shop.id} not found")
            current + (shop.id to shop)
        }
    }

    override suspend fun deleteShop(id: Long) {
        shops.update { it - id }
        inventories.update { it - id }
    }

    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> =
        inventories.map { it[shopId] ?: emptyList() }

    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {
        inventories.update { current ->
            val existing = current[shopId] ?: emptyList()
            val existingEntry = existing.find { it.item.id == item.id }
            val updated = if (existingEntry != null) {
                val newQuantity = when {
                    existingEntry.quantity == null || quantity == null -> null
                    else -> existingEntry.quantity + quantity
                }
                existing.map {
                    if (it.item.id == item.id) it.copy(quantity = newQuantity) else it
                }
            } else {
                existing + ShopInventoryItem(item, quantity, adjustedPrice)
            }
            current + (shopId to updated)
        }
    }

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {
        inventories.update { current ->
            val existing = current[shopId] ?: return@update current
            current + (shopId to existing.filter { it.item.id != itemId })
        }
    }

    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
        inventories.update { current ->
            val existing = current[shopId] ?: return@update current
            current + (shopId to existing.map {
                if (it.item.id == itemId) it.copy(quantity = quantity) else it
            })
        }
    }

    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
        shops.value.also { current ->
            if (shopId !in current) throw NoSuchElementException("Shop $shopId not found")
        }
        inventories.update { current ->
            current + (shopId to items)
        }
    }

    // ---- Test helpers ----

    fun getShopSnapshot(id: Long): Shop? = shops.value[id]

    fun getInventorySnapshot(shopId: Long): List<ShopInventoryItem> =
        inventories.value[shopId] ?: emptyList()
}
