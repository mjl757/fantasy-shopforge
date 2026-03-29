package com.shopforge.ui.screen.editshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.usecase.DeleteShopUseCase
import com.shopforge.domain.usecase.RegenerateInventoryUseCase
import com.shopforge.domain.usecase.UpdateShopUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Edit Shop screen.
 *
 * Loads existing shop data, supports editing name/type/description,
 * regenerating inventory (with confirmation), and deleting the shop
 * (with confirmation).
 */
class EditShopViewModel(
    private val shopId: Long,
    private val updateShopUseCase: UpdateShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val regenerateInventoryUseCase: RegenerateInventoryUseCase,
    private val getShopFlow: (Long) -> kotlinx.coroutines.flow.Flow<Shop?>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditShopUiState())
    val uiState: StateFlow<EditShopUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditShopEvent>()
    val events: SharedFlow<EditShopEvent> = _events.asSharedFlow()

    /** The originally loaded shop, used for the update operation. */
    private var originalShop: Shop? = null

    init {
        loadShop()
    }

    private fun loadShop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shop = getShopFlow(shopId).filterNotNull().first()
                originalShop = shop
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = shop.name,
                        selectedType = shop.type,
                        description = shop.description ?: "",
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, nameError = "Failed to load shop") }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onTypeSelected(type: ShopType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun saveShop() {
        val state = _uiState.value
        val shop = originalShop ?: return

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
                updateShopUseCase(
                    existingShop = shop,
                    name = state.name,
                    type = state.selectedType!!,
                    description = state.description.ifBlank { null },
                )
                _events.emit(EditShopEvent.ShopUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, nameError = e.message) }
            }
        }
    }

    // ---- Regenerate Inventory ----

    fun requestRegenerateInventory() {
        _uiState.update { it.copy(showRegenerateConfirmation = true) }
    }

    fun dismissRegenerateConfirmation() {
        _uiState.update { it.copy(showRegenerateConfirmation = false) }
    }

    fun confirmRegenerateInventory() {
        _uiState.update { it.copy(showRegenerateConfirmation = false) }
        val type = _uiState.value.selectedType ?: return

        viewModelScope.launch {
            try {
                regenerateInventoryUseCase(shopId, type)
                _events.emit(EditShopEvent.InventoryRegenerated)
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = "Failed to regenerate inventory") }
            }
        }
    }

    // ---- Delete Shop ----

    fun requestDeleteShop() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDeleteShop() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }

        viewModelScope.launch {
            try {
                deleteShopUseCase(shopId)
                _events.emit(EditShopEvent.ShopDeleted)
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = "Failed to delete shop") }
            }
        }
    }
}

/**
 * UI state for the Edit Shop form.
 */
data class EditShopUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val selectedType: ShopType? = null,
    val description: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val showRegenerateConfirmation: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && selectedType != null
}

/**
 * One-shot navigation events from the Edit Shop screen.
 */
sealed interface EditShopEvent {
    data object ShopUpdated : EditShopEvent
    data object InventoryRegenerated : EditShopEvent
    data object ShopDeleted : EditShopEvent
}
