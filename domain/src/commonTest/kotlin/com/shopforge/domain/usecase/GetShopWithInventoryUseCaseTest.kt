package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class GetShopWithInventoryUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)
    private val useCase = GetShopWithInventoryUseCase(repository)

    @Test
    fun `returns null for nonexistent shop`() = runTest {
        val result = useCase(999L).first()
        assertNull(result)
    }

    @Test
    fun `returns shop with empty inventory`() = runTest {
        val id = createUseCase("Empty Shop", ShopType.GeneralStore)

        val result = useCase(id).first()
        assertNotNull(result)
        assertEquals("Empty Shop", result.shop.name)
        assertTrue(result.inventory.isEmpty())
    }

    @Test
    fun `returns shop with inventory items`() = runTest {
        val id = createUseCase("Stocked Shop", ShopType.Blacksmith)
        val item1 = TestFixtures.sampleItem(id = 1L, name = "Sword")
        val item2 = TestFixtures.sampleItem(id = 2L, name = "Shield")
        addItemUseCase(id, item1, 3, item1.price)
        addItemUseCase(id, item2, null, item2.price)

        val result = useCase(id).first()
        assertNotNull(result)
        assertEquals(2, result.inventory.size)
    }
}
