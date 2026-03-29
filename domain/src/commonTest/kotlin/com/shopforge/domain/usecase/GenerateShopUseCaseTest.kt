package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GenerateShopUseCaseTest {

    // ---- Fakes ----

    private class FakeShopRepository : ShopRepository {
        var createdShop: Shop? = null
        var replacedInventoryShopId: Long? = null
        var replacedInventoryItems: List<ShopInventoryItem>? = null
        private var nextId = 1L

        override fun getAllShops(): Flow<List<Shop>> = flowOf(emptyList())
        override fun getShopById(id: Long): Flow<Shop?> = flowOf(null)

        override suspend fun createShop(shop: Shop): Long {
            createdShop = shop
            return nextId++
        }

        override suspend fun updateShop(shop: Shop) {}
        override suspend fun deleteShop(id: Long) {}
        override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = flowOf(emptyList())
        override suspend fun addItemToShop(shopId: Long, item: Item, quantity: Int?, adjustedPrice: Price) {}
        override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {}
        override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {}

        override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
            replacedInventoryShopId = shopId
            replacedInventoryItems = items
        }
    }

    private class FakeGenerateInventory : GenerateInventoryUseCase {
        var invokedWithType: ShopType? = null

        private val fakeItem = Item(
            id = 100L,
            name = "Test Sword",
            category = ItemCategory.Weapon,
            price = Price.ofGold(10),
            rarity = Rarity.Common,
            isCustom = false,
        )

        val fakeInventory = listOf(
            ShopInventoryItem(
                item = fakeItem,
                quantity = 3,
                adjustedPrice = Price.ofGold(11),
            )
        )

        override suspend fun invoke(shopType: ShopType, random: Random): List<ShopInventoryItem> {
            invokedWithType = shopType
            return fakeInventory
        }
    }

    private val fixedClock: () -> Long = { 1000000L }

    // ---- Tests ----

    @Test
    fun `generates shop with specified type`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = ShopType.Blacksmith, random = Random(42))

        assertEquals(ShopType.Blacksmith, result.shop.type)
        assertEquals(1L, result.shop.id)
        assertEquals(ShopType.Blacksmith, inventory.invokedWithType)
    }

    @Test
    fun `generates shop with random type when null`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = null, random = Random(42))

        assertNotNull(result.shop.type)
        // The type should be a valid ShopType
        assertTrue(result.shop.type in ShopType.entries)
    }

    @Test
    fun `shop name matches the generated type pool`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = ShopType.MagicShop, random = Random(42))

        val expectedNames = GenerateShopNameUseCase.nameTemplates.getValue(ShopType.MagicShop)
        assertTrue(
            result.shop.name in expectedNames,
            "Shop name '${result.shop.name}' not in MagicShop pool"
        )
    }

    @Test
    fun `persists shop to repository`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        useCase(shopType = ShopType.Tavern, random = Random(42))

        assertNotNull(repository.createdShop)
        assertEquals(ShopType.Tavern, repository.createdShop!!.type)
    }

    @Test
    fun `persists inventory to repository with correct shop id`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        useCase(shopType = ShopType.Blacksmith, random = Random(42))

        assertEquals(1L, repository.replacedInventoryShopId)
        assertEquals(inventory.fakeInventory, repository.replacedInventoryItems)
    }

    @Test
    fun `result contains inventory from GenerateInventoryUseCase`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = ShopType.Blacksmith, random = Random(42))

        assertEquals(inventory.fakeInventory, result.inventory)
    }

    @Test
    fun `shop timestamps use provided clock`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = ShopType.Blacksmith, random = Random(42))

        assertEquals(1000000L, result.shop.createdAt)
        assertEquals(1000000L, result.shop.updatedAt)
    }

    @Test
    fun `shop description is null for generated shops`() = runTest {
        val repository = FakeShopRepository()
        val inventory = FakeGenerateInventory()
        val useCase = GenerateShopUseCase(
            shopRepository = repository,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory,
            clock = fixedClock,
        )

        val result = useCase(shopType = ShopType.Blacksmith, random = Random(42))

        assertNull(result.shop.description)
    }

    @Test
    fun `seeded random produces deterministic shop type when type is null`() = runTest {
        val repository1 = FakeShopRepository()
        val inventory1 = FakeGenerateInventory()
        val useCase1 = GenerateShopUseCase(
            shopRepository = repository1,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory1,
            clock = fixedClock,
        )

        val repository2 = FakeShopRepository()
        val inventory2 = FakeGenerateInventory()
        val useCase2 = GenerateShopUseCase(
            shopRepository = repository2,
            generateShopName = GenerateShopNameUseCase(),
            generateInventory = inventory2,
            clock = fixedClock,
        )

        val result1 = useCase1(shopType = null, random = Random(42))
        val result2 = useCase2(shopType = null, random = Random(42))

        assertEquals(result1.shop.type, result2.shop.type)
        assertEquals(result1.shop.name, result2.shop.name)
    }

    private fun assertTrue(condition: Boolean, message: String = "") {
        kotlin.test.assertTrue(condition, message)
    }
}
