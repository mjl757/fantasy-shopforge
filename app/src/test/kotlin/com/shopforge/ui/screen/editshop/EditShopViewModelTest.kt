package com.shopforge.ui.editshop

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.DeleteShopUseCase
import com.shopforge.domain.usecase.GenerateInventoryUseCase
import com.shopforge.domain.usecase.RegenerateInventoryUseCase
import com.shopforge.domain.usecase.UpdateShopUseCase
import com.shopforge.ui.editshop.EditShopEvent
import com.shopforge.ui.editshop.EditShopViewModel
import com.shopforge.ui.createshop.FakeShopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeShopRepository: FakeShopRepository
    private lateinit var fakeItemRepository: FakeItemRepository
    private lateinit var updateShopUseCase: UpdateShopUseCase
    private lateinit var deleteShopUseCase: DeleteShopUseCase
    private lateinit var regenerateInventoryUseCase: RegenerateInventoryUseCase

    private val testShop = Shop(
        id = 1L,
        name = "The Iron Forge",
        type = ShopType.Blacksmith,
        description = "A fine smithy",
        createdAt = 1000L,
        updatedAt = 1000L,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeShopRepository = FakeShopRepository()
        fakeItemRepository = FakeItemRepository()
        updateShopUseCase = UpdateShopUseCase(fakeShopRepository, clock = { 2000L })
        deleteShopUseCase = DeleteShopUseCase(fakeShopRepository)
        regenerateInventoryUseCase = RegenerateInventoryUseCase(
            fakeShopRepository,
            GenerateInventoryUseCase(fakeItemRepository),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EditShopViewModel {
        // Pre-populate the repository with the test shop (uses addShop to update flow)
        fakeShopRepository.addShop(testShop)
        return EditShopViewModel(
            shopId = testShop.id,
            updateShopUseCase = updateShopUseCase,
            deleteShopUseCase = deleteShopUseCase,
            regenerateInventoryUseCase = regenerateInventoryUseCase,
            getShopFlow = { id -> fakeShopRepository.getShopById(id) },
        )
    }

    @Test
    fun `loads existing shop data into form`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("The Iron Forge", state.name)
        assertEquals(ShopType.Blacksmith, state.selectedType)
        assertEquals("A fine smithy", state.description)
    }

    @Test
    fun `onNameChanged updates name`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChanged("The Golden Anvil")
        assertEquals("The Golden Anvil", viewModel.uiState.value.name)
    }

    @Test
    fun `onTypeSelected updates type`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onTypeSelected(ShopType.MagicShop)
        assertEquals(ShopType.MagicShop, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `saveShop sets error when name is blank`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChanged("")
        viewModel.saveShop()

        assertEquals("Shop name is required", viewModel.uiState.value.nameError)
    }

    @Test
    fun `saveShop updates shop and emits ShopUpdated event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChanged("The Golden Anvil")
        viewModel.onDescriptionChanged("Updated description")

        var eventReceived = false
        val job = launch {
            viewModel.events.first().let { event ->
                assertTrue(event is EditShopEvent.ShopUpdated)
                eventReceived = true
            }
        }

        viewModel.saveShop()
        advanceUntilIdle()

        assertTrue(eventReceived)
        val updatedShop = fakeShopRepository.shops[1L]!!
        assertEquals("The Golden Anvil", updatedShop.name)
        assertEquals("Updated description", updatedShop.description)
        assertEquals(2000L, updatedShop.updatedAt)
        job.cancel()
    }

    @Test
    fun `requestRegenerateInventory shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRegenerateConfirmation)
        viewModel.requestRegenerateInventory()
        assertTrue(viewModel.uiState.value.showRegenerateConfirmation)
    }

    @Test
    fun `dismissRegenerateConfirmation hides dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.requestRegenerateInventory()
        assertTrue(viewModel.uiState.value.showRegenerateConfirmation)
        viewModel.dismissRegenerateConfirmation()
        assertFalse(viewModel.uiState.value.showRegenerateConfirmation)
    }

    @Test
    fun `confirmRegenerateInventory triggers regeneration`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        var eventReceived = false
        val job = launch {
            viewModel.events.first().let { event ->
                assertTrue(event is EditShopEvent.InventoryRegenerated)
                eventReceived = true
            }
        }

        viewModel.requestRegenerateInventory()
        viewModel.confirmRegenerateInventory()
        advanceUntilIdle()

        assertTrue(eventReceived)
        assertFalse(viewModel.uiState.value.showRegenerateConfirmation)
        job.cancel()
    }

    @Test
    fun `requestDeleteShop shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        viewModel.requestDeleteShop()
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `dismissDeleteConfirmation hides dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.requestDeleteShop()
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
        viewModel.dismissDeleteConfirmation()
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `confirmDeleteShop deletes shop and emits ShopDeleted event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        var eventReceived = false
        val job = launch {
            viewModel.events.first().let { event ->
                assertTrue(event is EditShopEvent.ShopDeleted)
                eventReceived = true
            }
        }

        viewModel.requestDeleteShop()
        viewModel.confirmDeleteShop()
        advanceUntilIdle()

        assertTrue(eventReceived)
        assertFalse(fakeShopRepository.shops.containsKey(1L))
        job.cancel()
    }
}
