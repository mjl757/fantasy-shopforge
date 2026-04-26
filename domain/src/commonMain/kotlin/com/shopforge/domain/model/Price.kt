package com.shopforge.domain.model

/**
 * Denomination of currency in the D&D standard currency system.
 *
 * Conversion rates (for comparison only):
 *   1 PP = 10 GP = 100 SP = 1000 CP
 */
enum class Denomination(val abbreviation: String, val copperValue: Int) {
    Copper("CP", 1),
    Silver("SP", 10),
    Gold("GP", 100),
    Platinum("PP", 1_000),
}

/**
 * Represents a monetary value as a whole number in a single [Denomination].
 */
data class Price(val amount: Int, val denomination: Denomination) : Comparable<Price> {

    init {
        require(amount >= 0) { "Price cannot be negative. Got: $amount" }
    }

    companion object {
        val ZERO = Price(0, Denomination.Gold)
    }

    /** Equivalent value in copper pieces, used for comparison. */
    fun toCopperValue(): Long = amount.toLong() * denomination.copperValue

    /**
     * Formats the price as a human-readable string, e.g. "15 GP", "1,500 GP".
     */
    fun format(): String = "${formatNumber(amount)} ${denomination.abbreviation}"

    private fun formatNumber(value: Int): String {
        if (value < 1_000) return value.toString()
        val str = value.toString()
        val result = StringBuilder()
        var count = 0
        for (i in str.lastIndex downTo 0) {
            if (count > 0 && count % 3 == 0) result.insert(0, ',')
            result.insert(0, str[i])
            count++
        }
        return result.toString()
    }

    override operator fun compareTo(other: Price): Int =
        toCopperValue().compareTo(other.toCopperValue())

    override fun toString(): String = format()
}
