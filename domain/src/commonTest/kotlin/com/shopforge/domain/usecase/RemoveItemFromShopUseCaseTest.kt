package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RemoveItemFromShopUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)
    private val useCase = RemoveItemFromShopUseCase(repository)

    @Test
    fun `removes item from shop inventory`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 5, item.price)

        useCase(shopId, item.id)

        assertTrue(repository.getInventorySnapshot(shopId).isEmpty())
    }

    @Test
    fun `only removes specified item, keeps others`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item1 = TestFixtures.sampleItem(id = 1L, name = "Sword")
        val item2 = TestFixtures.sampleItem(id = 2L, name = "Shield")
        addItemUseCase(shopId, item1, 3, item1.price)
        addItemUseCase(shopId, item2, 5, item2.price)

        useCase(shopId, item1.id)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(1, inventory.size)
        assertEquals("Shield", inventory[0].item.name)
    }

    @Test
    fun `removing nonexistent item does not throw`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        useCase(shopId, 999L) // Should complete without error
    }
}
