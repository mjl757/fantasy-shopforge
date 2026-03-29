package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecrementQuantityUseCaseTest {

    private val updateCalls = mutableListOf<Triple<Long, Long, Int?>>()

    private val repository = object : ShopRepository {
        override fun getAllShops(): Flow<List<Shop>> = throw NotImplementedError()
        override fun getShopById(id: Long): Flow<Shop?> = throw NotImplementedError()
        override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = throw NotImplementedError()
        override suspend fun createShop(shop: Shop): Long = throw NotImplementedError()
        override suspend fun updateShop(shop: Shop) = throw NotImplementedError()
        override suspend fun deleteShop(id: Long) = throw NotImplementedError()
        override suspend fun addItemToShop(shopId: Long, item: Item, quantity: Int?, adjustedPrice: Price) =
            throw NotImplementedError()
        override suspend fun removeItemFromShop(shopId: Long, itemId: Long) = throw NotImplementedError()
        override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
            updateCalls.add(Triple(shopId, itemId, quantity))
        }
        override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) =
            throw NotImplementedError()
    }

    private val useCase = DecrementQuantityUseCase(repository)

    @Test
    fun decrementsFiniteQuantity() = runTest {
        useCase(shopId = 1L, itemId = 10L, currentQuantity = 5)

        assertEquals(1, updateCalls.size)
        assertEquals(Triple(1L, 10L, 4), updateCalls[0])
    }

    @Test
    fun decrementsToZero() = runTest {
        useCase(shopId = 1L, itemId = 10L, currentQuantity = 1)

        assertEquals(1, updateCalls.size)
        assertEquals(Triple(1L, 10L, 0), updateCalls[0])
    }

    @Test
    fun noOpForUnlimitedStock() = runTest {
        useCase(shopId = 1L, itemId = 10L, currentQuantity = null)

        assertTrue(updateCalls.isEmpty())
    }

    @Test
    fun noOpForSoldOut() = runTest {
        useCase(shopId = 1L, itemId = 10L, currentQuantity = 0)

        assertTrue(updateCalls.isEmpty())
    }

    @Test
    fun noOpForNegativeQuantity() = runTest {
        useCase(shopId = 1L, itemId = 10L, currentQuantity = -1)

        assertTrue(updateCalls.isEmpty())
    }
}
