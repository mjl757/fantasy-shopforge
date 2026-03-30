package com.shopforge.data.mapper

import com.shopforge.data.db.SelectByShopId
import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopInventoryItem

internal fun SelectByShopId.toDomain(): ShopInventoryItem {
    val item = Item(
        id = id,
        name = name,
        description = description,
        category = ItemCategory.valueOf(type),
        price = Price(price),
        rarity = Rarity.valueOf(rarity),
        isCustom = isCustom != 0L,
    )
    return ShopInventoryItem(
        item = item,
        quantity = quantity?.toInt(),
        adjustedPrice = Price(adjustedPrice),
    )
}
