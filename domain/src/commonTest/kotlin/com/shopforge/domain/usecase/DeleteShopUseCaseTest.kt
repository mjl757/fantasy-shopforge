package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DeleteShopUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)
    private val useCase = DeleteShopUseCase(repository)

    @Test
    fun `deletes existing shop`() = runTest {
        val id = createUseCase("Doomed Shop", ShopType.Blacksmith)

        useCase(id)

        assertNull(repository.getShopSnapshot(id))
    }

    @Test
    fun `deletes shop inventory along with shop`() = runTest {
        val id = createUseCase("Shop", ShopType.Blacksmith)
        val item = TestFixtures.sampleItem()
        addItemUseCase(id, item, 5, item.price)

        useCase(id)

        assertTrue(repository.getInventorySnapshot(id).isEmpty())
    }

    @Test
    fun `deleting nonexistent shop does not throw`() = runTest {
        useCase(999L) // Should complete without error
    }
}
