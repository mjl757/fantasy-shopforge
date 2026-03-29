package com.shopforge.domain.model

/**
 * Represents a fantasy shop managed by the Game Master.
 *
 * @param id Unique identifier for the shop.
 * @param name Display name of the shop (e.g. "The Gilded Anvil").
 * @param type The category of shop, which drives inventory generation.
 * @param description Optional flavor text or notes about this shop.
 * @param createdAt Epoch milliseconds when the shop was created.
 * @param updatedAt Epoch milliseconds when the shop was last modified.
 */
data class Shop(
    val id: Long,
    val name: String,
    val type: ShopType,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
