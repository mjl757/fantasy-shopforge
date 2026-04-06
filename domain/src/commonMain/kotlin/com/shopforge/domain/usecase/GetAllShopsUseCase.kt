package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case that returns a reactive stream of all saved shops.
 */
class GetAllShopsUseCase(
    private val shopRepository: ShopRepository,
) {
    operator fun invoke(): Flow<List<Shop>> = shopRepository.getAllShops()
}
