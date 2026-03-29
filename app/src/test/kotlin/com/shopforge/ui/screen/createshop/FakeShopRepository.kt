package com.shopforge.ui.screen.createshop

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake [ShopRepository] for ViewModel testing.
 */
class FakeShopRepository : ShopRepository {

    val shops = mutableMapOf<Long, Shop>()
    val inventory = mutableMapOf<Long, MutableList<ShopInventoryItem>>()
    private var nextId = 1L

    private val shopsFlow = MutableStateFlow<List<Shop>>(emptyList())

    /**
     * Pre-populates the repository with the given shop for testing.
     * Updates both the map and the flow.
     */
    fun addShop(shop: Shop) {
        shops[shop.id] = shop
        if (shop.id >= nextId) nextId = shop.id + 1
        shopsFlow.value = shops.values.toList()
    }

    override fun getAllShops(): Flow<List<Shop>> = shopsFlow

    override fun getShopById(id: Long): Flow<Shop?> = shopsFlow.map { list ->
        list.find { it.id == id }
    }

    override suspend fun createShop(shop: Shop): Long {
        val id = nextId++
        val saved = shop.copy(id = id)
        shops[id] = saved
        shopsFlow.value = shops.values.toList()
        return id
    }

    override suspend fun updateShop(shop: Shop) {
        shops[shop.id] = shop
        shopsFlow.value = shops.values.toList()
    }

    override suspend fun deleteShop(id: Long) {
        shops.remove(id)
        inventory.remove(id)
        shopsFlow.value = shops.values.toList()
    }

    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> {
        return MutableStateFlow(inventory[shopId] ?: emptyList())
    }

    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {
        val list = inventory.getOrPut(shopId) { mutableListOf() }
        list.add(ShopInventoryItem(item, quantity, adjustedPrice))
    }

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {
        inventory[shopId]?.removeAll { it.item.id == itemId }
    }

    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
        inventory[shopId]?.replaceAll { invItem ->
            if (invItem.item.id == itemId) invItem.copy(quantity = quantity) else invItem
        }
    }

    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
        inventory[shopId] = items.toMutableList()
    }
}
