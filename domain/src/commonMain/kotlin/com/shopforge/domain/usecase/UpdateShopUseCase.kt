package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository

/**
 * Updates an existing shop's name, type, and description.
 *
 * Business rules:
 * - Shop name must not be blank.
 */
class UpdateShopUseCase(
    private val shopRepository: ShopRepository,
    private val clock: () -> Long,
) {

    /**
     * @param existingShop The current shop to update (used for id, createdAt).
     * @param name The new name.
     * @param type The new type.
     * @param description The new description.
     * @throws IllegalArgumentException if [name] is blank.
     */
    suspend operator fun invoke(
        existingShop: Shop,
        name: String,
        type: ShopType,
        description: String?,
    ) {
        require(name.isNotBlank()) { "Shop name must not be blank" }

        val updated = existingShop.copy(
            name = name.trim(),
            type = type,
            description = description?.trim()?.ifBlank { null },
            updatedAt = clock(),
        )
        shopRepository.updateShop(updated)
    }
}
