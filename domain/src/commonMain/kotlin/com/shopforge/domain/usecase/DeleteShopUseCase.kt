package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository

/**
 * Deletes a shop and its inventory.
 */
class DeleteShopUseCase(
    private val shopRepository: ShopRepository,
) {

    suspend operator fun invoke(shopId: Long) {
        shopRepository.deleteShop(shopId)
    }
}
