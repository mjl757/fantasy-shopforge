package com.shopforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.shopforge.navigation.ShopForgeNavHost
import com.shopforge.ui.theme.ShopForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopForgeTheme {
                ShopForgeNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
