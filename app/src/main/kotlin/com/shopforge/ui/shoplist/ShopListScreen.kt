package com.shopforge.ui.shoplist

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType

/**
 * Shop List screen — the app's home screen showing all saved shops.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopListScreen(
    uiState: ShopListUiState,
    onShopClick: (Long) -> Unit,
    onCreateShopClick: () -> Unit,
    onGenerateShopClick: () -> Unit,
    onFilterSelected: (ShopType?) -> Unit,
    onSortOrderChanged: (ShopSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Fantasy ShopForge") },
                actions = {
                    TextButton(onClick = onGenerateShopClick) {
                        Text("Generate")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateShopClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Shop",
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is ShopListUiState.Loading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is ShopListUiState.Empty -> EmptyContent(
                onCreateShopClick = onCreateShopClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is ShopListUiState.Content -> ShopListContent(
                shops = uiState.shops,
                selectedFilter = uiState.selectedFilter,
                sortOrder = uiState.sortOrder,
                onShopClick = onShopClick,
                onFilterSelected = onFilterSelected,
                onSortOrderChanged = onSortOrderChanged,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(
    onCreateShopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No shops yet",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first shop!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onCreateShopClick) {
            Text("Create Shop")
        }
    }
}

@Composable
private fun ShopListContent(
    shops: List<Shop>,
    selectedFilter: ShopType?,
    sortOrder: ShopSortOrder,
    onShopClick: (Long) -> Unit,
    onFilterSelected: (ShopType?) -> Unit,
    onSortOrderChanged: (ShopSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ShopFilterRow(
            selectedFilter = selectedFilter,
            sortOrder = sortOrder,
            onFilterSelected = onFilterSelected,
            onSortOrderChanged = onSortOrderChanged,
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(shops, key = { it.id }) { shop ->
                ShopCard(
                    shop = shop,
                    onClick = { onShopClick(shop.id) },
                )
            }
        }
    }
}

@Composable
private fun ShopFilterRow(
    selectedFilter: ShopType?,
    sortOrder: ShopSortOrder,
    onFilterSelected: (ShopType?) -> Unit,
    onSortOrderChanged: (ShopSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("All") },
                )
            }
            items(ShopType.entries.toList()) { shopType ->
                FilterChip(
                    selected = selectedFilter == shopType,
                    onClick = { onFilterSelected(shopType) },
                    label = { Text(shopType.displayName()) },
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Text(
                    text = when (sortOrder) {
                        ShopSortOrder.Name -> "AZ"
                        ShopSortOrder.CreatedDate -> "New"
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Sort by Name") },
                    onClick = {
                        onSortOrderChanged(ShopSortOrder.Name)
                        showSortMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Sort by Date") },
                    onClick = {
                        onSortOrderChanged(ShopSortOrder.CreatedDate)
                        showSortMenu = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ShopCard(
    shop: Shop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shop.type.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            val description = shop.description
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Returns a human-readable display name for this [ShopType].
 */
fun ShopType.displayName(): String = when (this) {
    ShopType.Blacksmith -> "Blacksmith"
    ShopType.MagicShop -> "Magic Shop"
    ShopType.GeneralStore -> "General Store"
    ShopType.Alchemist -> "Alchemist"
    ShopType.Fletcher -> "Fletcher"
    ShopType.Tavern -> "Tavern"
    ShopType.Temple -> "Temple"
    ShopType.ExoticGoods -> "Exotic Goods"
}
