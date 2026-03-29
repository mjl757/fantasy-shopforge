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
 * Generates an inventory (8-15 items) for a given [ShopType] from the item catalog.
 *
 * Applies rarity distribution (70/15/10/4/1) and price variance (plus or minus 10%).
 */
class GenerateInventoryUseCase(
    private val itemRepository: ItemRepository,
    private val random: Random = Random.Default,
) {

    suspend operator fun invoke(shopType: ShopType): List<ShopInventoryItem> {
        // Collect all candidate items matching the shop type's categories
        val candidates = shopType.defaultCategories.flatMap { category ->
            itemRepository.getItemsByCategory(category)
        }.distinctBy { it.id }

        if (candidates.isEmpty()) return emptyList()

        val inventorySize = random.nextInt(MIN_INVENTORY, MAX_INVENTORY + 1)

        // Select items respecting rarity distribution, no duplicates
        val selected = selectItems(candidates, inventorySize)

        return selected.map { item ->
            ShopInventoryItem(
                item = item,
                quantity = generateQuantity(),
                adjustedPrice = applyPriceVariance(item.price),
            )
        }
    }

    private fun selectItems(candidates: List<Item>, count: Int): List<Item> {
        val selected = mutableListOf<Item>()
        val usedIds = mutableSetOf<Long>()

        repeat(count) {
            val targetRarity = rollRarity()
            // Try to find a candidate matching the target rarity that hasn't been selected
            val candidate = candidates
                .filter { it.rarity == targetRarity && it.id !in usedIds }
                .randomOrNull(random)
            // Fallback to any unselected candidate if no match
                ?: candidates.filter { it.id !in usedIds }.randomOrNull(random)
                ?: return selected // No more candidates available

            selected.add(candidate)
            usedIds.add(candidate.id)
        }
        return selected
    }

    private fun rollRarity(): Rarity {
        val roll = random.nextInt(100)
        return when {
            roll < 70 -> Rarity.Common
            roll < 85 -> Rarity.Uncommon
            roll < 95 -> Rarity.Rare
            roll < 99 -> Rarity.VeryRare
            else -> Rarity.Legendary
        }
    }

    private fun generateQuantity(): Int? {
        // 20% chance of unlimited stock, otherwise 1-10
        return if (random.nextInt(5) == 0) null else random.nextInt(1, 11)
    }

    private fun applyPriceVariance(basePrice: Price): Price {
        if (basePrice.copperPieces == 0L) return basePrice
        val variance = 1.0 + (random.nextDouble() * 0.2 - 0.1) // -10% to +10%
        val adjusted = (basePrice.copperPieces * variance).roundToLong().coerceAtLeast(1L)
        return Price(adjusted)
    }

    companion object {
        const val MIN_INVENTORY = 8
        const val MAX_INVENTORY = 15
    }
}
