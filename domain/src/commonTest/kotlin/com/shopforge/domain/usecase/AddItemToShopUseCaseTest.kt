package com.shopforge.domain.usecase

import com.shopforge.domain.model.Price
import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class AddItemToShopUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val useCase = AddItemToShopUseCase(repository)

    @Test
    fun `adds item to shop inventory`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem()
        val adjustedPrice = Price.ofGold(16)

        useCase(shopId, item, 5, adjustedPrice)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(1, inventory.size)
        assertEquals(item, inventory[0].item)
        assertEquals(5, inventory[0].quantity)
        assertEquals(adjustedPrice, inventory[0].adjustedPrice)
    }

    @Test
    fun `adds item with unlimited quantity`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Tavern)
        val item = TestFixtures.sampleItem()

        useCase(shopId, item, null, item.price)

        val inventory = repository.getInventorySnapshot(shopId)
        assertNull(inventory[0].quantity)
    }

    @Test
    fun `adds multiple different items`() = runTest {
        val shopId = createUseCase("Shop", ShopType.GeneralStore)
        val item1 = TestFixtures.sampleItem(id = 1L, name = "Rope")
        val item2 = TestFixtures.sampleItem(id = 2L, name = "Torch")

        useCase(shopId, item1, 10, item1.price)
        useCase(shopId, item2, 20, item2.price)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(2, inventory.size)
    }
}
