package com.shopforge.data.mapper

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.data.db.migrations.Shop as DbShop

/**
 * Maps between SQLDelight-generated [DbShop] and domain [Shop].
 */
object ShopMapper {

    fun toDomain(dbShop: DbShop): Shop = Shop(
        id = dbShop.id,
        name = dbShop.name,
        type = ShopType.valueOf(dbShop.type),
        description = dbShop.description,
        createdAt = dbShop.createdAt,
        updatedAt = dbShop.updatedAt,
    )

    fun toDbType(shopType: ShopType): String = shopType.name
}
