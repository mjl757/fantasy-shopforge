package com.shopforge.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PriceTest {

    // ---- Construction helpers ----

    @Test
    fun `ofCopper stores raw value`() {
        val price = Price.ofCopper(150)
        assertEquals(150L, price.copperPieces)
    }

    @Test
    fun `ofSilver converts to copper`() {
        val price = Price.ofSilver(3)
        assertEquals(30L, price.copperPieces)
    }

    @Test
    fun `ofGold converts to copper`() {
        val price = Price.ofGold(2)
        assertEquals(200L, price.copperPieces)
    }

    @Test
    fun `ofPlatinum converts to copper`() {
        val price = Price.ofPlatinum(1)
        assertEquals(1000L, price.copperPieces)
    }

    @Test
    fun `negative price throws`() {
        assertFailsWith<IllegalArgumentException> {
            Price(-1)
        }
    }

    @Test
    fun `ZERO is 0 copper`() {
        assertEquals(0L, Price.ZERO.copperPieces)
    }

    // ---- Denomination breakdown ----

    @Test
    fun `breakdown of 1234 CP`() {
        // 1234 CP = 1 PP (1000 CP) + 2 GP (200 CP) + 3 SP (30 CP) + 4 CP
        val price = Price(1234)
        assertEquals(1L, price.platinumPieces)
        assertEquals(2L, price.goldPieces)
        assertEquals(3L, price.silverPieces)
        assertEquals(4L, price.remainingCopperPieces)
    }

    @Test
    fun `breakdown of exactly 100 CP is 1 GP`() {
        val price = Price(100)
        assertEquals(0L, price.platinumPieces)
        assertEquals(1L, price.goldPieces)
        assertEquals(0L, price.silverPieces)
        assertEquals(0L, price.remainingCopperPieces)
    }

    @Test
    fun `breakdown of 0 CP is all zeros`() {
        val price = Price(0)
        assertEquals(0L, price.platinumPieces)
        assertEquals(0L, price.goldPieces)
        assertEquals(0L, price.silverPieces)
        assertEquals(0L, price.remainingCopperPieces)
    }

    // ---- format() – single denomination display ----

    @Test
    fun `format zero is 0 CP`() {
        assertEquals("0 CP", Price.ZERO.format())
    }

    @Test
    fun `format sub-silver shows CP`() {
        assertEquals("1 CP", Price(1).format())
        assertEquals("9 CP", Price(9).format())
    }

    @Test
    fun `format exact silver shows SP`() {
        assertEquals("1 SP", Price(10).format())
        assertEquals("5 SP", Price(50).format())
    }

    @Test
    fun `format sub-gold rounds up to nearest SP`() {
        assertEquals("2 SP", Price(15).format())
        assertEquals("10 SP", Price(99).format())
    }

    @Test
    fun `format exact gold shows GP`() {
        assertEquals("1 GP", Price(100).format())
        assertEquals("15 GP", Price(1500).format())
    }

    @Test
    fun `format rounds up to nearest GP`() {
        assertEquals("2 GP", Price(150).format())
        assertEquals("13 GP", Price(1234).format())
    }

    @Test
    fun `format below PP threshold shows GP`() {
        // 25,000 GP = 2,500,000 CP – at threshold, stays GP
        assertEquals("25,000 GP", Price(2_500_000).format())
    }

    @Test
    fun `format above PP threshold and evenly divisible shows PP`() {
        // 26,000 GP = 2,600,000 CP = 2600 PP
        assertEquals("2,600 PP", Price(2_600_000).format())
        // 30,000 GP = 3,000,000 CP = 3000 PP
        assertEquals("3,000 PP", Price(3_000_000).format())
    }

    @Test
    fun `format above PP threshold but not evenly divisible shows GP`() {
        // 25,001 GP worth (not evenly divisible by PP)
        assertEquals("25,001 GP", Price(2_500_001).format())
        // 25,005 GP = 2,500,500 CP – not evenly divisible by 1000 CP (PP)
        assertEquals("25,005 GP", Price(2_500_500).format())
    }

    @Test
    fun `format uses comma separators for large numbers`() {
        assertEquals("1,500 GP", Price(150_000).format())
        // 10,000,000 CP = 100,000 GP > 25k, evenly divisible → PP
        assertEquals("10,000 PP", Price(10_000_000).format())
    }

    @Test
    fun `format 1000 CP shows GP not PP below threshold`() {
        assertEquals("10 GP", Price(1000).format())
    }

    @Test
    fun `toString delegates to format`() {
        assertEquals("1 GP", Price(100).toString())
    }

    // ---- toGoldDecimal() ----

    @Test
    fun `toGoldDecimal for 150 CP is 1 point 5`() {
        assertEquals(1.5, Price(150).toGoldDecimal(), 0.0001)
    }

    @Test
    fun `toGoldDecimal for 100 CP is 1 point 0`() {
        assertEquals(1.0, Price(100).toGoldDecimal(), 0.0001)
    }

    @Test
    fun `toGoldDecimal for 0 CP is 0 point 0`() {
        assertEquals(0.0, Price.ZERO.toGoldDecimal(), 0.0001)
    }

    // ---- Arithmetic ----

    @Test
    fun `plus adds copper pieces`() {
        val result = Price(100) + Price(50)
        assertEquals(150L, result.copperPieces)
    }

    @Test
    fun `minus subtracts copper pieces`() {
        val result = Price(100) - Price(50)
        assertEquals(50L, result.copperPieces)
    }

    @Test
    fun `times scales copper pieces`() {
        val result = Price(100) * 3
        assertEquals(300L, result.copperPieces)
    }

    @Test
    fun `compareTo orders by copper pieces`() {
        val cheap = Price(50)
        val expensive = Price(200)
        assertTrue(cheap < expensive)
        assertTrue(expensive > cheap)
        assertEquals(0, Price(100).compareTo(Price(100)))
    }
}
