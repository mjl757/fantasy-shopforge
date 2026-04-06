package com.shopforge.domain.usecase

import com.shopforge.domain.model.Price
import com.shopforge.domain.repository.ShopRepository

/**
 * Updates the adjusted price of an item in a shop's inventory.
 */
class UpdateItemAdjustedPriceUseCase(
    private val shopRepository: ShopRepository,
) {
    suspend operator fun invoke(shopId: Long, itemId: Long, adjustedPrice: Price) {
        shopRepository.updateItemAdjustedPrice(shopId, itemId, adjustedPrice)
    }
}
