package com.shopforge.data.mapper

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.data.db.migrations.Item as DbItem

internal fun DbItem.toDomain(): Item = Item(
    id = id,
    name = name,
    description = description,
    category = ItemCategory.valueOf(type),
    price = Price(price),
    rarity = Rarity.valueOf(rarity),
    isCustom = isCustom != 0L,
)

internal fun ItemCategory.toDbString(): String = name

internal fun Rarity.toDbString(): String = name

internal fun Boolean.toDbIsCustom(): Long = if (this) 1L else 0L
