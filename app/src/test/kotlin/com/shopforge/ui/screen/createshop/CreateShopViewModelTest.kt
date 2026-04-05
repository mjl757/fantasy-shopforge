package com.shopforge.ui.screen.createshop

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.CreateShopUseCase
import com.shopforge.domain.usecase.GenerateShopNameUseCase
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class CreateShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeShopRepository: FakeShopRepository
    private lateinit var createShopUseCase: CreateShopUseCase
    private lateinit var generateShopNameUseCase: GenerateShopNameUseCase
    private lateinit var viewModel: CreateShopViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeShopRepository = FakeShopRepository()
        createShopUseCase = CreateShopUseCase(fakeShopRepository, clock = { 1000L })
        generateShopNameUseCase = GenerateShopNameUseCase(Random(42))
        viewModel = CreateShopViewModel(createShopUseCase, generateShopNameUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty form`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertNull(state.selectedType)
        assertEquals("", state.description)
        assertNull(state.nameError)
        assertFalse(state.isSaving)
        assertFalse(state.isValid)
    }

    @Test
    fun `onNameChanged updates name and clears error`() = runTest {
        viewModel.onNameChanged("The Iron Forge")
        val state = viewModel.uiState.value
        assertEquals("The Iron Forge", state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `onTypeSelected updates selected type`() = runTest {
        viewModel.onTypeSelected(ShopType.Blacksmith)
        assertEquals(ShopType.Blacksmith, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `onDescriptionChanged updates description`() = runTest {
        viewModel.onDescriptionChanged("A fine smithy")
        assertEquals("A fine smithy", viewModel.uiState.value.description)
    }

    @Test
    fun `isValid is true when name and type are set`() = runTest {
        viewModel.onNameChanged("Shop")
        viewModel.onTypeSelected(ShopType.Blacksmith)
        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `isValid is false when name is blank`() = runTest {
        viewModel.onNameChanged("   ")
        viewModel.onTypeSelected(ShopType.Blacksmith)
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `isValid is false when type is not selected`() = runTest {
        viewModel.onNameChanged("Shop")
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `generateName populates name field based on selected type`() = runTest {
        viewModel.onTypeSelected(ShopType.Blacksmith)
        viewModel.generateName()
        val name = viewModel.uiState.value.name
        assertTrue(name.isNotBlank(), "Generated name should not be blank")
        // With seeded random, the name should come from the Blacksmith templates
        assertTrue(
            GenerateShopNameUseCase.nameTemplates[ShopType.Blacksmith]!!.contains(name),
            "Generated name '$name' should be a valid Blacksmith template"
        )
    }

    @Test
    fun `generateName does nothing when no type selected`() = runTest {
        viewModel.generateName()
        assertEquals("", viewModel.uiState.value.name)
    }

    @Test
    fun `saveShop sets error when name is blank`() = runTest {
        viewModel.onTypeSelected(ShopType.Blacksmith)
        viewModel.saveShop()
        assertEquals("Shop name is required", viewModel.uiState.value.nameError)
    }

    @Test
    fun `saveShop creates shop and emits ShopCreated event`() = runTest {
        viewModel.onNameChanged("The Iron Forge")
        viewModel.onTypeSelected(ShopType.Blacksmith)
        viewModel.onDescriptionChanged("A sturdy forge")

        var createdShopId: Long? = null
        val job = launch {
            viewModel.events.first().let { event ->
                assertTrue(event is CreateShopEvent.ShopCreated)
                createdShopId = (event as CreateShopEvent.ShopCreated).shopId
            }
        }

        viewModel.saveShop()
        advanceUntilIdle()

        assertEquals(1L, createdShopId)
        assertEquals(1, fakeShopRepository.shops.size)
        assertEquals("The Iron Forge", fakeShopRepository.shops[1L]?.name)
        job.cancel()
    }

    @Test
    fun `saveShop stores trimmed name and description via CreateShopUseCase`() = runTest {
        viewModel.onNameChanged("  The Iron Forge  ")
        viewModel.onTypeSelected(ShopType.Blacksmith)
        viewModel.onDescriptionChanged("  A fine smithy  ")

        val job = launch { viewModel.events.first() }

        viewModel.saveShop()
        advanceUntilIdle()

        val shop = fakeShopRepository.shops.values.first()
        assertEquals("The Iron Forge", shop.name)
        assertEquals("A fine smithy", shop.description)
        job.cancel()
    }
}
