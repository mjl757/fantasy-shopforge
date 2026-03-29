package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Retrieves a single shop along with its full inventory as a reactive Flow.
 * Emits null if the shop does not exist.
 */
class GetShopWithInventoryUseCase(
    private val shopRepository: ShopRepository,
) {
    operator fun invoke(shopId: Long): Flow<ShopWithInventory?> {
        return combine(
            shopRepository.getShopById(shopId),
            shopRepository.getInventory(shopId),
        ) { shop, inventory ->
            shop?.let { ShopWithInventory(it, inventory) }
        }
    }
}

/**
 * A shop paired with its current inventory.
 */
data class ShopWithInventory(
    val shop: Shop,
    val inventory: List<ShopInventoryItem>,
)
