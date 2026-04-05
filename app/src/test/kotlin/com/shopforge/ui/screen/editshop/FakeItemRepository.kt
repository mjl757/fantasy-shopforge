package com.shopforge.ui.screen.editshop

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake [ItemRepository] for ViewModel testing.
 * Returns empty results — sufficient for Edit Shop screen tests
 * where inventory generation is not the focus.
 */
class FakeItemRepository : ItemRepository {

    private val items = MutableStateFlow<List<Item>>(emptyList())

    override fun getAllItems(): Flow<List<Item>> = items

    override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
        items.value.filter { it.category == category }

    override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
        items.value.filter { it.rarity == rarity }

    override suspend fun searchItems(query: String): List<Item> =
        items.value.filter { it.name.contains(query, ignoreCase = true) }

    override suspend fun getItemById(id: Long): Item? =
        items.value.find { it.id == id }

    override suspend fun createCustomItem(item: Item): Long = 0L

    override suspend fun updateCustomItem(item: Item) {}

    override suspend fun deleteCustomItem(id: Long) {}
}
