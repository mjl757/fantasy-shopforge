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
        val existing = repository.getShopSnapshot(id)!!

        useCase(existing, "New Name", ShopType.MagicShop, "New desc")

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("New Name", shop.name)
        assertEquals(ShopType.MagicShop, shop.type)
        assertEquals("New desc", shop.description)
    }

    @Test
    fun `updates updatedAt timestamp`() = runTest {
        val id = createUseCase("Shop", ShopType.Blacksmith, null)
        currentTime += 5000
        val existing = repository.getShopSnapshot(id)!!

        useCase(existing, "Updated", ShopType.Blacksmith, null)

        val shop = repository.getShopSnapshot(id)!!
        assertEquals(TestFixtures.FIXED_TIME, shop.createdAt)
        assertEquals(TestFixtures.FIXED_TIME + 5000, shop.updatedAt)
    }

    @Test
    fun `trims name and description`() = runTest {
        val id = createUseCase("Shop", ShopType.Tavern, null)
        val existing = repository.getShopSnapshot(id)!!

        useCase(existing, "  Trimmed  ", ShopType.Tavern, "  Trimmed desc  ")

        val shop = repository.getShopSnapshot(id)!!
        assertEquals("Trimmed", shop.name)
        assertEquals("Trimmed desc", shop.description)
    }

    @Test
    fun `empty description becomes null`() = runTest {
        val id = createUseCase("Shop", ShopType.Tavern, "Has desc")
        val existing = repository.getShopSnapshot(id)!!

        useCase(existing, "Shop", ShopType.Tavern, description = "")

        val shop = repository.getShopSnapshot(id)!!
        assertNull(shop.description)
    }

    @Test
    fun `blank name throws IllegalArgumentException`() = runTest {
        val id = createUseCase("Shop", ShopType.Blacksmith, null)
        val existing = repository.getShopSnapshot(id)!!

        assertFailsWith<IllegalArgumentException> {
            useCase(existing, "", ShopType.Blacksmith, null)
        }
    }

    @Test
    fun `nonexistent shop throws NoSuchElementException`() = runTest {
        // Build a shop object that doesn't exist in the repository
        val nonExistentShop = repository.getShopSnapshot(1L)?.copy(id = 999L)
            ?: com.shopforge.domain.model.Shop(
                id = 999L,
                name = "Ghost",
                type = ShopType.Blacksmith,
                description = null,
                createdAt = currentTime,
                updatedAt = currentTime,
            )

        assertFailsWith<NoSuchElementException> {
            useCase(nonExistentShop, "Name", ShopType.Blacksmith, null)
        }
    }
}
