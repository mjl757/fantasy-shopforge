package com.shopforge.ui.shopdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shopforge.domain.model.Denomination
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem

/**
 * Shop Detail screen displaying shop info and inventory.
 * Supports search filtering, expandable item rows with detail,
 * quantity controls, price editing, and item removal.
 */
@Composable
fun ShopDetailScreen(
    viewModel: ShopDetailViewModel,
    onNavigateBack: () -> Unit = {},
    onEditShop: (Long) -> Unit = {},
    onAddItem: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ShopDetailScreen(state, viewModel, onNavigateBack, onEditShop, onAddItem)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopDetailScreen(
    state: ShopDetailUiState,
    viewModel: ShopDetailViewModel,
    onNavigateBack: () -> Unit = {},
    onEditShop: (Long) -> Unit = {},
    onAddItem: (Long) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state) {
                            is ShopDetailUiState.Loaded -> state.shop.name
                            else -> "Shop Detail"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                actions = {
                    val loadedState = state as? ShopDetailUiState.Loaded
                    if (loadedState != null) {
                        IconButton(onClick = { onAddItem(loadedState.shop.id) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add item",
                            )
                        }
                        IconButton(onClick = { onEditShop(loadedState.shop.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit shop",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            is ShopDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ShopDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Shop not found",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            is ShopDetailUiState.Loaded -> {
                ShopDetailContent(
                    state = state,
                    onSearchQueryChange = viewModel::searchInventory,
                    onDecrementQuantity = viewModel::decrementQuantity,
                    onIncrementQuantity = viewModel::incrementQuantity,
                    onToggleExpand = viewModel::toggleExpandItem,
                    onEditPrice = viewModel::openEditSheet,
                    onRequestDeleteItem = viewModel::requestDeleteItem,
                    onDismissBottomSheet = viewModel::dismissBottomSheet,
                    onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
                    onConfirmDeleteItem = viewModel::confirmDeleteItem,
                    onSavePrice = viewModel::savePrice,
                    onClearPriceEditError = viewModel::clearPriceEditError,
                    contentPadding = padding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopDetailContent(
    state: ShopDetailUiState.Loaded,
    onSearchQueryChange: (String) -> Unit,
    onDecrementQuantity: (Long) -> Unit,
    onIncrementQuantity: (Long) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onEditPrice: (Long) -> Unit,
    onRequestDeleteItem: (Long) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmDeleteItem: () -> Unit,
    onSavePrice: (Long, String, String) -> Unit,
    onClearPriceEditError: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        // Shop header
        item {
            ShopHeader(
                name = state.shop.name,
                type = state.shop.type.name,
                description = state.shop.description,
            )
        }

        // Search bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search inventory") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // Inventory list
        if (state.inventory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.searchQuery.isNotBlank()) {
                            "No items match your search"
                        } else {
                            "No items in inventory"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(
                items = state.inventory,
                key = { it.item.id },
            ) { inventoryItem ->
                InventoryItemRow(
                    inventoryItem = inventoryItem,
                    isExpanded = state.expandedItemId == inventoryItem.item.id,
                    onToggleExpand = { onToggleExpand(inventoryItem.item.id) },
                    onEditPrice = { onEditPrice(inventoryItem.item.id) },
                    onRequestDelete = { onRequestDeleteItem(inventoryItem.item.id) },
                    onDecrement = { onDecrementQuantity(inventoryItem.item.id) },
                    onIncrement = { onIncrementQuantity(inventoryItem.item.id) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    // Edit item bottom sheet
    if (state.editingItem != null) {
        EditItemBottomSheet(
            inventoryItem = state.editingItem,
            priceEditError = state.priceEditError,
            onDismiss = onDismissBottomSheet,
            onSave = { rawAmount, denominationName ->
                onSavePrice(state.editingItem.item.id, rawAmount, denominationName)
            },
            onClearError = onClearPriceEditError,
        )
    }

    // Delete confirmation dialog
    val deleteItemName = state.inventory.find { it.item.id == state.expandedItemId }?.item?.name
    if (state.showDeleteConfirmation && deleteItemName != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirmation,
            title = { Text("Remove Item") },
            text = { Text("Remove $deleteItemName from this shop?") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDeleteItem,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteConfirmation) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemBottomSheet(
    inventoryItem: ShopInventoryItem,
    priceEditError: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onClearError: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var priceInput by rememberSaveable {
        mutableStateOf(inventoryItem.adjustedPrice.amount.toString())
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedDenomination by rememberSaveable {
        mutableStateOf(inventoryItem.adjustedPrice.denomination.name)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Adjusted Price",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        if (priceEditError != null) onClearError()
                    },
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = priceEditError != null,
                    supportingText = if (priceEditError != null) {
                        { Text(priceEditError) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.width(120.dp),
                ) {
                    OutlinedTextField(
                        value = Denomination.valueOf(selectedDenomination).abbreviation,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        Denomination.entries.forEach { denomination ->
                            DropdownMenuItem(
                                text = { Text(denomination.abbreviation) },
                                onClick = {
                                    selectedDenomination = denomination.name
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(priceInput, selectedDenomination) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun ShopHeader(
    name: String,
    type: String,
    description: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InventoryItemRow(
    inventoryItem: ShopInventoryItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditPrice: () -> Unit,
    onRequestDelete: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val isSoldOut = inventoryItem.isSoldOut
    val isUnlimited = inventoryItem.isUnlimitedStock
    val alpha = if (isSoldOut) 0.5f else 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .alpha(alpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Item info (left side)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            ) {
                Text(
                    text = inventoryItem.item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isSoldOut) TextDecoration.LineThrough else TextDecoration.None,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inventoryItem.adjustedPrice.format(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RarityBadge(rarity = inventoryItem.item.rarity)
                }
            }

            // Quantity controls (right side)
            QuantityControls(
                quantity = inventoryItem.quantity,
                isSoldOut = isSoldOut,
                isUnlimited = isUnlimited,
                onDecrement = onDecrement,
                onIncrement = onIncrement,
                modifier = Modifier.padding(end = 4.dp),
            )
        }

        // Expanded detail section
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            ) {
                inventoryItem.item.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatCategoryLabel(inventoryItem.item.category.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Edit Price",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onEditPrice)
                                .padding(4.dp),
                        )
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable(onClick = onRequestDelete)
                                .padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Inserts spaces before uppercase letters in a PascalCase string.
 * e.g. "AdventuringGear" → "Adventuring Gear"
 */
private fun formatCategoryLabel(name: String): String =
    name.replace(Regex("(?<=.)([A-Z])"), " $1")

@Composable
private fun QuantityControls(
    quantity: Int?,
    isSoldOut: Boolean,
    isUnlimited: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayText = when {
        isUnlimited -> "\u221E"
        else -> quantity?.toString() ?: "0"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = !isUnlimited && !isSoldOut,
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease quantity",
            )
        }
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelLarge,
            color = when {
                isSoldOut -> MaterialTheme.colorScheme.error
                isUnlimited -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        IconButton(
            onClick = onIncrement,
            enabled = !isUnlimited,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase quantity",
            )
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val label = when (rarity) {
        Rarity.Common -> "Common"
        Rarity.Uncommon -> "Uncommon"
        Rarity.Rare -> "Rare"
        Rarity.VeryRare -> "Very Rare"
        Rarity.Legendary -> "Legendary"
    }

    val containerColor = when (rarity) {
        Rarity.Common -> MaterialTheme.colorScheme.surfaceVariant
        Rarity.Uncommon -> MaterialTheme.colorScheme.secondaryContainer
        Rarity.Rare -> MaterialTheme.colorScheme.primaryContainer
        Rarity.VeryRare -> MaterialTheme.colorScheme.tertiaryContainer
        Rarity.Legendary -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (rarity) {
        Rarity.Common -> MaterialTheme.colorScheme.onSurfaceVariant
        Rarity.Uncommon -> MaterialTheme.colorScheme.onSecondaryContainer
        Rarity.Rare -> MaterialTheme.colorScheme.onPrimaryContainer
        Rarity.VeryRare -> MaterialTheme.colorScheme.onTertiaryContainer
        Rarity.Legendary -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
