package com.shopforge.ui.screen.editshop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shopforge.ui.screen.createshop.ShopTypeDropdown

/**
 * Edit Shop screen — pre-filled form for modifying an existing shop,
 * with options to regenerate inventory or delete the shop.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopScreen(
    viewModel: EditShopViewModel,
    onNavigateBack: () -> Unit,
    onShopDeleted: () -> Unit = onNavigateBack,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditShopEvent.ShopUpdated -> onNavigateBack()
                EditShopEvent.InventoryRegenerated -> { /* stay on screen */ }
                EditShopEvent.ShopDeleted -> onShopDeleted()
            }
        }
    }

    // Regenerate Inventory confirmation dialog
    if (state.showRegenerateConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRegenerateConfirmation,
            title = { Text("Regenerate Inventory") },
            text = { Text("This will replace all current inventory. Continue?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRegenerateInventory) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRegenerateConfirmation) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Shop confirmation dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            title = { Text("Delete Shop") },
            text = { Text("Are you sure you want to delete this shop? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDeleteShop,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirmation) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Shop") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Shop Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Shop Name") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Shop Type Dropdown
                ShopTypeDropdown(
                    selectedType = state.selectedType,
                    onTypeSelected = viewModel::onTypeSelected,
                )

                // Description
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Description (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = viewModel::saveShop,
                    enabled = state.isValid && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save Changes")
                }

                // Regenerate Inventory Button
                OutlinedButton(
                    onClick = viewModel::requestRegenerateInventory,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Regenerate Inventory")
                }

                // Delete Shop Button (destructive)
                Button(
                    onClick = viewModel::requestDeleteShop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete Shop")
                }
            }
        }
    }
}
