package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository

/**
 * Decrements the quantity of an item in a shop's inventory by 1.
 *
 * This is a no-op when:
 * - The item has unlimited stock (null quantity)
 * - The item is already sold out (quantity == 0)
 */
class DecrementQuantityUseCase(
    private val shopRepository: ShopRepository,
) {
    /**
     * @param shopId The shop containing the item.
     * @param itemId The item whose quantity to decrement.
     * @param currentQuantity The current quantity of the item (null = unlimited).
     */
    suspend operator fun invoke(shopId: Long, itemId: Long, currentQuantity: Int?) {
        // No-op for unlimited stock or already sold out
        if (currentQuantity == null || currentQuantity <= 0) return

        shopRepository.updateItemQuantity(shopId, itemId, currentQuantity - 1)
    }
}
