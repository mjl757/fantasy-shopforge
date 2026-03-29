package com.shopforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shop List screen — start destination.
 *
 * Displays all saved shops. Empty-state placeholder; full implementation in issue #10.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopListScreen(
    onShopClick: (shopId: String) -> Unit,
    onCreateShop: () -> Unit,
    onGenerateShop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("My Shops") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("No shops yet.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCreateShop) {
                Text("New Shop")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGenerateShop) {
                Text("Generate Shop")
            }
        }
    }
}
