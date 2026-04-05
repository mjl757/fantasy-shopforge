package com.shopforge.ui.generate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopforge.domain.model.ShopType

/**
 * Generate Shop screen that allows the GM to optionally select a shop type
 * and generate a complete shop with inventory.
 *
 * @param viewModel The [GenerateShopViewModel] managing this screen's state.
 * @param onShopGenerated Callback invoked with the shop ID when generation succeeds,
 *                        triggering navigation to the Shop Detail screen.
 * @param onBack Callback invoked when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GenerateShopScreen(
    viewModel: GenerateShopViewModel,
    onShopGenerated: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate on successful generation
    LaunchedEffect(uiState.generatedShopId) {
        uiState.generatedShopId?.let { shopId ->
            onShopGenerated(shopId)
            viewModel.onNavigated()
        }
    }

    // Show error in snackbar; keyed on error.id so repeated identical messages still trigger
    LaunchedEffect(uiState.error?.id) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Shop") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Choose a shop type or leave blank for random",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shop type chip group
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShopType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = {
                                if (uiState.selectedType == type) {
                                    viewModel.selectType(null)
                                } else {
                                    viewModel.selectType(type)
                                }
                            },
                            label = { Text(formatShopTypeName(type)) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Generate button — text stays visible; the animated block below shows loading feedback
                Button(
                    onClick = { viewModel.generate() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(
                        text = "Generate Shop",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                // Loading animation below button
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generating your shop...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats a [ShopType] enum name into a human-readable display string.
 * E.g., MagicShop -> "Magic Shop", ExoticGoods -> "Exotic Goods".
 */
internal fun formatShopTypeName(type: ShopType): String {
    return type.name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}
