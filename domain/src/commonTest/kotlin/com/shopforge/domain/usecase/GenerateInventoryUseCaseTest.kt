package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Denomination
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateInventoryUseCaseTest {

    // ---- Fake repository ----

    private class FakeItemRepository(private val items: List<Item> = emptyList()) : ItemRepository {
        override fun getAllItems(): Flow<List<Item>> = flowOf(items)
        override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
            items.filter { it.category == category }
        override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
            items.filter { it.rarity == rarity }
        override suspend fun searchItems(query: String): List<Item> =
            items.filter { it.name.contains(query, ignoreCase = true) }
        override suspend fun getItemById(id: Long): Item? =
            items.find { it.id == id }
        override suspend fun createCustomItem(item: Item): Long = item.id
        override suspend fun updateCustomItem(item: Item) {}
        override suspend fun deleteCustomItem(id: Long) {}
    }

    // ---- Test catalog ----

    private val testCatalog = listOf(
        // Weapons (Blacksmith, Fletcher)
        Item(1, "Longsword", null, ItemCategory.Weapon, Price(15, Denomination.Gold), Rarity.Common, false),
        Item(2, "Shortsword", null, ItemCategory.Weapon, Price(10, Denomination.Gold), Rarity.Common, false),
        Item(3, "Greatsword", null, ItemCategory.Weapon, Price(50, Denomination.Gold), Rarity.Uncommon, false),
        Item(4, "Flame Tongue", "A magical flaming blade", ItemCategory.Weapon, Price(500, Denomination.Gold), Rarity.Rare, false),
        Item(5, "Vorpal Sword", "Snicker-snack", ItemCategory.Weapon, Price(2000, Denomination.Gold), Rarity.VeryRare, false),
        Item(6, "Holy Avenger", "A legendary blade", ItemCategory.Weapon, Price(5000, Denomination.Gold), Rarity.Legendary, false),
        // Armor (Blacksmith)
        Item(7, "Leather Armor", null, ItemCategory.Armor, Price(10, Denomination.Gold), Rarity.Common, false),
        Item(8, "Chain Mail", null, ItemCategory.Armor, Price(75, Denomination.Gold), Rarity.Common, false),
        Item(9, "Plate Armor", null, ItemCategory.Armor, Price(1500, Denomination.Gold), Rarity.Uncommon, false),
        Item(10, "Mithral Half Plate", null, ItemCategory.Armor, Price(3000, Denomination.Gold), Rarity.Rare, false),
        Item(11, "Shield +1", null, ItemCategory.Armor, Price(200, Denomination.Gold), Rarity.Uncommon, false),
        Item(12, "Adamantine Plate", null, ItemCategory.Armor, Price(4000, Denomination.Gold), Rarity.VeryRare, false),
        Item(13, "Shield +3", null, ItemCategory.Armor, Price(8000, Denomination.Gold), Rarity.Legendary, false),
        // Potions (Alchemist, MagicShop, Temple)
        Item(14, "Healing Potion", null, ItemCategory.Potion, Price(50, Denomination.Gold), Rarity.Common, false),
        Item(15, "Greater Healing Potion", null, ItemCategory.Potion, Price(100, Denomination.Gold), Rarity.Uncommon, false),
        // Adventuring Gear (General Store)
        Item(16, "Rope (50 ft)", null, ItemCategory.AdventuringGear, Price(1, Denomination.Gold), Rarity.Common, false),
        Item(17, "Torch (10-pack)", null, ItemCategory.AdventuringGear, Price(1, Denomination.Silver), Rarity.Common, false),
        // Ammunition (Fletcher)
        Item(18, "Arrows (20)", null, ItemCategory.Ammunition, Price(1, Denomination.Gold), Rarity.Common, false),
        Item(19, "Bolts (20)", null, ItemCategory.Ammunition, Price(1, Denomination.Gold), Rarity.Common, false),
    )

    private fun createUseCase(
        items: List<Item> = testCatalog,
        random: Random = Random.Default,
    ): GenerateInventoryUseCase {
        return GenerateInventoryUseCase(FakeItemRepository(items), random)
    }

    // ---- Inventory size tests ----

    @Test
    fun `generated inventory size is between 8 and 15`() = runTest {
        // Run multiple times with different seeds
        repeat(50) { seed ->
            val useCase = createUseCase(random = Random(seed))
            val result = useCase.invoke(ShopType.Blacksmith)
            assertTrue(
                result.size in 8..15,
                "Inventory size ${result.size} not in expected range for seed $seed"
            )
        }
    }

    @Test
    fun `inventory size is capped by available candidates`() = runTest {
        // Only 2 items available for General Store (AdventuringGear + Food)
        val useCase = createUseCase(random = Random(42))
        val result = useCase.invoke(ShopType.GeneralStore)
        // GeneralStore categories: AdventuringGear, Food -> only 2 items in our catalog
        assertEquals(2, result.size)
    }

    @Test
    fun `empty catalog returns empty inventory`() = runTest {
        val useCase = createUseCase(emptyList(), Random(42))
        val result = useCase.invoke(ShopType.Blacksmith)
        assertTrue(result.isEmpty())
    }

    // ---- Category filtering tests ----

    @Test
    fun `items are filtered by shop type default categories`() = runTest {
        val useCase = createUseCase(random = Random(42))
        val result = useCase.invoke(ShopType.Blacksmith)
        val allowedCategories = ShopType.Blacksmith.defaultCategories.toSet()
        result.forEach { inventoryItem ->
            assertTrue(
                inventoryItem.item.category in allowedCategories,
                "Item '${inventoryItem.item.name}' has category ${inventoryItem.item.category} " +
                    "which is not in Blacksmith's categories: $allowedCategories"
            )
        }
    }

    @Test
    fun `fletcher inventory only contains weapons and ammunition`() = runTest {
        val useCase = createUseCase(random = Random(42))
        val result = useCase.invoke(ShopType.Fletcher)
        val allowedCategories = ShopType.Fletcher.defaultCategories.toSet()
        result.forEach { inventoryItem ->
            assertTrue(inventoryItem.item.category in allowedCategories)
        }
    }

    // ---- No duplicates test ----

    @Test
    fun `no duplicate items in generated inventory`() = runTest {
        repeat(50) { seed ->
            val useCase = createUseCase(random = Random(seed))
            val result = useCase.invoke(ShopType.Blacksmith)
            val ids = result.map { it.item.id }
            assertEquals(ids.size, ids.toSet().size, "Duplicate items found for seed $seed")
        }
    }

    // ---- Price variance tests ----

    @Test
    fun `price variance is within plus minus 10 percent`() = runTest {
        repeat(50) { seed ->
            val useCase = createUseCase(random = Random(seed))
            val result = useCase.invoke(ShopType.Blacksmith)
            result.forEach { inventoryItem ->
                val baseAmount = inventoryItem.item.price.amount
                val adjustedAmount = inventoryItem.adjustedPrice.amount
                val lowerBound = (baseAmount * 0.90).toInt()
                val upperBound = (baseAmount * 1.10).toInt() + 1 // +1 for rounding tolerance
                assertEquals(
                    inventoryItem.item.price.denomination,
                    inventoryItem.adjustedPrice.denomination,
                    "Denomination should not change for '${inventoryItem.item.name}'"
                )
                assertTrue(
                    adjustedAmount in lowerBound..upperBound,
                    "Price $adjustedAmount not within ±10% of base $baseAmount for '${inventoryItem.item.name}'"
                )
            }
        }
    }

    @Test
    fun `price variance keeps amount at least 1`() = runTest {
        val useCase = createUseCase(random = Random(42))
        val result = useCase.invoke(ShopType.Blacksmith)
        result.forEach { inventoryItem ->
            assertTrue(inventoryItem.adjustedPrice.amount >= 1)
        }
    }

    // ---- Rarity distribution tests (tested indirectly through invoke) ----

    @Test
    fun `rarity distribution favors common items over large sample`() = runTest {
        val counts = mutableMapOf<Rarity, Int>()

        for (seed in 0 until 200) {
            val useCase = createUseCase(random = Random(seed))
            val result = useCase(ShopType.Blacksmith)
            result.forEach { item ->
                counts[item.item.rarity] = (counts[item.item.rarity] ?: 0) + 1
            }
        }

        // Common should appear more than any single non-common rarity
        val commonCount = counts[Rarity.Common] ?: 0
        val legendaryCount = counts[Rarity.Legendary] ?: 0
        assertTrue(commonCount > legendaryCount, "Common ($commonCount) should outnumber Legendary ($legendaryCount)")
    }

    // ---- Deterministic with seeded Random ----

    @Test
    fun `same seed produces identical results`() = runTest {
        val result1 = createUseCase(random = Random(12345)).invoke(ShopType.Blacksmith)
        val result2 = createUseCase(random = Random(12345)).invoke(ShopType.Blacksmith)

        assertEquals(result1.size, result2.size)
        result1.zip(result2).forEach { (a, b) ->
            assertEquals(a.item.id, b.item.id)
            assertEquals(a.adjustedPrice, b.adjustedPrice)
            assertEquals(a.quantity, b.quantity)
        }
    }

    @Test
    fun `different seeds produce different results`() = runTest {
        val result1 = createUseCase(random = Random(1)).invoke(ShopType.Blacksmith)
        val result2 = createUseCase(random = Random(99999)).invoke(ShopType.Blacksmith)

        // At least one item or price should differ (extremely unlikely to be identical)
        val itemIds1 = result1.map { it.item.id }
        val itemIds2 = result2.map { it.item.id }
        val pricesMatch = result1.map { it.adjustedPrice } == result2.map { it.adjustedPrice }

        assertTrue(
            itemIds1 != itemIds2 || !pricesMatch || result1.size != result2.size,
            "Two different seeds produced identical inventories"
        )
    }

    // ---- Quantity generation tests ----

    @Test
    fun `generated quantities are valid`() = runTest {
        repeat(20) { seed ->
            val useCase = createUseCase(random = Random(seed))
            val result = useCase.invoke(ShopType.Blacksmith)
            result.forEach { inventoryItem ->
                val qty = inventoryItem.quantity
                assertTrue(
                    qty == null || qty in 1..10,
                    "Invalid quantity: $qty"
                )
            }
        }
    }

    @Test
    fun `some items have unlimited stock over many generations`() = runTest {
        var foundUnlimited = false
        for (seed in 0 until 100) {
            val useCase = createUseCase(random = Random(seed))
            val result = useCase.invoke(ShopType.Blacksmith)
            if (result.any { it.isUnlimitedStock }) {
                foundUnlimited = true
                break
            }
        }
        assertTrue(foundUnlimited, "Expected at least some items with unlimited stock across 100 generations")
    }
}
