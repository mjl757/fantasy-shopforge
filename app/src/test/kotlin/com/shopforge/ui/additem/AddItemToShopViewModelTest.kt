package com.shopforge.ui.additem

import app.cash.turbine.test
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddItemToShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeItemRepository: FakeItemRepository
    private lateinit var fakeShopRepository: FakeShopRepository
    private lateinit var viewModel: AddItemToShopViewModel

    private val sampleItems = listOf(
        Item(
            id = 1L,
            name = "Longsword",
            description = "A standard longsword",
            category = ItemCategory.Weapon,
            price = Price.ofGold(15),
            rarity = Rarity.Common,
            isCustom = false,
        ),
        Item(
            id = 2L,
            name = "Chain Mail",
            description = "Heavy armor",
            category = ItemCategory.Armor,
            price = Price.ofGold(75),
            rarity = Rarity.Common,
            isCustom = false,
        ),
        Item(
            id = 3L,
            name = "Potion of Healing",
            description = "Restores health",
            category = ItemCategory.Potion,
            price = Price.ofGold(50),
            rarity = Rarity.Uncommon,
            isCustom = false,
        ),
        Item(
            id = 4L,
            name = "Vorpal Sword",
            description = "A legendary blade",
            category = ItemCategory.Weapon,
            price = Price.ofGold(5000),
            rarity = Rarity.Legendary,
            isCustom = false,
        ),
    )

    private val shopId = 1L

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeItemRepository = FakeItemRepository(sampleItems)
        fakeShopRepository = FakeShopRepository()
        viewModel = AddItemToShopViewModel(
            shopId = shopId,
            itemRepository = fakeItemRepository,
            shopRepository = fakeShopRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.filteredItems.isEmpty())
    }

    @Test
    fun `catalog items load successfully`() = runTest {
        viewModel.uiState.test {
            // Skip initial loading state
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            // Items loaded
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(4, loaded.filteredItems.size)
            assertEquals(4, loaded.allItems.size)
        }
    }

    @Test
    fun `search filters items by name`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onSearchQueryChanged("sword")
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all { "sword" in it.name.lowercase() })
            assertEquals("sword", state.searchQuery)
        }
    }

    @Test
    fun `search is case insensitive`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onSearchQueryChanged("POTION")
            val state = awaitItem()

            assertEquals(1, state.filteredItems.size)
            assertEquals("Potion of Healing", state.filteredItems.first().name)
        }
    }

    @Test
    fun `category filter works`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onCategorySelected(ItemCategory.Weapon)
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all { it.category == ItemCategory.Weapon })
            assertEquals(ItemCategory.Weapon, state.selectedCategory)
        }
    }

    @Test
    fun `clearing category filter shows all items`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onCategorySelected(ItemCategory.Weapon)
            val filtered = awaitItem()
            assertEquals(2, filtered.filteredItems.size)

            viewModel.onCategorySelected(null)
            val all = awaitItem()
            assertEquals(4, all.filteredItems.size)
            assertNull(all.selectedCategory)
        }
    }

    @Test
    fun `search and category filter combine`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onSearchQueryChanged("sword")
            awaitItem() // search applied

            viewModel.onCategorySelected(ItemCategory.Weapon)
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all {
                "sword" in it.name.lowercase() && it.category == ItemCategory.Weapon
            })
        }
    }

    @Test
    fun `add item to shop succeeds`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.addItem(1L, 5)
            val state = awaitItem()

            assertTrue(1L in state.addedItemIds)
            assertNull(state.errorMessage)

            // Verify the shop repository received the call
            assertEquals(1, fakeShopRepository.addedItems.size)
            val added = fakeShopRepository.addedItems.first()
            assertEquals(shopId, added.shopId)
            assertEquals(1L, added.itemId)
            assertEquals(5, added.quantity)
        }
    }

    @Test
    fun `add item with unlimited quantity`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.addItem(2L, null)
            val state = awaitItem()

            assertTrue(2L in state.addedItemIds)

            val added = fakeShopRepository.addedItems.first()
            assertNull(added.quantity)
        }
    }

    @Test
    fun `add nonexistent item does nothing`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.addItem(999L, 1)
            // No state change should be emitted for nonexistent items
            expectNoEvents()

            assertTrue(fakeShopRepository.addedItems.isEmpty())
        }
    }

    @Test
    fun `empty search query shows all items`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.onSearchQueryChanged("sword")
            val filtered = awaitItem()
            assertEquals(2, filtered.filteredItems.size)

            viewModel.onSearchQueryChanged("")
            val all = awaitItem()
            assertEquals(4, all.filteredItems.size)
        }
    }

    @Test
    fun `multiple items can be marked as added`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip loading
            awaitItem() // items loaded

            viewModel.addItem(1L, 1)
            val afterFirst = awaitItem()
            assertTrue(1L in afterFirst.addedItemIds)

            viewModel.addItem(3L, 2)
            val afterSecond = awaitItem()
            assertTrue(1L in afterSecond.addedItemIds)
            assertTrue(3L in afterSecond.addedItemIds)
            assertEquals(2, afterSecond.addedItemIds.size)
        }
    }

    // ---- Fake Repositories ----

    private class FakeItemRepository(items: List<Item>) : ItemRepository {
        private val itemsFlow = MutableStateFlow(items)
        private val itemMap = items.associateBy { it.id }

        override fun getAllItems(): Flow<List<Item>> = itemsFlow

        override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
            itemsFlow.value.filter { it.category == category }

        override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
            itemsFlow.value.filter { it.rarity == rarity }

        override suspend fun searchItems(query: String): List<Item> =
            itemsFlow.value.filter { it.name.contains(query, ignoreCase = true) }

        override suspend fun getItemById(id: Long): Item? = itemMap[id]

        override suspend fun createCustomItem(item: Item): Long = item.id
        override suspend fun updateCustomItem(item: Item) {}
        override suspend fun deleteCustomItem(id: Long) {}
    }

    private class FakeShopRepository : ShopRepository {
        data class AddedItem(
            val shopId: Long,
            val itemId: Long,
            val quantity: Int?,
            val adjustedPrice: Price,
        )

        val addedItems = mutableListOf<AddedItem>()

        override fun getAllShops(): Flow<List<Shop>> = MutableStateFlow(emptyList())
        override fun getShopById(id: Long): Flow<Shop?> = MutableStateFlow(null)
        override suspend fun createShop(shop: Shop): Long = 0L
        override suspend fun updateShop(shop: Shop) {}
        override suspend fun deleteShop(id: Long) {}
        override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> =
            MutableStateFlow(emptyList())

        override suspend fun addItemToShop(
            shopId: Long,
            item: Item,
            quantity: Int?,
            adjustedPrice: Price,
        ) {
            addedItems.add(AddedItem(shopId, item.id, quantity, adjustedPrice))
        }

        override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {}
        override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {}
        override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {}
    }
}
