package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class UpdateShopUseCaseTest {

    private val repository = FakeShopRepository()
    private var currentTime = TestFixtures.FIXED_TIME
    private val createUseCase = CreateShopUseCase(repository, clock = { currentTime })
    private val useCase = UpdateShopUseCase(repository, clock = { currentTime })

    @Test
    fun `updates shop name, type, and description`() = runTest {
        val id = createUseCase("Old Name", ShopType.Blacksmith, "Old desc")
        currentTime += 1000

        useCase(id, "New Name", ShopType.MagicShop, "New desc")

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("New Name", shop.name)
        assertEquals(ShopType.MagicShop, shop.type)
        assertEquals("New desc", shop.description)
    }

    @Test
    fun `updates updatedAt timestamp`() = runTest {
        val id = createUseCase("Shop", ShopType.Blacksmith)
        currentTime += 5000

        useCase(id, "Updated", ShopType.Blacksmith)

        val shop = repository.getShopSnapshot(id)!!
        assertEquals(TestFixtures.FIXED_TIME, shop.createdAt)
        assertEquals(TestFixtures.FIXED_TIME + 5000, shop.updatedAt)
    }

    @Test
    fun `trims name and description`() = runTest {
        val id = createUseCase("Shop", ShopType.Tavern)

        useCase(id, "  Trimmed  ", ShopType.Tavern, "  Trimmed desc  ")

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("Trimmed", shop.name)
        assertEquals("Trimmed desc", shop.description)
    }

    @Test
    fun `empty description becomes null`() = runTest {
        val id = createUseCase("Shop", ShopType.Tavern, "Has desc")

        useCase(id, "Shop", ShopType.Tavern, description = "")

        val shop = repository.getShopSnapshot(id)!!
        assertNull(shop.description)
    }

    @Test
    fun `blank name throws IllegalArgumentException`() = runTest {
        val id = createUseCase("Shop", ShopType.Blacksmith)

        assertFailsWith<IllegalArgumentException> {
            useCase(id, "", ShopType.Blacksmith)
        }
    }

    @Test
    fun `nonexistent shop throws NoSuchElementException`() = runTest {
        assertFailsWith<NoSuchElementException> {
            useCase(999L, "Name", ShopType.Blacksmith)
        }
    }
}
