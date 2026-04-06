package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository

/**
 * Increments the quantity of an item in a shop's inventory by 1.
 *
 * This is a no-op when the item has unlimited stock (null quantity).
 */
class IncrementQuantityUseCase(
    private val shopRepository: ShopRepository,
) {
    /**
     * @param shopId The shop containing the item.
     * @param itemId The item whose quantity to increment.
     * @param currentQuantity The current quantity of the item (null = unlimited).
     */
    suspend operator fun invoke(shopId: Long, itemId: Long, currentQuantity: Int?) {
        if (currentQuantity == null) return

        shopRepository.updateItemQuantity(shopId, itemId, currentQuantity + 1)
    }
}
