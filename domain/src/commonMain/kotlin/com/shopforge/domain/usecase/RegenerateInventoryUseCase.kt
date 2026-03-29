package com.shopforge.domain.usecase

import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.first

/**
 * Clears a shop's existing inventory and replaces it with a freshly
 * generated set of items based on the shop's type.
 *
 * Delegates inventory generation to [GenerateInventoryUseCase].
 */
class RegenerateInventoryUseCase(
    private val shopRepository: ShopRepository,
    private val generateInventoryUseCase: GenerateInventoryUseCase,
) {
    /**
     * @param shopId The shop whose inventory to regenerate.
     * @throws NoSuchElementException if no shop with [shopId] exists.
     */
    suspend operator fun invoke(shopId: Long) {
        val shop = shopRepository.getShopById(shopId).first()
            ?: throw NoSuchElementException("Shop with id $shopId not found")

        val newInventory = generateInventoryUseCase(shop.type)
        shopRepository.replaceInventory(shopId, newInventory)
    }
}
