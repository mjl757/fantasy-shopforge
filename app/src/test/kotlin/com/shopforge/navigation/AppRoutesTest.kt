package com.shopforge.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for the type-safe navigation route definitions.
 *
 * These tests verify that route objects and data classes are constructed correctly,
 * that equality is based on arguments, and that each route is a distinct type.
 */
class AppRoutesTest {

    // ------------------------------------------------------------------
    // ShopList
    // ------------------------------------------------------------------

    @Test
    fun `ShopList is a singleton object`() {
        val r1: AppRoute = AppRoute.ShopList
        val r2: AppRoute = AppRoute.ShopList
        assertEquals(r1, r2, "ShopList should be equal to itself")
    }

    // ------------------------------------------------------------------
    // ShopDetail
    // ------------------------------------------------------------------

    @Test
    fun `ShopDetail carries shopId`() {
        val route = AppRoute.ShopDetail(shopId = "shop-123")
        assertEquals("shop-123", route.shopId)
    }

    @Test
    fun `ShopDetail equality is based on shopId`() {
        val a = AppRoute.ShopDetail("abc")
        val b = AppRoute.ShopDetail("abc")
        val c = AppRoute.ShopDetail("xyz")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    // ------------------------------------------------------------------
    // CreateShop
    // ------------------------------------------------------------------

    @Test
    fun `CreateShop is a singleton object`() {
        assertEquals(AppRoute.CreateShop, AppRoute.CreateShop)
    }

    // ------------------------------------------------------------------
    // EditShop
    // ------------------------------------------------------------------

    @Test
    fun `EditShop carries shopId`() {
        val route = AppRoute.EditShop(shopId = "edit-42")
        assertEquals("edit-42", route.shopId)
    }

    @Test
    fun `EditShop equality is based on shopId`() {
        val a = AppRoute.EditShop("alpha")
        val b = AppRoute.EditShop("alpha")
        val c = AppRoute.EditShop("beta")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    // ------------------------------------------------------------------
    // GenerateShop
    // ------------------------------------------------------------------

    @Test
    fun `GenerateShop is a singleton object`() {
        assertEquals(AppRoute.GenerateShop, AppRoute.GenerateShop)
    }

    // ------------------------------------------------------------------
    // AddItemToShop
    // ------------------------------------------------------------------

    @Test
    fun `AddItemToShop carries shopId`() {
        val route = AppRoute.AddItemToShop(shopId = "shop-99")
        assertEquals("shop-99", route.shopId)
    }

    @Test
    fun `AddItemToShop equality is based on shopId`() {
        val a = AppRoute.AddItemToShop("one")
        val b = AppRoute.AddItemToShop("one")
        val c = AppRoute.AddItemToShop("two")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    // ------------------------------------------------------------------
    // All routes implement AppRoute
    // ------------------------------------------------------------------

    @Test
    fun `all route types implement AppRoute sealed interface`() {
        val routes: List<AppRoute> = listOf(
            AppRoute.ShopList,
            AppRoute.ShopDetail("id"),
            AppRoute.CreateShop,
            AppRoute.EditShop("id"),
            AppRoute.GenerateShop,
            AppRoute.AddItemToShop("id"),
        )
        assertEquals(6, routes.size, "Expected exactly 6 route types")
    }

    // ------------------------------------------------------------------
    // Routes with the same shopId but different types are not equal
    // ------------------------------------------------------------------

    @Test
    fun `ShopDetail and EditShop with same shopId are not equal`() {
        val detail: Any = AppRoute.ShopDetail("same-id")
        val edit: Any = AppRoute.EditShop("same-id")
        assertNotEquals(detail, edit)
    }

    @Test
    fun `ShopDetail and AddItemToShop with same shopId are not equal`() {
        val detail: Any = AppRoute.ShopDetail("same-id")
        val add: Any = AppRoute.AddItemToShop("same-id")
        assertNotEquals(detail, add)
    }
}
