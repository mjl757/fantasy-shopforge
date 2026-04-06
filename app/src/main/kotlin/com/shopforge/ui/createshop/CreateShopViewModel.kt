package com.shopforge.ui.createshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.CreateShopUseCase
import com.shopforge.domain.usecase.GenerateShopNameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Create Shop screen.
 *
 * Manages form state for name, type, and description. Supports
 * generating a thematic name and saving the shop.
 */
class CreateShopViewModel(
    private val createShopUseCase: CreateShopUseCase,
    private val generateShopNameUseCase: GenerateShopNameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateShopUiState())
    val uiState: StateFlow<CreateShopUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateShopEvent>()
    val events: SharedFlow<CreateShopEvent> = _events.asSharedFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onTypeSelected(type: ShopType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun generateName() {
        val type = _uiState.value.selectedType ?: return
        val name = generateShopNameUseCase(type)
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun saveShop() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update {
                it.copy(
                    nameError = if (state.name.isBlank()) "Shop name is required" else null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val shopId = createShopUseCase(
                    name = state.name,
                    type = state.selectedType!!,
                    description = state.description.ifBlank { null },
                )
                _events.emit(CreateShopEvent.ShopCreated(shopId))
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = e.message) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

/**
 * UI state for the Create Shop form.
 */
data class CreateShopUiState(
    val name: String = "",
    val selectedType: ShopType? = null,
    val description: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && selectedType != null
}

/**
 * One-shot navigation events from the Create Shop screen.
 */
sealed interface CreateShopEvent {
    data class ShopCreated(val shopId: Long) : CreateShopEvent
}
