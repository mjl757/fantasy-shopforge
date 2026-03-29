package com.shopforge.ui.shoplist

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.GetAllShopsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: FakeShopRepository): ShopListViewModel {
        val useCase = GetAllShopsUseCase(repository)
        return ShopListViewModel(useCase)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val repository = FakeShopRepository()
        val viewModel = createViewModel(repository)

        assertEquals(ShopListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `emits Empty state when no shops exist`() = runTest {
        val repository = FakeShopRepository()
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(ShopListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `emits Content state when shops exist`() = runTest {
        val shops = listOf(
            makeShop(id = 1, name = "The Gilded Anvil", type = ShopType.Blacksmith),
            makeShop(id = 2, name = "Arcane Emporium", type = ShopType.MagicShop),
        )
        val repository = FakeShopRepository(initialShops = shops)
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertInstanceOf(ShopListUiState.Content::class.java, state)
        val content = state as ShopListUiState.Content
        assertEquals(2, content.shops.size)
        assertNull(content.selectedFilter)
        assertEquals(ShopSortOrder.Name, content.sortOrder)
    }

    @Test
    fun `shops are sorted by name by default`() = runTest {
        val shops = listOf(
            makeShop(id = 1, name = "Zephyr's Wands", type = ShopType.MagicShop),
            makeShop(id = 2, name = "Anvil & Hammer", type = ShopType.Blacksmith),
        )
        val repository = FakeShopRepository(initialShops = shops)
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val content = viewModel.uiState.value as ShopListUiState.Content
        assertEquals("Anvil & Hammer", content.shops[0].name)
        assertEquals("Zephyr's Wands", content.shops[1].name)
    }

    @Test
    fun `filter by type reduces the list`() = runTest {
        val shops = listOf(
            makeShop(id = 1, name = "The Gilded Anvil", type = ShopType.Blacksmith),
            makeShop(id = 2, name = "Arcane Emporium", type = ShopType.MagicShop),
            makeShop(id = 3, name = "Iron Works", type = ShopType.Blacksmith),
        )
        val repository = FakeShopRepository(initialShops = shops)
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.onFilterSelected(ShopType.Blacksmith)
        advanceUntilIdle()

        val content = viewModel.uiState.value as ShopListUiState.Content
        assertEquals(2, content.shops.size)
        assertTrue(content.shops.all { it.type == ShopType.Blacksmith })
        assertEquals(ShopType.Blacksmith, content.selectedFilter)
    }

    @Test
    fun `clearing filter shows all shops`() = runTest {
        val shops = listOf(
            makeShop(id = 1, name = "The Gilded Anvil", type = ShopType.Blacksmith),
            makeShop(id = 2, name = "Arcane Emporium", type = ShopType.MagicShop),
        )
        val repository = FakeShopRepository(initialShops = shops)
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.onFilterSelected(ShopType.Blacksmith)
        advanceUntilIdle()
        viewModel.onFilterSelected(null)
        advanceUntilIdle()

        val content = viewModel.uiState.value as ShopListUiState.Content
        assertEquals(2, content.shops.size)
        assertNull(content.selectedFilter)
    }

    @Test
    fun `sorting by created date sorts newest first`() = runTest {
        val shops = listOf(
            makeShop(id = 1, name = "Old Shop", type = ShopType.Blacksmith, createdAt = 1000L),
            makeShop(id = 2, name = "New Shop", type = ShopType.MagicShop, createdAt = 2000L),
        )
        val repository = FakeShopRepository(initialShops = shops)
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        viewModel.onSortOrderChanged(ShopSortOrder.CreatedDate)
        advanceUntilIdle()

        val content = viewModel.uiState.value as ShopListUiState.Content
        assertEquals("New Shop", content.shops[0].name)
        assertEquals("Old Shop", content.shops[1].name)
        assertEquals(ShopSortOrder.CreatedDate, content.sortOrder)
    }

    @Test
    fun `onShopClicked emits navigation event`() = runTest {
        val repository = FakeShopRepository(initialShops = listOf(makeShop(id = 42)))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val collected = mutableListOf<ShopListEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { collected.add(it) } }
        advanceUntilIdle()

        viewModel.onShopClicked(42)
        advanceUntilIdle()

        assertEquals(1, collected.size)
        assertEquals(ShopListEvent.NavigateToShopDetail(42), collected[0])

        job.cancel()
    }

    @Test
    fun `onCreateShopClicked emits navigation event`() = runTest {
        val repository = FakeShopRepository()
        val viewModel = createViewModel(repository)

        val collected = mutableListOf<ShopListEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { collected.add(it) } }
        advanceUntilIdle()

        viewModel.onCreateShopClicked()
        advanceUntilIdle()

        assertEquals(1, collected.size)
        assertEquals(ShopListEvent.NavigateToCreateShop, collected[0])

        job.cancel()
    }

    @Test
    fun `onGenerateShopClicked emits navigation event`() = runTest {
        val repository = FakeShopRepository()
        val viewModel = createViewModel(repository)

        val collected = mutableListOf<ShopListEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { collected.add(it) } }
        advanceUntilIdle()

        viewModel.onGenerateShopClicked()
        advanceUntilIdle()

        assertEquals(1, collected.size)
        assertEquals(ShopListEvent.NavigateToGenerateShop, collected[0])

        job.cancel()
    }

    @Test
    fun `reactive updates when repository emits new data`() = runTest {
        val repository = FakeShopRepository()
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertEquals(ShopListUiState.Empty, viewModel.uiState.value)

        repository.emitShops(listOf(makeShop(id = 1, name = "New Shop")))
        advanceUntilIdle()

        val content = viewModel.uiState.value as ShopListUiState.Content
        assertEquals(1, content.shops.size)
        assertEquals("New Shop", content.shops[0].name)
    }

    // -- Helpers --

    private fun makeShop(
        id: Long = 1,
        name: String = "Test Shop",
        type: ShopType = ShopType.GeneralStore,
        description: String? = null,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis(),
    ) = Shop(
        id = id,
        name = name,
        type = type,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
