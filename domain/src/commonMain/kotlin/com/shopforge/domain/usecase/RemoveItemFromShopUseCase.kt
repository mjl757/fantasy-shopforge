package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository

/**
 * Removes an item from a shop's inventory.
 */
class RemoveItemFromShopUseCase(
    private val shopRepository: ShopRepository,
) {
    suspend operator fun invoke(shopId: Long, itemId: Long) {
        shopRepository.removeItemFromShop(shopId = shopId, itemId = itemId)
    }
}
