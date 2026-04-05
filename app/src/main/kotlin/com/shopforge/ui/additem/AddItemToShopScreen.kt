package com.shopforge.ui.additem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity

/**
 * Screen for browsing the item catalog and adding items to a shop's inventory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToShopScreen(
    uiState: AddItemToShopUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (ItemCategory?) -> Unit,
    onItemTap: (Item) -> Unit,
    onAddConfirmed: (quantity: Int?) -> Unit,
    onAddDismissed: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Keyed on error.id so repeated identical messages still trigger the snackbar.
    LaunchedEffect(uiState.error?.id) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Item to Shop") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            TextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics { contentDescription = "Search items" },
                placeholder = { Text("Search items...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") },
                )
                ItemCategory.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(categoryDisplayName(category)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No items found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(
                            items = uiState.filteredItems,
                            key = { it.id },
                        ) { item ->
                            val isAdded = item.id in uiState.addedItemIds
                            CatalogItemRow(
                                item = item,
                                isAdded = isAdded,
                                onTap = { onItemTap(item) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Quantity picker dialog — shown when the ViewModel has a pending item selected.
    // Dialog state (quantity, isUnlimited) is local since it resets on each open.
    uiState.pendingAddItem?.let { item ->
        QuantityPickerDialog(
            itemName = item.name,
            onConfirm = onAddConfirmed,
            onDismiss = onAddDismissed,
        )
    }
}

/**
 * A single item row in the catalog list.
 */
@Composable
private fun CatalogItemRow(
    item: Item,
    isAdded: Boolean,
    onTap: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onTap),
        headlineContent = {
            Text(text = item.name)
        },
        supportingContent = {
            Text(
                text = "${categoryDisplayName(item.category)} \u2022 ${item.price.format()}",
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RarityBadge(rarity = item.rarity)
                Spacer(modifier = Modifier.width(8.dp))
                if (isAdded) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Already added",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
    )
}

/**
 * A small colored badge indicating the item's rarity.
 */
@Composable
private fun RarityBadge(rarity: Rarity) {
    val color = when (rarity) {
        Rarity.Common -> MaterialTheme.colorScheme.outline
        Rarity.Uncommon -> MaterialTheme.colorScheme.tertiary
        Rarity.Rare -> MaterialTheme.colorScheme.primary
        Rarity.VeryRare -> MaterialTheme.colorScheme.secondary
        Rarity.Legendary -> MaterialTheme.colorScheme.error
    }
    Text(
        text = rarityDisplayName(rarity),
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}

/**
 * Dialog for selecting the quantity of an item to add.
 * Supports a specific number (default 1) or unlimited stock.
 *
 * Note: the minimum quantity the dialog produces is 1. The [AddItemToShopUseCase]
 * allows quantity >= 0, so quantity = 0 cannot be expressed here by design.
 */
@Composable
private fun QuantityPickerDialog(
    itemName: String,
    onConfirm: (quantity: Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var quantity by remember { mutableIntStateOf(1) }
    var isUnlimited by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add $itemName") },
        text = {
            Column {
                Text("Select quantity:")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Decrement button: minimum is 1 (dialog enforces quantity > 0)
                    TextButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = !isUnlimited && quantity > 1,
                    ) {
                        Text("-")
                    }
                    Text(
                        text = if (isUnlimited) "\u221E" else quantity.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    // Increment button
                    TextButton(
                        onClick = { quantity++ },
                        enabled = !isUnlimited,
                    ) {
                        Text("+")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = isUnlimited,
                    onClick = { isUnlimited = !isUnlimited },
                    label = { Text("Unlimited (\u221E)") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(if (isUnlimited) null else quantity) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/** Returns a user-friendly display name for the given [ItemCategory]. */
internal fun categoryDisplayName(category: ItemCategory): String = when (category) {
    ItemCategory.Weapon -> "Weapons"
    ItemCategory.Armor -> "Armor"
    ItemCategory.Potion -> "Potions"
    ItemCategory.AdventuringGear -> "Adventuring Gear"
    ItemCategory.MagicItem -> "Magic Items"
    ItemCategory.Food -> "Food & Drink"
    ItemCategory.HolyItem -> "Holy Items"
    ItemCategory.ExoticItem -> "Exotic Items"
    ItemCategory.Ammunition -> "Ammunition"
    ItemCategory.AlchemicalSupply -> "Alchemical Supplies"
}

/** Returns a user-friendly display name for the given [Rarity]. */
internal fun rarityDisplayName(rarity: Rarity): String = when (rarity) {
    Rarity.Common -> "Common"
    Rarity.Uncommon -> "Uncommon"
    Rarity.Rare -> "Rare"
    Rarity.VeryRare -> "Very Rare"
    Rarity.Legendary -> "Legendary"
}
