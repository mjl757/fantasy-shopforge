package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.first

/**
 * Decrements an item's quantity by 1 in a shop's inventory.
 *
 * Business rules:
 * - Null quantity (unlimited stock) is a no-op.
 * - Quantity at 0 (sold out) is a no-op.
 * - Otherwise, quantity is decremented by 1.
 */
class DecrementQuantityUseCase(
    private val shopRepository: ShopRepository,
) {
    /**
     * @param shopId The shop containing the item.
     * @param itemId The item whose quantity to decrement.
     */
    suspend operator fun invoke(shopId: Long, itemId: Long) {
        val inventory = shopRepository.getInventory(shopId).first()
        val inventoryItem = inventory.find { it.item.id == itemId } ?: return

        val currentQuantity = inventoryItem.quantity

        // Unlimited stock (null) -> no-op
        if (currentQuantity == null) return

        // Already sold out (0) -> no-op
        if (currentQuantity <= 0) return

        // Decrement by 1
        shopRepository.updateItemQuantity(
            shopId = shopId,
            itemId = itemId,
            quantity = currentQuantity - 1,
        )
    }
}
