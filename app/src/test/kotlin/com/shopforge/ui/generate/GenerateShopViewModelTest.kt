package com.shopforge.ui.generate

import app.cash.turbine.test
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import com.shopforge.domain.usecase.GenerateShopUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no selection and is not loading`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.selectedType)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.generatedShopId)
    }

    @Test
    fun `selectType updates the selected type`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectType(ShopType.Blacksmith)
        assertEquals(ShopType.Blacksmith, viewModel.uiState.value.selectedType)

        viewModel.selectType(ShopType.MagicShop)
        assertEquals(ShopType.MagicShop, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `selectType with null clears selection`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectType(ShopType.Blacksmith)
        assertEquals(ShopType.Blacksmith, viewModel.uiState.value.selectedType)

        viewModel.selectType(null)
        assertNull(viewModel.uiState.value.selectedType)
    }

    @Test
    fun `generate with specific type produces a shop with that type`() = runTest {
        val shopRepo = FakeShopRepository()
        val viewModel = createViewModel(shopRepository = shopRepo)

        viewModel.selectType(ShopType.Blacksmith)
        viewModel.generate()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.generatedShopId == null && state.error == null) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertNotNull(state.generatedShopId)
            assertNull(state.error)

            // Verify the shop was created with the correct type
            val createdShop = shopRepo.createdShops.first()
            assertEquals(ShopType.Blacksmith, createdShop.type)
        }
    }

    @Test
    fun `generate with no selection generates a shop with random type`() = runTest {
        val shopRepo = FakeShopRepository()
        val viewModel = createViewModel(shopRepository = shopRepo)

        // No type selected -- should still succeed
        viewModel.generate()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.generatedShopId == null && state.error == null) {
                state = awaitItem()
            }
            assertNotNull(state.generatedShopId)
            assertNull(state.error)

            // A shop was created (with whatever random type the seeded Random chose)
            assertTrue(shopRepo.createdShops.isNotEmpty())
        }
    }

    @Test
    fun `generate shows loading state during generation`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.generate()

            // Should see loading = true
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            // Then success
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertNotNull(success.generatedShopId)
        }
    }

    @Test
    fun `generate sets error state on failure`() = runTest {
        val errorMessage = "Database error"
        val failingRepo = FailingShopRepository(RuntimeException(errorMessage))
        val viewModel = createViewModel(shopRepository = failingRepo)

        viewModel.generate()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.error == null && !state.isLoading) {
                state = awaitItem()
            }
            // Advance through loading to error
            while (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)
            assertNull(state.generatedShopId)
        }
    }

    @Test
    fun `onNavigated resets generated shop id`() = runTest {
        val viewModel = createViewModel()

        viewModel.generate()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.generatedShopId == null) {
                state = awaitItem()
            }
            assertNotNull(state.generatedShopId)

            viewModel.onNavigated()

            val afterNav = awaitItem()
            assertNull(afterNav.generatedShopId)
        }
    }

    @Test
    fun `generated shop has a thematic name`() = runTest {
        val shopRepo = FakeShopRepository()
        val viewModel = createViewModel(shopRepository = shopRepo)

        viewModel.selectType(ShopType.Blacksmith)
        viewModel.generate()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.generatedShopId == null) {
                state = awaitItem()
            }
            val shop = shopRepo.createdShops.first()
            assertTrue(shop.name.isNotBlank(), "Shop name should not be blank")
            assertTrue(shop.name.contains(" "), "Shop name should have prefix and suffix")
        }
    }

    // --- Helpers ---

    private val catalogItems = listOf(
        Item(id = 1, name = "Longsword", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 2, name = "Shortsword", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 3, name = "Chain Mail", category = ItemCategory.Armor, price = Price.ofGold(75), rarity = Rarity.Common, isCustom = false),
        Item(id = 4, name = "Leather Armor", category = ItemCategory.Armor, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 5, name = "Plate Armor", category = ItemCategory.Armor, price = Price.ofGold(1500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 6, name = "Dagger", category = ItemCategory.Weapon, price = Price.ofGold(2), rarity = Rarity.Common, isCustom = false),
        Item(id = 7, name = "Battleaxe", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 8, name = "Warhammer", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 9, name = "Wooden Shield", category = ItemCategory.Armor, price = Price.ofGold(5), rarity = Rarity.Common, isCustom = false),
        Item(id = 10, name = "Potion of Healing", category = ItemCategory.Potion, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),
        Item(id = 11, name = "Wand of Sparks", category = ItemCategory.MagicItem, price = Price.ofGold(500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 12, name = "Rations", category = ItemCategory.Food, price = Price.ofSilver(5), rarity = Rarity.Common, isCustom = false),
    )

    private fun createViewModel(
        shopRepository: ShopRepository = FakeShopRepository(),
    ): GenerateShopViewModel {
        val itemRepo = FakeItemRepository(catalogItems)
        val useCase = GenerateShopUseCase(
            shopRepository = shopRepository,
            itemRepository = itemRepo,
            random = Random(seed = 42),
            clock = { 1000L },
        )
        return GenerateShopViewModel(useCase)
    }
}

/** In-memory fake [ShopRepository] for testing. */
private class FakeShopRepository : ShopRepository {
    val createdShops = mutableListOf<Shop>()
    private var nextId = 1L

    override fun getAllShops(): Flow<List<Shop>> = flowOf(createdShops.toList())
    override fun getShopById(id: Long): Flow<Shop?> = flowOf(createdShops.find { it.id == id })

    override suspend fun createShop(shop: Shop): Long {
        val id = nextId++
        createdShops.add(shop.copy(id = id))
        return id
    }

    override suspend fun updateShop(shop: Shop) {
        val index = createdShops.indexOfFirst { it.id == shop.id }
        if (index >= 0) createdShops[index] = shop
    }

    override suspend fun deleteShop(id: Long) {
        createdShops.removeAll { it.id == id }
    }

    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = flowOf(emptyList())

    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {}

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {}
    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {}
    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {}
}

/** A [ShopRepository] that throws on [createShop]. */
private class FailingShopRepository(private val error: Throwable) : ShopRepository {
    override fun getAllShops(): Flow<List<Shop>> = flowOf(emptyList())
    override fun getShopById(id: Long): Flow<Shop?> = flowOf(null)
    override suspend fun createShop(shop: Shop): Long = throw error
    override suspend fun updateShop(shop: Shop) {}
    override suspend fun deleteShop(id: Long) {}
    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = flowOf(emptyList())
    override suspend fun addItemToShop(shopId: Long, item: Item, quantity: Int?, adjustedPrice: Price) {}
    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {}
    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {}
    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {}
}

/** In-memory fake [ItemRepository] for testing. */
private class FakeItemRepository(private val items: List<Item>) : ItemRepository {
    override fun getAllItems(): Flow<List<Item>> = flowOf(items)

    override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
        items.filter { it.category == category }

    override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
        items.filter { it.rarity == rarity }

    override suspend fun searchItems(query: String): List<Item> =
        items.filter { it.name.contains(query, ignoreCase = true) }

    override suspend fun getItemById(id: Long): Item? =
        items.find { it.id == id }

    override suspend fun createCustomItem(item: Item): Long = item.id
    override suspend fun updateCustomItem(item: Item) {}
    override suspend fun deleteCustomItem(id: Long) {}
}
