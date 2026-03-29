package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType
import kotlin.random.Random

/**
 * Generates a list of inventory items appropriate for a given [ShopType].
 *
 * This interface is defined here so that [GenerateShopUseCase] and
 * [RegenerateInventoryUseCase] can depend on it.
 * The concrete implementation is provided in issue #4.
 */
interface GenerateInventoryUseCase {

    /**
     * Generates inventory items for a shop of the given [shopType].
     *
     * @param shopType The type of shop to generate inventory for.
     * @param random A [Random] instance for deterministic testing.
     * @return A list of [ShopInventoryItem] for the generated shop.
     */
    suspend operator fun invoke(shopType: ShopType, random: Random = Random): List<ShopInventoryItem>
}
