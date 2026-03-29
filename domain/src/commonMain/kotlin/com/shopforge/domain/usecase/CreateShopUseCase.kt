package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ShopRepository

/**
 * Creates a new shop with the given name, type, and optional description.
 * The shop is created with an empty inventory.
 *
 * Business rules:
 * - Shop name must not be blank.
 * - Shop type is required (enforced by the non-nullable parameter).
 */
class CreateShopUseCase(
    private val shopRepository: ShopRepository,
    private val clock: () -> Long,
) {
    /**
     * @return The generated ID of the newly created shop.
     * @throws IllegalArgumentException if [name] is blank.
     */
    suspend operator fun invoke(
        name: String,
        type: ShopType,
        description: String? = null,
    ): Long {
        require(name.isNotBlank()) { "Shop name must not be blank" }

        val now = clock()
        val shop = Shop(
            id = 0,
            name = name.trim(),
            type = type,
            description = description?.trim()?.ifEmpty { null },
            createdAt = now,
            updatedAt = now,
        )
        return shopRepository.createShop(shop)
    }
}
