package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Price
import com.shopforge.domain.repository.ShopRepository

/**
 * Adds an item (catalog or custom) to a shop's inventory with a given
 * quantity and adjusted price.
 */
class AddItemToShopUseCase(
    private val shopRepository: ShopRepository,
) {
    /**
     * @param shopId The shop to add the item to.
     * @param item The item to add.
     * @param quantity Stock quantity, or null for unlimited stock.
     * @param adjustedPrice The price for this item in this shop.
     */
    suspend operator fun invoke(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: Price,
    ) {
        shopRepository.addItemToShop(
            shopId = shopId,
            item = item,
            quantity = quantity,
            adjustedPrice = adjustedPrice,
        )
    }
}
