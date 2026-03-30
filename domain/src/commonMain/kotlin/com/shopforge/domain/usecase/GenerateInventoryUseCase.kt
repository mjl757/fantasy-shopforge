package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Generates a randomized inventory for a shop of a given [ShopType].
 *
 * Generation rules:
 * - Inventory size: 8-15 items (randomly determined)
 * - Items filtered by the shop type's [ShopType.defaultCategories]
 * - Rarity distribution: 70% Common, 15% Uncommon, 10% Rare, 4% Very Rare, 1% Legendary
 * - Price variance: +/-10% from the base catalog price, rounded to nearest denomination
 * - No duplicate items in a single inventory
 * - Quantity: random (1-10) or null (unlimited stock)
 */
class GenerateInventoryUseCase(
    private val itemRepository: ItemRepository,
) {

    private companion object {
        const val MIN_INVENTORY_SIZE = 8
        const val MAX_INVENTORY_SIZE = 15
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 10
        const val UNLIMITED_STOCK_CHANCE = 0.2
        const val PRICE_VARIANCE_FRACTION = 0.10

        val RARITY_WEIGHTS: Map<Rarity, Int> = mapOf(
            Rarity.Common to 70,
            Rarity.Uncommon to 15,
            Rarity.Rare to 10,
            Rarity.VeryRare to 4,
            Rarity.Legendary to 1,
        ).also { check(it.values.sum() == 100) { "Rarity weights must sum to 100" } }
    }

    /**
     * Generates an inventory for the given [shopType].
     *
     * @param shopType The type of shop to generate inventory for.
     * @param random A [Random] instance for testability (use a seeded Random for deterministic tests).
     * @return A list of [ShopInventoryItem] representing the shop's generated inventory.
     */
    suspend operator fun invoke(shopType: ShopType, random: Random = Random): List<ShopInventoryItem> {
        // 1. Determine inventory size
        val inventorySize = random.nextInt(MIN_INVENTORY_SIZE, MAX_INVENTORY_SIZE + 1)

        // 2. Fetch catalog items for this shop type's categories
        val candidateItems = shopType.defaultCategories
            .flatMap { category -> itemRepository.getItemsByCategory(category) }
            .distinctBy { it.id }

        if (candidateItems.isEmpty()) return emptyList()

        // 3. Group candidates by rarity for weighted selection
        val itemsByRarity = candidateItems.groupBy { it.rarity }

        // 4. Select items with rarity distribution, no duplicates
        val selectedItems = selectItems(
            itemsByRarity = itemsByRarity,
            candidateItems = candidateItems,
            targetSize = inventorySize.coerceAtMost(candidateItems.size),
            random = random,
        )

        // 5. Build inventory items with price variance and quantity
        return selectedItems.map { item ->
            ShopInventoryItem(
                item = item,
                quantity = generateQuantity(random),
                adjustedPrice = applyPriceVariance(item.price, random),
            )
        }
    }

    /**
     * Selects items respecting rarity distribution weights.
     * Falls back to random selection from all candidates if the target rarity bucket is empty.
     */
    private fun selectItems(
        itemsByRarity: Map<Rarity, List<Item>>,
        candidateItems: List<Item>,
        targetSize: Int,
        random: Random,
    ): List<Item> {
        val selected = mutableSetOf<Long>() // track by ID to avoid duplicates
        val result = mutableListOf<Item>()

        repeat(targetSize) {
            val targetRarity = rollRarity(random)

            // Try to pick from the target rarity bucket, excluding already-selected items
            val bucket = itemsByRarity[targetRarity]
                ?.filter { it.id !in selected }

            val item = if (!bucket.isNullOrEmpty()) {
                bucket[random.nextInt(bucket.size)]
            } else {
                // Fallback: pick from any remaining candidate
                val remaining = candidateItems.filter { it.id !in selected }
                if (remaining.isEmpty()) return result
                remaining[random.nextInt(remaining.size)]
            }

            selected.add(item.id)
            result.add(item)
        }

        return result
    }

    /**
     * Rolls a rarity based on the defined distribution weights.
     */
    private fun rollRarity(random: Random): Rarity {
        val roll = random.nextInt(100) // 0-99
        var cumulative = 0
        for ((rarity, weight) in RARITY_WEIGHTS) {
            cumulative += weight
            if (roll < cumulative) return rarity
        }
        // Should not be reached given weights sum to 100, but default to Common
        return Rarity.Common
    }

    /**
     * Applies +/-10% price variance to the base price.
     * The result is rounded to the nearest copper piece (minimum 1 CP).
     */
    private fun applyPriceVariance(basePrice: Price, random: Random): Price {
        if (basePrice.copperPieces == 0L) return basePrice

        // Generate a variance factor between -0.10 and +0.10
        val varianceFactor = (random.nextDouble() * 2 * PRICE_VARIANCE_FRACTION) - PRICE_VARIANCE_FRACTION
        val adjustedCp = (basePrice.copperPieces * (1.0 + varianceFactor)).roundToLong()

        // Ensure minimum price of 1 CP
        return Price(adjustedCp.coerceAtLeast(1L))
    }

    /**
     * Generates a random quantity for an inventory item.
     * Has a [UNLIMITED_STOCK_CHANCE] chance of returning null (unlimited stock).
     */
    private fun generateQuantity(random: Random): Int? {
        return if (random.nextDouble() < UNLIMITED_STOCK_CHANCE) {
            null
        } else {
            random.nextInt(MIN_QUANTITY, MAX_QUANTITY + 1)
        }
    }
}
