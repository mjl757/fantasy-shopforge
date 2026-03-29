package com.shopforge.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ---------------------------------------------------------------------------
// Light color scheme — parchment backgrounds, gold accents, brown surfaces
// ---------------------------------------------------------------------------
internal val LightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = Color.White,
    primaryContainer = GoldContainer,
    onPrimaryContainer = BrownDark,

    secondary = BrownMid,
    onSecondary = Parchment,
    secondaryContainer = ParchmentDark,
    onSecondaryContainer = BrownDark,

    tertiary = BrownLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC8),
    onTertiaryContainer = Color(0xFF330E00),

    background = Parchment,
    onBackground = BrownDark,

    surface = Color(0xFFFAF0DC),
    onSurface = BrownDark,
    surfaceVariant = Color(0xFFEDD9A3),
    onSurfaceVariant = BrownMid,

    error = BurgundyLight,
    onError = Color.White,
)

// ---------------------------------------------------------------------------
// Dark color scheme — deep browns, muted golds, dim parchment
// ---------------------------------------------------------------------------
internal val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = BrownDark,
    primaryContainer = Color(0xFF5C4200),
    onPrimaryContainer = GoldContainer,

    secondary = ParchmentDark,
    onSecondary = BrownDark,
    secondaryContainer = Color(0xFF4A2C1A),
    onSecondaryContainer = ParchmentDark,

    tertiary = Color(0xFFFFB77C),
    onTertiary = Color(0xFF4A1800),
    tertiaryContainer = Color(0xFF6B2B00),
    onTertiaryContainer = Color(0xFFFFDCC8),

    background = BrownDark,
    onBackground = Parchment,

    surface = Color(0xFF2A1200),
    onSurface = Parchment,
    surfaceVariant = Color(0xFF4A3020),
    onSurfaceVariant = ParchmentDark,

    error = Color(0xFFFF8A80),
    onError = BurgundyDark,
)

// ---------------------------------------------------------------------------
// Theme composable
// ---------------------------------------------------------------------------

@Composable
fun ShopForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is opt-in; falls back to the fantasy palette when disabled.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShopForgeTypography,
        content = content,
    )
}
