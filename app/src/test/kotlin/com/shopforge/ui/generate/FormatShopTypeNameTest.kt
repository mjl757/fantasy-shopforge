package com.shopforge.ui.generate

import com.shopforge.domain.model.ShopType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatShopTypeNameTest {

    @Test
    fun `single word type names are unchanged`() {
        assertEquals("Blacksmith", formatShopTypeName(ShopType.Blacksmith))
        assertEquals("Alchemist", formatShopTypeName(ShopType.Alchemist))
        assertEquals("Fletcher", formatShopTypeName(ShopType.Fletcher))
        assertEquals("Tavern", formatShopTypeName(ShopType.Tavern))
        assertEquals("Temple", formatShopTypeName(ShopType.Temple))
    }

    @Test
    fun `camelCase type names are split with spaces`() {
        assertEquals("Magic Shop", formatShopTypeName(ShopType.MagicShop))
        assertEquals("General Store", formatShopTypeName(ShopType.GeneralStore))
        assertEquals("Exotic Goods", formatShopTypeName(ShopType.ExoticGoods))
    }
}
