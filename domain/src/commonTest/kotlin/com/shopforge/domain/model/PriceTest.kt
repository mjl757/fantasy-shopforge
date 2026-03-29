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

    // ---- format() ----

    @Test
    fun `format zero is 0 CP`() {
        assertEquals("0 CP", Price.ZERO.format())
    }

    @Test
    fun `format 1 CP shows only CP`() {
        assertEquals("1 CP", Price(1).format())
    }

    @Test
    fun `format 10 CP shows only 1 SP`() {
        assertEquals("1 SP", Price(10).format())
    }

    @Test
    fun `format 100 CP shows only 1 GP`() {
        assertEquals("1 GP", Price(100).format())
    }

    @Test
    fun `format 1000 CP shows only 1 PP`() {
        assertEquals("1 PP", Price(1000).format())
    }

    @Test
    fun `format 150 CP shows 1 SP 5 CP`() {
        // 150 CP = 1 GP 5 SP
        assertEquals("1 GP, 5 SP", Price(150).format())
    }

    @Test
    fun `format 15 CP shows 1 SP 5 CP`() {
        assertEquals("1 SP, 5 CP", Price(15).format())
    }

    @Test
    fun `format 1234 CP shows all denominations`() {
        assertEquals("1 PP, 2 GP, 3 SP, 4 CP", Price(1234).format())
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
