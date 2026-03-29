package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

/**
 * Retrieves all shops as a reactive Flow for the shop list screen.
 */
class GetAllShopsUseCase(
    private val shopRepository: ShopRepository,
) {
    operator fun invoke(): Flow<List<Shop>> {
        return shopRepository.getAllShops()
    }
}
