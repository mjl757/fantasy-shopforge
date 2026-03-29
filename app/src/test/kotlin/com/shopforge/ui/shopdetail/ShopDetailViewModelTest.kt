package com.shopforge.ui.shopdetail

import app.cash.turbine.test
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository
import com.shopforge.domain.usecase.DecrementQuantityUseCase
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var shopFlow: MutableStateFlow<Shop?>
    private lateinit var inventoryFlow: MutableStateFlow<List<ShopInventoryItem>>
    private lateinit var fakeRepository: FakeShopRepository
    private lateinit var viewModel: ShopDetailViewModel

    private val testShop = Shop(
        id = 1L,
        name = "The Gilded Anvil",
        type = ShopType.Blacksmith,
        description = "A fine smithy",
        createdAt = 1000L,
        updatedAt = 1000L,
    )

    private val longsword = Item(
        id = 10L,
        name = "Longsword",
        description = "A versatile blade",
        category = ItemCategory.Weapon,
        price = Price.ofGold(15),
        rarity = Rarity.Common,
        isCustom = false,
    )

    private val healingPotion = Item(
        id = 20L,
        name = "Potion of Healing",
        description = "Restores health",
        category = ItemCategory.Potion,
        price = Price.ofGold(50),
        rarity = Rarity.Common,
        isCustom = false,
    )

    private val flameTongue = Item(
        id = 30L,
        name = "Flame Tongue",
        description = "A fiery blade",
        category = ItemCategory.Weapon,
        price = Price.ofGold(5000),
        rarity = Rarity.Rare,
        isCustom = false,
    )

    private val testInventory = listOf(
        ShopInventoryItem(item = longsword, quantity = 3, adjustedPrice = Price.ofGold(16)),
        ShopInventoryItem(item = healingPotion, quantity = null, adjustedPrice = Price.ofGold(50)),
        ShopInventoryItem(item = flameTongue, quantity = 0, adjustedPrice = Price.ofGold(5200)),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        shopFlow = MutableStateFlow(testShop)
        inventoryFlow = MutableStateFlow(testInventory)
        fakeRepository = FakeShopRepository(shopFlow, inventoryFlow)

        val getShopWithInventory = GetShopWithInventoryUseCase(fakeRepository)
        val decrementQuantity = DecrementQuantityUseCase(fakeRepository)

        viewModel = ShopDetailViewModel(
            shopId = 1L,
            getShopWithInventory = getShopWithInventory,
            decrementQuantity = decrementQuantity,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Before any emissions, state should be Loading
        assertEquals(ShopDetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `inventory loads correctly`() = runTest {
        viewModel.uiState.test {
            // Skip Loading
            awaitItem()
            val loaded = awaitItem() as ShopDetailUiState.Loaded

            assertEquals(testShop, loaded.shop)
            assertEquals(3, loaded.inventory.size)
            assertEquals("Longsword", loaded.inventory[0].item.name)
            assertEquals("Potion of Healing", loaded.inventory[1].item.name)
            assertEquals("Flame Tongue", loaded.inventory[2].item.name)
        }
    }

    @Test
    fun `search filters items by name`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.searchInventory("sword")
            val filtered = awaitItem() as ShopDetailUiState.Loaded

            assertEquals(1, filtered.inventory.size)
            assertEquals("Longsword", filtered.inventory[0].item.name)
            assertEquals("sword", filtered.searchQuery)
        }
    }

    @Test
    fun `search is case insensitive`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.searchInventory("POTION")
            val filtered = awaitItem() as ShopDetailUiState.Loaded

            assertEquals(1, filtered.inventory.size)
            assertEquals("Potion of Healing", filtered.inventory[0].item.name)
        }
    }

    @Test
    fun `empty search shows all items`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.searchInventory("sword")
            awaitItem() // Filtered

            viewModel.searchInventory("")
            val all = awaitItem() as ShopDetailUiState.Loaded

            assertEquals(3, all.inventory.size)
        }
    }

    @Test
    fun `decrement updates quantity for finite stock`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.decrementQuantity(longsword.id)
            advanceUntilIdle()

            // Verify repository was called with decremented quantity
            assertEquals(1, fakeRepository.updateQuantityCalls.size)
            val call = fakeRepository.updateQuantityCalls[0]
            assertEquals(1L, call.shopId)
            assertEquals(longsword.id, call.itemId)
            assertEquals(2, call.quantity)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `decrement on unlimited stock is no-op`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.decrementQuantity(healingPotion.id)
            advanceUntilIdle()

            // Should not have called updateItemQuantity
            assertTrue(fakeRepository.updateQuantityCalls.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `decrement on sold-out item is no-op`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Loaded

            viewModel.decrementQuantity(flameTongue.id)
            advanceUntilIdle()

            // Should not have called updateItemQuantity
            assertTrue(fakeRepository.updateQuantityCalls.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits NotFound when shop does not exist`() = runTest {
        shopFlow.value = null

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem()
            assertEquals(ShopDetailUiState.NotFound, state)
        }
    }
}

/**
 * Fake repository for testing that records calls and emits from provided flows.
 */
private class FakeShopRepository(
    private val shopFlow: MutableStateFlow<Shop?>,
    private val inventoryFlow: MutableStateFlow<List<ShopInventoryItem>>,
) : ShopRepository {

    data class UpdateQuantityCall(val shopId: Long, val itemId: Long, val quantity: Int?)

    val updateQuantityCalls = mutableListOf<UpdateQuantityCall>()

    override fun getAllShops(): Flow<List<Shop>> = throw NotImplementedError()
    override fun getShopById(id: Long): Flow<Shop?> = shopFlow
    override fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>> = inventoryFlow
    override suspend fun createShop(shop: Shop): Long = throw NotImplementedError()
    override suspend fun updateShop(shop: Shop) = throw NotImplementedError()
    override suspend fun deleteShop(id: Long) = throw NotImplementedError()
    override suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) = throw NotImplementedError()

    override suspend fun removeItemFromShop(shopId: Long, itemId: Long) = throw NotImplementedError()

    override suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?) {
        updateQuantityCalls.add(UpdateQuantityCall(shopId, itemId, quantity))
    }

    override suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>) = throw NotImplementedError()
}
