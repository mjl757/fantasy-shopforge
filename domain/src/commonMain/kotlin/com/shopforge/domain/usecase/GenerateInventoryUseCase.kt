package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.model.ShopType

/**
 * Generates a randomized inventory for a shop based on its type.
 *
 * This interface is defined here so that [RegenerateInventoryUseCase] can
 * depend on it. The full implementation is provided by issue #4.
 */
interface GenerateInventoryUseCase {
    /**
     * Generates a list of inventory items appropriate for the given [shopType].
     */
    suspend operator fun invoke(shopType: ShopType): List<ShopInventoryItem>
}
