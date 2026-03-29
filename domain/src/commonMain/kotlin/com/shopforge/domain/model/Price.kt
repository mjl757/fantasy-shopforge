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
     * Formats the price as a human-readable string using only the non-zero
     * denominations. For example:
     *   1500 CP -> "1 GP, 5 SP"
     *   150 CP  -> "1 SP, 5 CP"
     *   100 CP  -> "1 GP"
     *   0 CP    -> "0 CP"
     */
    fun format(): String {
        if (copperPieces == 0L) return "0 CP"

        val parts = buildList {
            if (platinumPieces > 0) add("$platinumPieces PP")
            if (goldPieces > 0) add("$goldPieces GP")
            if (silverPieces > 0) add("$silverPieces SP")
            if (remainingCopperPieces > 0) add("$remainingCopperPieces CP")
        }

        return parts.joinToString(", ")
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
