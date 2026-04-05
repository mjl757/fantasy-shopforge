package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/**
 * Retrieves all catalog items as a reactive Flow.
 */
class GetAllItemsUseCase(
    private val itemRepository: ItemRepository,
) {
    operator fun invoke(): Flow<List<Item>> = itemRepository.getAllItems()
}
