package com.shopforge.domain.model

/**
 * The rarity tier of an item, primarily used for magic items.
 * Drives generation weights during inventory creation.
 */
enum class Rarity {
    Common,
    Uncommon,
    Rare,
    VeryRare,
    Legendary,
}
