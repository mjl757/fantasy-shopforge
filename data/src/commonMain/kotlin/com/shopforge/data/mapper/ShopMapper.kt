package com.shopforge.data.mapper

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.data.db.migrations.Shop as DbShop

internal fun DbShop.toDomain(): Shop = Shop(
    id = id,
    name = name,
    type = ShopType.valueOf(type),
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ShopType.toDbString(): String = name
