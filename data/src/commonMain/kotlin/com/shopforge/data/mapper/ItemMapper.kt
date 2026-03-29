package com.shopforge.data.mapper

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.data.db.migrations.Item as DbItem

/**
 * Maps between SQLDelight-generated [DbItem] and domain [Item].
 */
object ItemMapper {

    fun toDomain(dbItem: DbItem): Item = Item(
        id = dbItem.id,
        name = dbItem.name,
        description = dbItem.description,
        category = ItemCategory.valueOf(dbItem.type),
        price = Price(dbItem.price),
        rarity = Rarity.valueOf(dbItem.rarity),
        isCustom = dbItem.isCustom != 0L,
    )

    fun toDbCategory(category: ItemCategory): String = category.name

    fun toDbRarity(rarity: Rarity): String = rarity.name

    fun toDbIsCustom(isCustom: Boolean): Long = if (isCustom) 1L else 0L
}
