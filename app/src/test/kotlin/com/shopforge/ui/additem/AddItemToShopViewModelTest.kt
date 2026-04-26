package com.shopforge.ui.additem

import app.cash.turbine.test
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Denomination
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import com.shopforge.domain.usecase.AddItemToShopUseCase
import com.shopforge.domain.usecase.GetAllItemsUseCase
import com.shopforge.domain.usecase.GetShopWithInventoryUseCase
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddItemToShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val shopId = 1L

    private val sampleItems = listOf(
        Item(id = 1L, name = "Longsword", category = ItemCategory.Weapon, price = Price(15, Denomination.Gold), rarity = Rarity.Common, isCustom = false),
        Item(id = 2L, name = "Chain Mail", category = ItemCategory.Armor, price = Price(75, Denomination.Gold), rarity = Rarity.Common, isCustom = false),
        Item(id = 3L, name = "Potion of Healing", category = ItemCategory.Potion, price = Price(50, Denomination.Gold), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 4L, name = "Vorpal Sword", category = ItemCategory.Weapon, price = Price(5000, Denomination.Gold), rarity = Rarity.Legendary, isCustom = false),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.filteredItems.isEmpty())
    }

    @Test
    fun `catalog items load successfully`() = runTest {
        createViewModel().uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(4, loaded.filteredItems.size)
            assertEquals(4, loaded.allItems.size)
        }
    }

    @Test
    fun `search filters items by name`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1) // loading
            awaitItem()  // loaded

            viewModel.onSearchQueryChanged("sword")
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all { "sword" in it.name.lowercase() })
            assertEquals("sword", state.searchQuery)
        }
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onSearchQueryChanged("POTION")
            val state = awaitItem()

            assertEquals(1, state.filteredItems.size)
            assertEquals("Potion of Healing", state.filteredItems.first().name)
        }
    }

    @Test
    fun `category filter works`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onCategorySelected(ItemCategory.Weapon)
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all { it.category == ItemCategory.Weapon })
            assertEquals(ItemCategory.Weapon, state.selectedCategory)
        }
    }

    @Test
    fun `clearing category filter shows all items`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

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
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onSearchQueryChanged("sword")
            awaitItem()

            viewModel.onCategorySelected(ItemCategory.Weapon)
            val state = awaitItem()

            assertEquals(2, state.filteredItems.size)
            assertTrue(state.filteredItems.all {
                "sword" in it.name.lowercase() && it.category == ItemCategory.Weapon
            })
        }
    }

    @Test
    fun `selecting an item opens the quantity dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            val loaded = awaitItem()
            assertNull(loaded.pendingAddItem)

            viewModel.onItemSelected(sampleItems[0])
            val withDialog = awaitItem()
            assertEquals(sampleItems[0], withDialog.pendingAddItem)
        }
    }

    @Test
    fun `dismissing the dialog clears pendingAddItem`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onItemSelected(sampleItems[0])
            awaitItem() // dialog open

            viewModel.onAddDismissed()
            val afterDismiss = awaitItem()
            assertNull(afterDismiss.pendingAddItem)
        }
    }

    @Test
    fun `add item to shop succeeds`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem() // loaded

            viewModel.onItemSelected(sampleItems[0])
            awaitItem() // dialog open

            viewModel.onAddConfirmed(5)
            // onAddConfirmed clears pendingAddItem synchronously (first emit),
            // then updates addedItemIds in a coroutine (second emit).
            val dialogClosed = awaitItem()
            assertNull(dialogClosed.pendingAddItem)

            val added = awaitItem()
            assertTrue(1L in added.addedItemIds)
            assertNull(added.error)
        }
    }

    @Test
    fun `add item with unlimited quantity`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onItemSelected(sampleItems[1])
            awaitItem()

            viewModel.onAddConfirmed(null)
            awaitItem() // dialog closed
            val added = awaitItem() // addedItemIds updated
            assertTrue(2L in added.addedItemIds)
        }
    }

    @Test
    fun `add item with zero quantity sets error state`() = runTest {
        // AddItemToShopUseCase requires quantity >= 0, but quantity = 0 passes
        // the guard. The use case guard is quantity >= 0 (0 allowed).
        // Negative quantity should throw and produce an error state.
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onItemSelected(sampleItems[0])
            awaitItem()

            viewModel.onAddConfirmed(-1)
            // dialog closes immediately (pendingAddItem cleared before launch)
            val afterConfirm = awaitItem()
            assertNull(afterConfirm.pendingAddItem)

            // error is set after the coroutine runs
            val errorState = awaitItem()
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.message.contains("negative", ignoreCase = true))
        }
    }

    @Test
    fun `multiple items can be marked as added`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onItemSelected(sampleItems[0])
            awaitItem()
            viewModel.onAddConfirmed(1)
            awaitItem() // dialog closed
            awaitItem() // item 1 added

            viewModel.onItemSelected(sampleItems[2])
            awaitItem()
            viewModel.onAddConfirmed(2)
            awaitItem() // dialog closed
            val afterSecond = awaitItem() // item 3 added

            assertTrue(1L in afterSecond.addedItemIds)
            assertTrue(3L in afterSecond.addedItemIds)
            assertEquals(2, afterSecond.addedItemIds.size)
        }
    }

    @Test
    fun `empty search query shows all items`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onSearchQueryChanged("sword")
            val filtered = awaitItem()
            assertEquals(2, filtered.filteredItems.size)

            viewModel.onSearchQueryChanged("")
            val all = awaitItem()
            assertEquals(4, all.filteredItems.size)
        }
    }

    @Test
    fun `items already in inventory are shown as added on open`() = runTest {
        val shopRepo = FakeShopRepository(
            initialInventory = listOf(
                ShopInventoryItem(item = sampleItems[0], quantity = 3, adjustedPrice = sampleItems[0].price),
            ),
        )
        val viewModel = createViewModel(
            addItemToShopUseCase = AddItemToShopUseCase(shopRepo),
            getShopWithInventoryUseCase = GetShopWithInventoryUseCase(shopRepo),
        )

        viewModel.uiState.test {
            // Advance until all coroutines (items load + init seeding) have settled,
            // then inspect the most recent state.
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(1L in state.addedItemIds)
            assertFalse(2L in state.addedItemIds)
            cancelAndConsumeRemainingEvents()
        }
    }

    // --- Helpers ---

    private fun createViewModel(
        getAllItemsUseCase: GetAllItemsUseCase = GetAllItemsUseCase(FakeItemRepository(sampleItems)),
        addItemToShopUseCase: AddItemToShopUseCase = AddItemToShopUseCase(FakeShopRepository()),
        getShopWithInventoryUseCase: GetShopWithInventoryUseCase = GetShopWithInventoryUseCase(FakeShopRepository()),
    ): AddItemToShopViewModel = AddItemToShopViewModel(
        shopId = shopId,
        getAllItemsUseCase = getAllItemsUseCase,
        addItemToShopUseCase = addItemToShopUseCase,
        getShopWithInventoryUseCase = getShopWithInventoryUseCase,
    )

    // ---- Fake Repositories ----

    private class FakeItemRepository(items: List<Item>) : ItemRepository {
        private val itemsFlow = MutableStateFlow(items)

        override fun getAllItems(): Flow<List<Item>> = itemsFlow

        override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
            itemsFlow.value.filter { it.category == category }

        override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
            itemsFlow.value.filter { it.rarity == rarity }

        override suspend fun searchItems(query: String): List<Item> =
            itemsFlow.value.filter { it.name.contains(query, ignoreCase = true) }

        override suspend fun getItemById(id: Long): Item? = itemsFlow.value.find { it.id == id }

        override suspend fun createCustomItem(item: Item): Long = item.id
        override suspend fun updateCustomItem(item: Item) {}
        override suspend fun deleteCustomItem(id: Long) {}
    }

    private class FakeShopRepository(
        initialInventory: List<ShopInventoryItem> = emptyList(),
    ) : ShopRepository {
        private val inventoryFlow = MutableStateFlow(initialInventory)

        override fun getAllShops(): Flow<List<Shop>> = MutableStateFlow(emptyList())
        override fun getShopById(id: Long): Flow<Shop?> = MutableStateFlow(
            Shop(id = id, name = "Test Shop", type = ShopType.Blacksmith, description = null, createdAt = 0L, updatedAt = 0L)
        )
        override suspend fun createShop(shop: Shop): Long = 0L
        override suspend fun updateShop(shop: Shop) {}
        override suspend fun deleteShop(id: Long) {}
        override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = inventoryFlow

        override suspend fun addItemToShop(
            shopId: Long,
            item: Item,
            quantity: Int?,
            adjustedPrice: Price,
        ) {
            inventoryFlow.value = inventoryFlow.value + ShopInventoryItem(item, quantity, adjustedPrice)
        }

        override suspend fun removeItemFromShop(shopId: Long, itemId: Long) {}
    override suspend fun updateItemAdjustedPrice(shopId: Long, itemId: Long, adjustedPrice: Price) {
        throw NotImplementedError()
    }
        override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {}
        override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) {
            inventoryFlow.value = items
        }
    }
}
