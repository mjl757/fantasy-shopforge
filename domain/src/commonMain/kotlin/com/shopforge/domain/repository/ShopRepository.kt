package com.shopforge.domain.repository

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopInventoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [Shop] persistence and inventory management.
 * Implementations live in the `:data` module.
 */
interface ShopRepository {

    // ---- Shop CRUD ----

    /**
     * Emits the current list of all shops and re-emits on any change.
     */
    fun getAllShops(): Flow<List<Shop>>

    /**
     * Emits the shop with the given [id] and re-emits when it changes.
     * Emits null if no shop with that id exists.
     */
    fun getShopById(id: Long): Flow<Shop?>

    /**
     * Inserts a new shop and returns its generated id.
     */
    suspend fun createShop(shop: Shop): Long

    /**
     * Replaces the stored shop that has the same [Shop.id].
     */
    suspend fun updateShop(shop: Shop)

    /**
     * Deletes the shop with [id] along with its inventory items.
     */
    suspend fun deleteShop(id: Long)

    // ---- Inventory management ----

    /**
     * Emits the current inventory for [shopId] and re-emits on any change.
     */
    fun getInventory(shopId: Long): Flow<List<ShopInventoryItem>>

    /**
     * Adds [item] to the inventory of [shopId] with the given [quantity]
     * and [adjustedPrice]. If the item is already in the shop's inventory,
     * the quantity is incremented by [quantity].
     *
     * @param quantity Null represents unlimited stock.
     */
    suspend fun addItemToShop(
        shopId: Long,
        item: Item,
        quantity: Int?,
        adjustedPrice: com.shopforge.domain.model.Price,
    )

    /**
     * Removes [itemId] from the inventory of [shopId].
     */
    suspend fun removeItemFromShop(shopId: Long, itemId: Long)

    /**
     * Updates the quantity of [itemId] in the inventory of [shopId].
     *
     * @param quantity Null sets the stock to unlimited.
     */
    suspend fun updateItemQuantity(shopId: Long, itemId: Long, quantity: Int?)

    /**
     * Replaces the entire inventory of [shopId] with [items].
     * Existing inventory entries are deleted before the new ones are inserted.
     */
    suspend fun replaceInventory(shopId: Long, items: List<ShopInventoryItem>)
}
