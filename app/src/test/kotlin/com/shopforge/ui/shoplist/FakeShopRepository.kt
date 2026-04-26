package com.shopforge.ui.shoplist

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of [ShopRepository] for unit tests.
 * Only the methods needed for ShopListViewModel are implemented;
 * others throw [UnsupportedOperationException].
 */
class FakeShopRepository(
    initialShops: List<Shop> = emptyList(),
) : ShopRepository {

    private val shopsFlow = MutableStateFlow(initialShops)

    fun emitShops(shops: List<Shop>) {
        shopsFlow.value = shops
    }

    override fun getAllShops(): Flow<List<Shop>> = shopsFlow

    override fun getShopById(id: Long): Flow<Shop?> =
        shopsFlow.map { shops -> shops.find { it.id == id } }

    override suspend fun createShop(shop: Shop): Long {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override suspend fun updateShop(shop: Shop) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override suspend fun deleteShop(id: Long) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }
    override suspend fun updateItemAdjustedPrice(shopId: Long, itemId: Long, adjustedPrice: Price) {
        throw NotImplementedError()
    }

    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }

    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
        throw UnsupportedOperationException("Not needed for ShopListViewModel tests")
    }
}
