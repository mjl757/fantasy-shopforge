package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetShopWithInventoryUseCaseTest {

    private val shopFlow = MutableStateFlow<Shop?>(null)
    private val inventoryFlow = MutableStateFlow<List<ShopInventoryItem>>(emptyList())

    private val repository = object : ShopRepository {
        override fun getAllShops(): Flow<List<Shop>> = throw NotImplementedError()
        override fun getShopById(id: Long): Flow<Shop?> = shopFlow
        override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = inventoryFlow
        override suspend fun createShop(shop: Shop): Long = throw NotImplementedError()
        override suspend fun updateShop(shop: Shop) = throw NotImplementedError()
        override suspend fun deleteShop(id: Long) = throw NotImplementedError()
        override suspend fun addItemToShop(shopId: Long, item: Item, quantity: Int?, adjustedPrice: Price) =
            throw NotImplementedError()
        override suspend fun removeItemFromShop(shopId: Long, itemId: Long) = throw NotImplementedError()
        override suspend fun updateItemAdjustedPrice(shopId: Long, itemId: Long, adjustedPrice: Price) =
            throw NotImplementedError()
        override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) =
            throw NotImplementedError()
        override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) =
            throw NotImplementedError()
    }

    private val useCase = GetShopWithInventoryUseCase(repository)

    private val testShop = Shop(
        id = 1L,
        name = "Test Shop",
        type = ShopType.Blacksmith,
        description = null,
        createdAt = 1000L,
        updatedAt = 1000L,
    )

    private val testItem = ShopInventoryItem(
        item = Item(
            id = 10L,
            name = "Longsword",
            category = ItemCategory.Weapon,
            price = Price.ofGold(15),
            rarity = Rarity.Common,
            isCustom = false,
        ),
        quantity = 5,
        adjustedPrice = Price.ofGold(16),
    )

    @Test
    fun combinesShopAndInventory() = runTest {
        shopFlow.value = testShop
        inventoryFlow.value = listOf(testItem)

        val result = useCase(1L).first()

        assertNotNull(result)
        assertEquals(testShop, result.shop)
        assertEquals(1, result.inventory.size)
        assertEquals("Longsword", result.inventory[0].item.name)
    }

    @Test
    fun emitsNullWhenShopDoesNotExist() = runTest {
        shopFlow.value = null
        inventoryFlow.value = listOf(testItem)

        val result = useCase(1L).first()
        assertNull(result)
    }

    @Test
    fun emitsEmptyInventoryForNewShop() = runTest {
        shopFlow.value = testShop
        inventoryFlow.value = emptyList()

        val result = useCase(1L).first()

        assertNotNull(result)
        assertEquals(testShop, result.shop)
        assertEquals(0, result.inventory.size)
    }
}
