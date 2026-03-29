package com.shopforge.data.mapper

import com.shopforge.data.db.SelectByShopId
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem

/**
 * Maps between SQLDelight joined query results and domain [ShopInventoryItem].
 *
 * The selectByShopId query returns a flat [SelectByShopId] row combining
 * ShopInventory and Item columns.
 */
object ShopInventoryMapper {

    /**
     * Creates a [ShopInventoryItem] from a [SelectByShopId] joined row.
     */
    fun toDomain(row: SelectByShopId): ShopInventoryItem {
        val item = Item(
            id = row.id,
            name = row.name,
            description = row.description,
            category = ItemCategory.valueOf(row.type),
            price = Price(row.price),
            rarity = Rarity.valueOf(row.rarity),
            isCustom = row.isCustom != 0L,
        )
        return ShopInventoryItem(
            item = item,
            quantity = row.quantity?.toInt(),
            adjustedPrice = Price(row.adjustedPrice),
        )
    }
}
