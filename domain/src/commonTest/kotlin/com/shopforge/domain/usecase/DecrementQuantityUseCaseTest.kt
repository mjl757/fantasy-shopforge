package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class DecrementQuantityUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)
    private val useCase = DecrementQuantityUseCase(repository)

    @Test
    fun `decrements quantity by 1`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 5, item.price)

        useCase(shopId, item.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(4, inventory[0].quantity)
    }

    @Test
    fun `decrements from 1 to 0`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 1, item.price)

        useCase(shopId, item.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory[0].quantity)
    }

    @Test
    fun `quantity 0 is a no-op`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 0, item.price)

        useCase(shopId, item.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory[0].quantity)
    }

    @Test
    fun `unlimited stock (null quantity) is a no-op`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Tavern)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, null, item.price)

        useCase(shopId, item.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertNull(inventory[0].quantity)
    }

    @Test
    fun `nonexistent item is a no-op`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)

        useCase(shopId, 999L) // Should complete without error
    }

    @Test
    fun `multiple decrements work correctly`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 3, item.price)

        useCase(shopId, item.id) // 3 -> 2
        useCase(shopId, item.id) // 2 -> 1
        useCase(shopId, item.id) // 1 -> 0

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory[0].quantity)
    }

    @Test
    fun `decrement at 0 stays at 0 after multiple calls`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 0, item.price)

        useCase(shopId, item.id)
        useCase(shopId, item.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory[0].quantity)
    }
}
