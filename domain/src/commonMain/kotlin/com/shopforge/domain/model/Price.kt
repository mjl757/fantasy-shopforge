package com.shopforge.domain.model

/**
 * Represents a monetary value in the D&D standard currency system.
 * Stored internally as copper pieces (CP) for precision.
 *
 * Conversion rates:
 *   1 PP = 10 GP
 *   1 GP = 10 SP
 *   1 SP = 10 CP
 *   Therefore: 1 PP = 1000 CP, 1 GP = 100 CP, 1 SP = 10 CP
 */
data class Price(val copperPieces: Long) : Comparable<Price> {

    init {
        require(copperPieces >= 0) { "Price cannot be negative. Got: $copperPieces" }
    }

    companion object {
        const val CP_PER_SP = 10L
        const val CP_PER_GP = 100L
        const val CP_PER_PP = 1_000L
        const val PP_THRESHOLD_GP = 25_000L

        fun ofCopper(cp: Long) = Price(cp)
        fun ofSilver(sp: Long) = Price(sp * CP_PER_SP)
        fun ofGold(gp: Long) = Price(gp * CP_PER_GP)
        fun ofPlatinum(pp: Long) = Price(pp * CP_PER_PP)

        val ZERO = Price(0L)
    }

    /** Platinum pieces component (leftover after dividing). */
    val platinumPieces: Long get() = copperPieces / CP_PER_PP

    /** Gold pieces component (after extracting PP). */
    val goldPieces: Long get() = (copperPieces % CP_PER_PP) / CP_PER_GP

    /** Silver pieces component (after extracting PP and GP). */
    val silverPieces: Long get() = (copperPieces % CP_PER_GP) / CP_PER_SP

    /** Remaining copper pieces component (after extracting PP, GP, and SP). */
    val remainingCopperPieces: Long get() = copperPieces % CP_PER_SP

    /**
     * Formats the price as a single denomination, the way games typically display prices.
     *
     * Rules:
     *   - >= 1 GP (100 CP): round up to nearest GP, display as GP
     *   - >= 1 SP (10 CP) but < 1 GP: round to nearest SP, display as SP
     *   - < 1 SP (< 10 CP): display as CP
     *   - PP is only used when the value exceeds 25,000 GP AND divides evenly into PP
     */
    fun format(): String {
        if (copperPieces == 0L) return "0 CP"

        // GP value rounded up
        val gpRoundedUp = (copperPieces + CP_PER_GP - 1) / CP_PER_GP

        if (copperPieces >= CP_PER_GP) {
            // Use PP only if > 25,000 GP and evenly divisible by PP
            if (gpRoundedUp > PP_THRESHOLD_GP && copperPieces % CP_PER_PP == 0L) {
                val pp = copperPieces / CP_PER_PP
                return "${formatNumber(pp)} PP"
            }
            return "${formatNumber(gpRoundedUp)} GP"
        }

        if (copperPieces >= CP_PER_SP) {
            val sp = (copperPieces + CP_PER_SP - 1) / CP_PER_SP
            return "$sp SP"
        }

        return "$copperPieces CP"
    }

    private fun formatNumber(value: Long): String {
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

    /**
     * Returns the price expressed as a decimal gold piece value (e.g. 150 CP -> 1.5).
     */
    fun toGoldDecimal(): Double = copperPieces.toDouble() / CP_PER_GP

    operator fun plus(other: Price): Price = Price(copperPieces + other.copperPieces)
    operator fun minus(other: Price): Price = Price(maxOf(0L, copperPieces - other.copperPieces))
    operator fun times(factor: Int): Price = Price(copperPieces * factor)
    override operator fun compareTo(other: Price): Int = copperPieces.compareTo(other.copperPieces)

    override fun toString(): String = format()
}
