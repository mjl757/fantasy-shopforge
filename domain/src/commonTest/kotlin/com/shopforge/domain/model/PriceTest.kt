package com.shopforge.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PriceTest {

    // ---- Construction ----

    @Test
    fun `stores amount and denomination`() {
        val price = Price(15, Denomination.Gold)
        assertEquals(15, price.amount)
        assertEquals(Denomination.Gold, price.denomination)
    }

    @Test
    fun `negative amount throws`() {
        assertFailsWith<IllegalArgumentException> {
            Price(-1, Denomination.Gold)
        }
    }

    @Test
    fun `zero amount is allowed`() {
        val price = Price(0, Denomination.Copper)
        assertEquals(0, price.amount)
    }

    @Test
    fun `ZERO constant`() {
        assertEquals(0, Price.ZERO.amount)
        assertEquals(Denomination.Gold, Price.ZERO.denomination)
    }

    // ---- toCopperValue() ----

    @Test
    fun `toCopperValue for copper`() {
        assertEquals(5L, Price(5, Denomination.Copper).toCopperValue())
    }

    @Test
    fun `toCopperValue for silver`() {
        assertEquals(30L, Price(3, Denomination.Silver).toCopperValue())
    }

    @Test
    fun `toCopperValue for gold`() {
        assertEquals(1500L, Price(15, Denomination.Gold).toCopperValue())
    }

    @Test
    fun `toCopperValue for platinum`() {
        assertEquals(5000L, Price(5, Denomination.Platinum).toCopperValue())
    }

    // ---- format() ----

    @Test
    fun `format copper`() {
        assertEquals("1 CP", Price(1, Denomination.Copper).format())
        assertEquals("9 CP", Price(9, Denomination.Copper).format())
    }

    @Test
    fun `format silver`() {
        assertEquals("1 SP", Price(1, Denomination.Silver).format())
        assertEquals("5 SP", Price(5, Denomination.Silver).format())
    }

    @Test
    fun `format gold`() {
        assertEquals("1 GP", Price(1, Denomination.Gold).format())
        assertEquals("15 GP", Price(15, Denomination.Gold).format())
    }

    @Test
    fun `format platinum`() {
        assertEquals("1 PP", Price(1, Denomination.Platinum).format())
        assertEquals("100 PP", Price(100, Denomination.Platinum).format())
    }

    @Test
    fun `format zero`() {
        assertEquals("0 GP", Price.ZERO.format())
    }

    @Test
    fun `format uses comma separators for large numbers`() {
        assertEquals("1,500 GP", Price(1_500, Denomination.Gold).format())
        assertEquals("25,000 GP", Price(25_000, Denomination.Gold).format())
        assertEquals("1,000,000 GP", Price(1_000_000, Denomination.Gold).format())
    }

    @Test
    fun `toString delegates to format`() {
        assertEquals("15 GP", Price(15, Denomination.Gold).toString())
    }

    // ---- compareTo ----

    @Test
    fun `compareTo same denomination`() {
        assertTrue(Price(5, Denomination.Gold) < Price(10, Denomination.Gold))
        assertTrue(Price(10, Denomination.Gold) > Price(5, Denomination.Gold))
        assertEquals(0, Price(5, Denomination.Gold).compareTo(Price(5, Denomination.Gold)))
    }

    @Test
    fun `compareTo cross denomination`() {
        // 1 GP (100 CP) > 9 SP (90 CP)
        assertTrue(Price(1, Denomination.Gold) > Price(9, Denomination.Silver))
        // 10 SP (100 CP) == 1 GP (100 CP)
        assertEquals(0, Price(10, Denomination.Silver).compareTo(Price(1, Denomination.Gold)))
        // 1 PP (1000 CP) > 9 GP (900 CP)
        assertTrue(Price(1, Denomination.Platinum) > Price(9, Denomination.Gold))
    }
}
