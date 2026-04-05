package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository

/**
 * Regenerates the entire inventory for a shop.
 * Clears existing inventory and generates new items based on the shop type.
 */
class RegenerateInventoryUseCase(
    private val shopRepository: ShopRepository,
    private val generateInventoryUseCase: GenerateInventoryUseCase,
) {

    suspend operator fun invoke(shopId: Long, shopType: ShopType) {
        val newInventory = generateInventoryUseCase(shopType)
        shopRepository.replaceInventory(shopId, newInventory)
    }
}
