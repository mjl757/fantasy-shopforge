package com.shopforge.domain.repository

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for catalog [Item] access and custom item management.
 * Implementations live in the `:data` module.
 */
interface ItemRepository {

    // ---- Catalog queries ----

    /**
     * Emits all catalog items (built-in and custom) and re-emits on any change.
     */
    fun getAllItems(): Flow<List<Item>>

    /**
     * Returns a one-shot snapshot of all items matching [category].
     */
    suspend fun getItemsByCategory(category: ItemCategory): List<Item>

    /**
     * Returns a one-shot snapshot of all items matching [rarity].
     */
    suspend fun getItemsByRarity(rarity: Rarity): List<Item>

    /**
     * Returns a one-shot snapshot of all items whose name or description
     * contains [query] (case-insensitive).
     */
    suspend fun searchItems(query: String): List<Item>

    /**
     * Returns the item with [id], or null if it does not exist.
     */
    suspend fun getItemById(id: Long): Item?

    // ---- Custom item CRUD ----

    /**
     * Inserts a custom item and returns its generated id.
     * The [item]'s [Item.isCustom] must be true.
     */
    suspend fun createCustomItem(item: Item): Long

    /**
     * Replaces the stored custom item with the same [Item.id].
     * Only custom items (where [Item.isCustom] is true) may be updated.
     */
    suspend fun updateCustomItem(item: Item)

    /**
     * Deletes the custom item with [id].
     * Only custom items may be deleted; catalog items are immutable.
     */
    suspend fun deleteCustomItem(id: Long)
}
