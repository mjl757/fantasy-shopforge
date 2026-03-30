package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CreateShopUseCaseTest {

    private val repository = FakeShopRepository()
    private val useCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })

    @Test
    fun `creates shop with name, type, and description`() = runTest {
        val id = useCase("The Gilded Anvil", ShopType.Blacksmith, "A fine smithy")

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("The Gilded Anvil", shop.name)
        assertEquals(ShopType.Blacksmith, shop.type)
        assertEquals("A fine smithy", shop.description)
        assertEquals(TestFixtures.FIXED_TIME, shop.createdAt)
        assertEquals(TestFixtures.FIXED_TIME, shop.updatedAt)
    }

    @Test
    fun `creates shop without description`() = runTest {
        val id = useCase("Magic Emporium", ShopType.MagicShop)

        val shop = repository.getShopSnapshot(id)!!
        assertNull(shop.description)
    }

    @Test
    fun `trims shop name`() = runTest {
        val id = useCase("  Padded Name  ", ShopType.GeneralStore)

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("Padded Name", shop.name)
    }

    @Test
    fun `empty description becomes null`() = runTest {
        val id = useCase("Shop", ShopType.Tavern, description = "")

        val shop = repository.getShopSnapshot(id)!!
        assertNull(shop.description)
    }

    @Test
    fun `whitespace-only description becomes null`() = runTest {
        val id = useCase("Shop", ShopType.Tavern, description = "   ")

        val shop = repository.getShopSnapshot(id)!!
        assertNull(shop.description)
    }

    @Test
    fun `blank name throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("", ShopType.Blacksmith)
        }
    }

    @Test
    fun `whitespace-only name throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("   ", ShopType.Blacksmith)
        }
    }

    @Test
    fun `shop is created with empty inventory`() = runTest {
        val id = useCase("Empty Shop", ShopType.GeneralStore)

        assertTrue(repository.getInventorySnapshot(id).isEmpty())
    }

    @Test
    fun `returns generated shop id`() = runTest {
        val id1 = useCase("Shop 1", ShopType.Blacksmith)
        val id2 = useCase("Shop 2", ShopType.Tavern)

        assertTrue(id1 > 0)
        assertTrue(id2 > id1)
    }
}
