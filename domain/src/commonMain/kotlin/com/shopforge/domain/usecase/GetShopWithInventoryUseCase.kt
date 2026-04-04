package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopWithInventory
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Loads a shop and its inventory reactively.
 * Emits a new [ShopWithInventory] whenever either the shop or its inventory changes.
 */
class GetShopWithInventoryUseCase(
    private val shopRepository: ShopRepository,
) {
    operator fun invoke(shopId: Long): Flow<ShopWithInventory?> =
        combine(
            shopRepository.getShopById(shopId),
            shopRepository.getInventory(shopId),
        ) { shop, inventory ->
            shop?.let { ShopWithInventory(it, inventory) }
        }
}

