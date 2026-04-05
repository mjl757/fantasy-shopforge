package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository
import kotlin.random.Random

/**
 * Use case that generates a complete shop with a thematic name and
 * randomized inventory based on the shop's type.
 *
 * Delegates inventory generation to [GenerateInventoryUseCase] to ensure
 * the correct rarity distribution (70/15/10/4/1), price variance, and
 * quantity logic (including unlimited-stock items) are applied consistently.
 */
class GenerateShopUseCase(
    private val shopRepository: ShopRepository,
    private val generateInventoryUseCase: GenerateInventoryUseCase,
    private val random: Random = Random.Default,
    private val clock: () -> Long,
) {

    /**
     * Generates a shop with the given [shopType], or a random type if null.
     * Returns the ID of the newly created shop.
     */
    suspend operator fun invoke(shopType: ShopType? = null): Long {
        val type = shopType ?: ShopType.entries.random(random)
        val name = generateShopName(type)
        val now = clock()

        val shop = Shop(
            id = 0, // Will be assigned by the database
            name = name,
            type = type,
            description = null,
            createdAt = now,
            updatedAt = now,
        )

        val shopId = shopRepository.createShop(shop)
        shopRepository.replaceInventory(shopId, generateInventoryUseCase(type))

        return shopId
    }

    /**
     * Generates a thematic shop name based on the shop type.
     */
    internal fun generateShopName(type: ShopType): String {
        val prefixes = listOf(
            "The Golden", "The Silver", "The Iron", "The Gilded",
            "The Rusty", "The Gleaming", "The Ancient", "The Humble",
            "The Grand", "The Crimson", "The Emerald", "The Sapphire",
            "The Mystic", "The Wandering", "The Enchanted", "The Stalwart",
        )

        val suffixes = when (type) {
            ShopType.Blacksmith -> listOf("Anvil", "Forge", "Hammer", "Blade", "Armory")
            ShopType.MagicShop -> listOf("Emporium", "Arcanum", "Sanctum", "Spire", "Wand")
            ShopType.GeneralStore -> listOf("Provisions", "Outfitter", "Supply", "Goods", "Market")
            ShopType.Alchemist -> listOf("Cauldron", "Flask", "Apothecary", "Elixir", "Remedy")
            ShopType.Fletcher -> listOf("Quiver", "Bowyer", "Arrow", "Mark", "Range")
            ShopType.Tavern -> listOf("Flagon", "Hearth", "Tankard", "Inn", "Rest")
            ShopType.Temple -> listOf("Shrine", "Sanctum", "Blessing", "Chapel", "Grace")
            ShopType.ExoticGoods -> listOf("Curiosity", "Wonder", "Bazaar", "Rarity", "Oddity")
        }

        val prefix = prefixes.random(random)
        val suffix = suffixes.random(random)
        return "$prefix $suffix"
    }

}
