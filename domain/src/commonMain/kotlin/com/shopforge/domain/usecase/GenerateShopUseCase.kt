package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import com.shopforge.domain.repository.ShopRepository
import kotlin.random.Random

/**
 * Use case that generates a complete shop with a thematic name and
 * randomized inventory based on the shop's type.
 *
 * Generation rules (from PRD):
 * - Inventory size: 8-15 items
 * - Rarity distribution: 70% Common, 15% Uncommon, 10% Rare, 4% Very Rare, 1% Legendary
 * - Price variance: +/-10% from base catalog price
 * - Quantity per item: 1-10 for common/uncommon, 1-3 for rare+
 */
class GenerateShopUseCase(
    private val shopRepository: ShopRepository,
    private val itemRepository: ItemRepository,
    private val random: Random = Random.Default,
    private val clock: () -> Long,
) {

    /**
     * Generates a shop with the given [shopType], or a random type if null.
     * Returns the ID of the newly created shop.
     */
    suspend fun invoke(shopType: ShopType? = null): Long {
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

        val inventory = generateInventory(type)
        shopRepository.replaceInventory(shopId, inventory)

        return shopId
    }

    /**
     * Generates a randomized inventory for the given [shopType].
     * Selects items from the catalog matching the shop type's default categories,
     * applying rarity distribution weights and price variance.
     */
    internal suspend fun generateInventory(shopType: ShopType): List<ShopInventoryItem> {
        val categories = shopType.defaultCategories
        val candidateItems = categories.flatMap { category ->
            itemRepository.getItemsByCategory(category)
        }.distinctBy { it.id }

        if (candidateItems.isEmpty()) return emptyList()

        val inventorySize = random.nextInt(8, 16) // 8-15 inclusive
        val selectedItems = selectItemsByRarity(candidateItems, inventorySize)

        return selectedItems.map { item ->
            ShopInventoryItem(
                item = item,
                quantity = generateQuantity(item.rarity),
                adjustedPrice = applyPriceVariance(item.price),
            )
        }
    }

    /**
     * Selects [count] items from [candidates] according to the rarity distribution weights.
     * If there aren't enough items of a given rarity, falls back to other rarities.
     */
    internal fun selectItemsByRarity(candidates: List<Item>, count: Int): List<Item> {
        if (candidates.size <= count) return candidates.shuffled(random)

        val byRarity = candidates.groupBy { it.rarity }
        val selected = mutableListOf<Item>()

        // Target counts based on rarity weights
        val targets = mapOf(
            Rarity.Common to (count * 0.70).toInt().coerceAtLeast(1),
            Rarity.Uncommon to (count * 0.15).toInt().coerceAtLeast(0),
            Rarity.Rare to (count * 0.10).toInt().coerceAtLeast(0),
            Rarity.VeryRare to (count * 0.04).toInt().coerceAtLeast(0),
            Rarity.Legendary to (count * 0.01).toInt().coerceAtLeast(0),
        )

        // Fill from each rarity tier
        for ((rarity, target) in targets) {
            val pool = byRarity[rarity]?.shuffled(random) ?: continue
            val toTake = target.coerceAtMost(pool.size)
            selected.addAll(pool.take(toTake))
        }

        // If we haven't reached count yet, fill from remaining candidates
        if (selected.size < count) {
            val remaining = candidates.filter { it !in selected }.shuffled(random)
            val needed = count - selected.size
            selected.addAll(remaining.take(needed))
        }

        return selected.shuffled(random).take(count)
    }

    /**
     * Applies +/-10% price variance from the base catalog price.
     */
    internal fun applyPriceVariance(basePrice: Price): Price {
        if (basePrice.copperPieces == 0L) return basePrice
        val variance = 0.90 + (random.nextDouble() * 0.20) // 0.90 to 1.10
        val adjusted = (basePrice.copperPieces * variance).toLong().coerceAtLeast(1L)
        return Price(adjusted)
    }

    /**
     * Generates a random quantity based on item rarity.
     * Common/Uncommon: 1-10, Rare+: 1-3.
     */
    internal fun generateQuantity(rarity: Rarity): Int {
        return when (rarity) {
            Rarity.Common, Rarity.Uncommon -> random.nextInt(1, 11) // 1-10
            Rarity.Rare, Rarity.VeryRare, Rarity.Legendary -> random.nextInt(1, 4) // 1-3
        }
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
