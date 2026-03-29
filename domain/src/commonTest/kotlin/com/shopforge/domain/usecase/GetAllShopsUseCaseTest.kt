package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class GetAllShopsUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val useCase = GetAllShopsUseCase(repository)

    @Test
    fun `returns empty list when no shops exist`() = runTest {
        val shops = useCase().first()
        assertTrue(shops.isEmpty())
    }

    @Test
    fun `returns all created shops`() = runTest {
        createUseCase("Shop A", ShopType.Blacksmith)
        createUseCase("Shop B", ShopType.Tavern)
        createUseCase("Shop C", ShopType.MagicShop)

        val shops = useCase().first()
        assertEquals(3, shops.size)
        assertEquals(setOf("Shop A", "Shop B", "Shop C"), shops.map { it.name }.toSet())
    }
}
