package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
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
        Item(1, "Longsword", null, ItemCategory.Weapon, Price.ofGold(15), Rarity.Common, false),
        Item(2, "Shortsword", null, ItemCategory.Weapon, Price.ofGold(10), Rarity.Common, false),
        Item(3, "Greatsword", null, ItemCategory.Weapon, Price.ofGold(50), Rarity.Uncommon, false),
        Item(4, "Flame Tongue", "A magical flaming blade", ItemCategory.Weapon, Price.ofGold(500), Rarity.Rare, false),
        Item(5, "Vorpal Sword", "Snicker-snack", ItemCategory.Weapon, Price.ofGold(2000), Rarity.VeryRare, false),
        Item(6, "Holy Avenger", "A legendary blade", ItemCategory.Weapon, Price.ofGold(5000), Rarity.Legendary, false),
        // Armor (Blacksmith)
        Item(7, "Leather Armor", null, ItemCategory.Armor, Price.ofGold(10), Rarity.Common, false),
        Item(8, "Chain Mail", null, ItemCategory.Armor, Price.ofGold(75), Rarity.Common, false),
        Item(9, "Plate Armor", null, ItemCategory.Armor, Price.ofGold(1500), Rarity.Uncommon, false),
        Item(10, "Mithral Half Plate", null, ItemCategory.Armor, Price.ofGold(3000), Rarity.Rare, false),
        Item(11, "Shield +1", null, ItemCategory.Armor, Price.ofGold(200), Rarity.Uncommon, false),
        Item(12, "Adamantine Plate", null, ItemCategory.Armor, Price.ofGold(4000), Rarity.VeryRare, false),
        Item(13, "Shield +3", null, ItemCategory.Armor, Price.ofGold(8000), Rarity.Legendary, false),
        // Potions (Alchemist, MagicShop, Temple)
        Item(14, "Healing Potion", null, ItemCategory.Potion, Price.ofGold(50), Rarity.Common, false),
        Item(15, "Greater Healing Potion", null, ItemCategory.Potion, Price.ofGold(100), Rarity.Uncommon, false),
        // Adventuring Gear (General Store)
        Item(16, "Rope (50 ft)", null, ItemCategory.AdventuringGear, Price.ofGold(1), Rarity.Common, false),
        Item(17, "Torch (10-pack)", null, ItemCategory.AdventuringGear, Price.ofSilver(1), Rarity.Common, false),
        // Ammunition (Fletcher)
        Item(18, "Arrows (20)", null, ItemCategory.Ammunition, Price.ofGold(1), Rarity.Common, false),
        Item(19, "Bolts (20)", null, ItemCategory.Ammunition, Price.ofGold(1), Rarity.Common, false),
    )

    private fun createUseCase(items: List<Item> = testCatalog): GenerateInventoryUseCase {
        return GenerateInventoryUseCase(FakeItemRepository(items))
    }

    // ---- Inventory size tests ----

    @Test
    fun `generated inventory size is between 8 and 15`() = runTest {
        val useCase = createUseCase()
        // Run multiple times with different seeds
        repeat(50) { seed ->
            val result = useCase.invoke(ShopType.Blacksmith, Random(seed))
            assertTrue(
                result.size in 8..15,
                "Inventory size ${result.size} not in expected range for seed $seed"
            )
        }
    }

    @Test
    fun `inventory size is capped by available candidates`() = runTest {
        // Only 2 items available for General Store (AdventuringGear + Food)
        val useCase = createUseCase()
        val result = useCase.invoke(ShopType.GeneralStore, Random(42))
        // GeneralStore categories: AdventuringGear, Food -> only 2 items in our catalog
        assertEquals(2, result.size)
    }

    @Test
    fun `empty catalog returns empty inventory`() = runTest {
        val useCase = createUseCase(emptyList())
        val result = useCase.invoke(ShopType.Blacksmith, Random(42))
        assertTrue(result.isEmpty())
    }

    // ---- Category filtering tests ----

    @Test
    fun `items are filtered by shop type default categories`() = runTest {
        val useCase = createUseCase()
        val result = useCase.invoke(ShopType.Blacksmith, Random(42))
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
        val useCase = createUseCase()
        val result = useCase.invoke(ShopType.Fletcher, Random(42))
        val allowedCategories = ShopType.Fletcher.defaultCategories.toSet()
        result.forEach { inventoryItem ->
            assertTrue(inventoryItem.item.category in allowedCategories)
        }
    }

    // ---- No duplicates test ----

    @Test
    fun `no duplicate items in generated inventory`() = runTest {
        val useCase = createUseCase()
        repeat(50) { seed ->
            val result = useCase.invoke(ShopType.Blacksmith, Random(seed))
            val ids = result.map { it.item.id }
            assertEquals(ids.size, ids.toSet().size, "Duplicate items found for seed $seed")
        }
    }

    // ---- Price variance tests ----

    @Test
    fun `price variance is within plus minus 10 percent`() = runTest {
        val useCase = createUseCase()
        repeat(50) { seed ->
            val result = useCase.invoke(ShopType.Blacksmith, Random(seed))
            result.forEach { inventoryItem ->
                val baseCp = inventoryItem.item.price.copperPieces
                val adjustedCp = inventoryItem.adjustedPrice.copperPieces
                val lowerBound = (baseCp * 0.90).toLong()
                val upperBound = (baseCp * 1.10).toLong() + 1 // +1 for rounding tolerance
                assertTrue(
                    adjustedCp in lowerBound..upperBound,
                    "Price $adjustedCp CP not within ±10% of base $baseCp CP for '${inventoryItem.item.name}'"
                )
            }
        }
    }

    @Test
    fun `price variance rounds to nearest copper piece`() = runTest {
        val useCase = createUseCase()
        val result = useCase.invoke(ShopType.Blacksmith, Random(42))
        result.forEach { inventoryItem ->
            // Price is always stored as whole copper pieces
            assertTrue(inventoryItem.adjustedPrice.copperPieces >= 1)
        }
    }

    // ---- Rarity distribution tests (tested indirectly through invoke) ----

    @Test
    fun `rarity distribution favors common items over large sample`() = runTest {
        val useCase = createUseCase()
        val counts = mutableMapOf<Rarity, Int>()

        for (seed in 0 until 200) {
            val result = useCase(ShopType.Blacksmith, Random(seed))
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
        val useCase = createUseCase()
        val result1 = useCase.invoke(ShopType.Blacksmith, Random(12345))
        val result2 = useCase.invoke(ShopType.Blacksmith, Random(12345))

        assertEquals(result1.size, result2.size)
        result1.zip(result2).forEach { (a, b) ->
            assertEquals(a.item.id, b.item.id)
            assertEquals(a.adjustedPrice, b.adjustedPrice)
            assertEquals(a.quantity, b.quantity)
        }
    }

    @Test
    fun `different seeds produce different results`() = runTest {
        val useCase = createUseCase()
        val result1 = useCase.invoke(ShopType.Blacksmith, Random(1))
        val result2 = useCase.invoke(ShopType.Blacksmith, Random(99999))

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
        val useCase = createUseCase()
        repeat(20) { seed ->
            val result = useCase.invoke(ShopType.Blacksmith, Random(seed))
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
        val useCase = createUseCase()
        var foundUnlimited = false
        for (seed in 0 until 100) {
            val result = useCase.invoke(ShopType.Blacksmith, Random(seed))
            if (result.any { it.isUnlimitedStock }) {
                foundUnlimited = true
                break
            }
        }
        assertTrue(foundUnlimited, "Expected at least some items with unlimited stock across 100 generations")
    }
}
