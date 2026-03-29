package com.shopforge.domain.usecase

import com.shopforge.domain.model.Price
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class RegenerateInventoryUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)

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

        val fakeGenerator = object : GenerateInventoryUseCase {
            override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> {
                assertEquals(ShopType.Blacksmith, shopType)
                return generatedItems
            }
        }

        val useCase = RegenerateInventoryUseCase(repository, fakeGenerator)
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

        val fakeGenerator = object : GenerateInventoryUseCase {
            override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> {
                capturedType = shopType
                return emptyList()
            }
        }

        val useCase = RegenerateInventoryUseCase(repository, fakeGenerator)
        useCase(shopId)

        assertEquals(ShopType.MagicShop, capturedType)
    }

    @Test
    fun `throws for nonexistent shop`() = runTest {
        val fakeGenerator = object : GenerateInventoryUseCase {
            override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> =
                emptyList()
        }

        val useCase = RegenerateInventoryUseCase(repository, fakeGenerator)

        assertFailsWith<NoSuchElementException> {
            useCase(999L)
        }
    }

    @Test
    fun `clears inventory when generator returns empty list`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Tavern)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 5, item.price)

        val fakeGenerator = object : GenerateInventoryUseCase {
            override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> =
                emptyList()
        }

        val useCase = RegenerateInventoryUseCase(repository, fakeGenerator)
        useCase(shopId)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory.size)
    }
}
