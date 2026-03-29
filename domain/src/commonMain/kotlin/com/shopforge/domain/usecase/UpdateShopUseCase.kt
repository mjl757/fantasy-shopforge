package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.first

/**
 * Updates an existing shop's name, type, and/or description.
 *
 * Business rules:
 * - Shop name must not be blank.
 * - Shop type is required.
 */
class UpdateShopUseCase(
    private val shopRepository: ShopRepository,
    private val clock: () -> Long,
) {
    /**
     * @throws IllegalArgumentException if [name] is blank.
     * @throws NoSuchElementException if no shop with [shopId] exists.
     */
    suspend operator fun invoke(
        shopId: Long,
        name: String,
        type: ShopType,
        description: String? = null,
    ) {
        require(name.isNotBlank()) { "Shop name must not be blank" }

        val existing = shopRepository.getShopById(shopId).first()
            ?: throw NoSuchElementException("Shop with id $shopId not found")

        val updated = existing.copy(
            name = name.trim(),
            type = type,
            description = description?.trim()?.ifEmpty { null },
            updatedAt = clock(),
        )
        shopRepository.updateShop(updated)
    }
}
