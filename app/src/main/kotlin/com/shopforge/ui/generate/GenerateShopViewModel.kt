package com.shopforge.ui.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.GenerateShopUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Wraps an error message with a unique ID so that consecutive identical errors
 * each produce a new key for [LaunchedEffect], ensuring the snackbar always fires.
 */
data class UiError(val id: Long = System.nanoTime(), val message: String)

/**
 * UI state for the Generate Shop screen.
 */
data class GenerateShopUiState(
    /** The currently selected shop type, or null for random. */
    val selectedType: ShopType? = null,
    /** Whether shop generation is in progress. */
    val isLoading: Boolean = false,
    /** Error to display, or null if none. */
    val error: UiError? = null,
    /** The ID of the generated shop, triggering navigation when non-null. */
    val generatedShopId: Long? = null,
)

/**
 * ViewModel for the Generate Shop screen.
 * Manages the optional shop type selection and delegates generation
 * to [GenerateShopUseCase].
 */
class GenerateShopViewModel(
    private val generateShopUseCase: GenerateShopUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateShopUiState())
    val uiState: StateFlow<GenerateShopUiState> = _uiState.asStateFlow()

    /**
     * Updates the selected shop type. Pass null to clear selection (random).
     */
    fun selectType(type: ShopType?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    /**
     * Generates a shop with the currently selected type (or random if none).
     * On success, sets [GenerateShopUiState.generatedShopId] to trigger navigation.
     */
    fun generate() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val shopId = generateShopUseCase(_uiState.value.selectedType)
                _uiState.update { it.copy(isLoading = false, generatedShopId = shopId) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiError(message = e.message ?: "Failed to generate shop"),
                    )
                }
            }
        }
    }

    /**
     * Called after navigation to the shop detail has been handled,
     * to reset the navigation trigger.
     */
    fun onNavigated() {
        _uiState.update { it.copy(generatedShopId = null) }
    }
}
