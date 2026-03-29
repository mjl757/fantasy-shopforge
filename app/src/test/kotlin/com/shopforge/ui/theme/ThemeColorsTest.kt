package com.shopforge.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests verifying the fantasy-themed color palette values.
 *
 * These tests check that named color constants match the intended hex values and
 * that light/dark palette colors are distinct where expected.
 */
class ThemeColorsTest {

    @Test
    fun `Gold matches dark goldenrod hex`() {
        // 0xFFB8860B — dark goldenrod, alpha=FF (opaque)
        assertEquals(Color(0xFFB8860B), Gold)
    }

    @Test
    fun `Parchment matches warm off-white hex`() {
        // 0xFFF5E6C8 — warm parchment
        assertEquals(Color(0xFFF5E6C8), Parchment)
    }

    @Test
    fun `BrownDark matches very dark brown hex`() {
        // 0xFF3E1F00 — very dark brown
        assertEquals(Color(0xFF3E1F00), BrownDark)
    }

    @Test
    fun `GoldLight matches bright gold hex`() {
        // 0xFFFFD700 — bright gold / yellow
        assertEquals(Color(0xFFFFD700), GoldLight)
    }

    @Test
    fun `Gold and Parchment are different colors`() {
        assertNotEquals(Gold, Parchment)
    }

    @Test
    fun `Gold and GoldLight are different shades`() {
        assertNotEquals(Gold, GoldLight)
    }

    @Test
    fun `BrownDark and BrownMid are different shades`() {
        assertNotEquals(BrownDark, BrownMid)
    }

    @Test
    fun `LightColorScheme primary is Gold`() {
        assertEquals(Gold, LightColorScheme.primary)
    }

    @Test
    fun `LightColorScheme background is Parchment`() {
        assertEquals(Parchment, LightColorScheme.background)
    }

    @Test
    fun `DarkColorScheme primary is GoldLight`() {
        assertEquals(GoldLight, DarkColorScheme.primary)
    }

    @Test
    fun `DarkColorScheme background is BrownDark`() {
        assertEquals(BrownDark, DarkColorScheme.background)
    }

    @Test
    fun `light and dark primary colors are different`() {
        assertNotEquals(LightColorScheme.primary, DarkColorScheme.primary)
    }

    @Test
    fun `light and dark backgrounds are different`() {
        assertNotEquals(LightColorScheme.background, DarkColorScheme.background)
    }
}
