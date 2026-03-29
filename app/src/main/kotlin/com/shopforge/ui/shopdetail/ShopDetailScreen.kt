package com.shopforge.ui.shopdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem

/**
 * Shop Detail screen displaying shop info and inventory.
 * Supports search filtering and tap-to-decrement for session reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    viewModel: ShopDetailViewModel,
    onNavigateBack: () -> Unit = {},
    onEditShop: (Long) -> Unit = {},
    onAddItem: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val s = state) {
                            is ShopDetailUiState.Loaded -> s.shop.name
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
        floatingActionButton = {
            val loadedState = state as? ShopDetailUiState.Loaded
            if (loadedState != null) {
                FloatingActionButton(onClick = { onAddItem(loadedState.shop.id) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add item",
                    )
                }
            }
        },
    ) { padding ->
        when (val s = state) {
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
                    state = s,
                    onSearchQueryChange = viewModel::searchInventory,
                    onDecrementQuantity = viewModel::decrementQuantity,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ShopDetailContent(
    state: ShopDetailUiState.Loaded,
    onSearchQueryChange: (String) -> Unit,
    onDecrementQuantity: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                    onTapQuantity = { onDecrementQuantity(inventoryItem.item.id) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
    onTapQuantity: () -> Unit,
) {
    val isSoldOut = inventoryItem.isSoldOut
    val alpha = if (isSoldOut) 0.5f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(alpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Item info (left side)
        Column(modifier = Modifier.weight(1f)) {
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

        // Quantity (right side, tappable)
        QuantityDisplay(
            quantity = inventoryItem.quantity,
            isSoldOut = isSoldOut,
            onClick = onTapQuantity,
        )
    }
}

@Composable
private fun QuantityDisplay(
    quantity: Int?,
    isSoldOut: Boolean,
    onClick: () -> Unit,
) {
    val displayText = when {
        quantity == null -> "\u221E" // infinity symbol
        isSoldOut -> "0"
        else -> quantity.toString()
    }

    Surface(
        modifier = Modifier
            .clickable(enabled = quantity != null && !isSoldOut, onClick = onClick),
        color = when {
            isSoldOut -> MaterialTheme.colorScheme.errorContainer
            quantity == null -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelLarge,
            color = when {
                isSoldOut -> MaterialTheme.colorScheme.onErrorContainer
                quantity == null -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
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
