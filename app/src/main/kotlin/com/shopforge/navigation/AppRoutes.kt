package com.shopforge.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Fantasy ShopForge app.
 *
 * Each sealed-class/object corresponds to a screen destination and carries any
 * required navigation arguments as constructor parameters.  Jetpack Navigation
 * Compose 2.8+ uses @Serializable for type-safe argument passing.
 */
sealed interface AppRoute {

    /** Main screen — list of all saved shops. */
    @Serializable
    data object ShopList : AppRoute

    /** View a shop's details and manage its inventory. */
    @Serializable
    data class ShopDetail(val shopId: String) : AppRoute

    /** Manually create a new shop. */
    @Serializable
    data object CreateShop : AppRoute

    /** Edit an existing shop's metadata or regenerate its inventory. */
    @Serializable
    data class EditShop(val shopId: String) : AppRoute

    /** Auto-generate a complete shop (name + inventory). */
    @Serializable
    data object GenerateShop : AppRoute

    /** Browse the item catalog to add items to a specific shop. */
    @Serializable
    data class AddItemToShop(val shopId: String) : AppRoute
}
