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
        val route = AppRoute.ShopDetail(shopId = 123L)
        assertEquals(123L, route.shopId)
    }

    @Test
    fun `ShopDetail equality is based on shopId`() {
        val a = AppRoute.ShopDetail(1L)
        val b = AppRoute.ShopDetail(1L)
        val c = AppRoute.ShopDetail(2L)
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
        val route = AppRoute.EditShop(shopId = 42L)
        assertEquals(42L, route.shopId)
    }

    @Test
    fun `EditShop equality is based on shopId`() {
        val a = AppRoute.EditShop(1L)
        val b = AppRoute.EditShop(1L)
        val c = AppRoute.EditShop(2L)
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
        val route = AppRoute.AddItemToShop(shopId = 99L)
        assertEquals(99L, route.shopId)
    }

    @Test
    fun `AddItemToShop equality is based on shopId`() {
        val a = AppRoute.AddItemToShop(1L)
        val b = AppRoute.AddItemToShop(1L)
        val c = AppRoute.AddItemToShop(2L)
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
            AppRoute.ShopDetail(0L),
            AppRoute.CreateShop,
            AppRoute.EditShop(0L),
            AppRoute.GenerateShop,
            AppRoute.AddItemToShop(0L),
        )
        assertEquals(6, routes.size, "Expected exactly 6 route types")
    }

    // ------------------------------------------------------------------
    // Routes with the same shopId but different types are not equal
    // ------------------------------------------------------------------

    @Test
    fun `ShopDetail and EditShop with same shopId are not equal`() {
        val detail: Any = AppRoute.ShopDetail(1L)
        val edit: Any = AppRoute.EditShop(1L)
        assertNotEquals(detail, edit)
    }

    @Test
    fun `ShopDetail and AddItemToShop with same shopId are not equal`() {
        val detail: Any = AppRoute.ShopDetail(99L)
        val add: Any = AppRoute.AddItemToShop(99L)
        assertNotEquals(detail, add)
    }
}
