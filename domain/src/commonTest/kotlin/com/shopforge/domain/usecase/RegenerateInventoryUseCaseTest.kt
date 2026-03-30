package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class RegenerateInventoryUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)

    private val emptyItemRepository = object : ItemRepository {
        override fun getAllItems() = flowOf(emptyList<com.shopforge.domain.model.Item>())
        override suspend fun getItemsByCategory(category: com.shopforge.domain.model.ItemCategory) = emptyList<com.shopforge.domain.model.Item>()
        override suspend fun getItemsByRarity(rarity: com.shopforge.domain.model.Rarity) = emptyList<com.shopforge.domain.model.Item>()
        override suspend fun searchItems(query: String) = emptyList<com.shopforge.domain.model.Item>()
        override suspend fun getItemById(id: Long) = null
        override suspend fun createCustomItem(item: com.shopforge.domain.model.Item) = 0L
        override suspend fun updateCustomItem(item: com.shopforge.domain.model.Item) {}
        override suspend fun deleteCustomItem(id: Long) {}
    }

    private fun fakeGenerator(
        onInvoke: suspend (ShopType) -> List<ShopInventoryItem>,
    ): GenerateInventoryUseCase = object : GenerateInventoryUseCase(emptyItemRepository) {
        override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> =
            onInvoke(shopType)
    }

    @Test
    fun `replaces existing inventory with generated items`() = runTest {
        val shopId = createUseCase("Smithy", ShopType.Blacksmith)
        val oldItem = TestFixtures.sampleItem(id = 1L, name = "Old Sword")
        addItemUseCase(shopId, oldItem, 5, oldItem.price)

        val generatedItems = listOf(
            TestFixtures.sampleInventoryItem(
                item = TestFixtures.sampleItem(id = 10L, name = "New Sword"),
                quantity = 3,
            ),
            TestFixtures.sampleInventoryItem(
                item = TestFixtures.sampleItem(id = 11L, name = "New Shield"),
                quantity = 2,
            ),
        )

        val generator = fakeGenerator { shopType ->
            assertEquals(ShopType.Blacksmith, shopType)
            generatedItems
        }

        val useCase = RegenerateInventoryUseCase(repository, generator)
        useCase(shopId)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(2, inventory.size)
        assertEquals("New Sword", inventory[0].item.name)
        assertEquals("New Shield", inventory[1].item.name)
    }

    @Test
    fun `uses shop type for generation`() = runTest {
        val shopId = createUseCase("Magic Place", ShopType.MagicShop)
        var capturedType: ShopType? = null

        val generator = fakeGenerator { shopType ->
            capturedType = shopType
            emptyList()
        }

        val useCase = RegenerateInventoryUseCase(repository, generator)
        useCase(shopId)

        assertEquals(ShopType.MagicShop, capturedType)
    }

    @Test
    fun `throws for nonexistent shop`() = runTest {
        val generator = fakeGenerator { emptyList() }

        val useCase = RegenerateInventoryUseCase(repository, generator)

        assertFailsWith<NoSuchElementException> {
            useCase(999L)
        }
    }

    @Test
    fun `clears inventory when generator returns empty list`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Tavern)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 5, item.price)

        val generator = fakeGenerator { emptyList() }

        val useCase = RegenerateInventoryUseCase(repository, generator)
        useCase(shopId)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory.size)
    }
}
