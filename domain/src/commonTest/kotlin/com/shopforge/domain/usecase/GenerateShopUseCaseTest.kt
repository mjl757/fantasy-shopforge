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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GenerateShopUseCaseTest {

    private val catalogItems = listOf(
        Item(id = 1, name = "Longsword", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 2, name = "Shortsword", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 3, name = "Dagger", category = ItemCategory.Weapon, price = Price.ofGold(2), rarity = Rarity.Common, isCustom = false),
        Item(id = 4, name = "Battleaxe", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 5, name = "Warhammer", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 6, name = "Chain Mail", category = ItemCategory.Armor, price = Price.ofGold(75), rarity = Rarity.Common, isCustom = false),
        Item(id = 7, name = "Leather Armor", category = ItemCategory.Armor, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 8, name = "Plate Armor", category = ItemCategory.Armor, price = Price.ofGold(1500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 9, name = "Wooden Shield", category = ItemCategory.Armor, price = Price.ofGold(5), rarity = Rarity.Common, isCustom = false),
        Item(id = 10, name = "Potion of Healing", category = ItemCategory.Potion, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),
        Item(id = 11, name = "Wand of Sparks", category = ItemCategory.MagicItem, price = Price.ofGold(500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 12, name = "Cloak of Shadows", category = ItemCategory.MagicItem, price = Price.ofGold(5000), rarity = Rarity.Rare, isCustom = false),
    )

    @Test
    fun invokeWithSpecificTypeCreatesShopOfThatType() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val shopId = useCase(ShopType.Blacksmith)

        val createdShop = shopRepo.getShopSnapshot(shopId)
        assertEquals(ShopType.Blacksmith, createdShop?.type)
    }

    @Test
    fun invokeWithNullTypePicksRandomType() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val shopId = useCase(null)

        val createdShop = shopRepo.getShopSnapshot(shopId)
        assertNotNull(createdShop)
        assertTrue(ShopType.entries.contains(createdShop.type))
    }

    @Test
    fun generatedShopHasThematicName() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val shopId = useCase(ShopType.Blacksmith)

        val name = shopRepo.getShopSnapshot(shopId)?.name ?: ""
        assertTrue(name.isNotBlank(), "Name should not be blank")
        assertTrue(name.contains(" "), "Name should have prefix and suffix like 'The Golden Anvil'")
    }

    @Test
    fun generatedInventoryHas8To15Items() = runTest {
        // Run multiple times with different seeds to test the range
        for (seed in 1..20) {
            val shopRepo = FakeShopRepository()
            val useCaseWithSeed = createUseCase(shopRepo, seed = seed)
            val shopId = useCaseWithSeed(ShopType.Blacksmith)

            val inventorySize = shopRepo.getInventorySnapshot(shopId).size
            assertTrue(
                inventorySize in 8..15,
                "Inventory size should be 8-15 but was $inventorySize (seed=$seed)"
            )
        }
    }

    @Test
    fun inventoryItemsMatchShopTypeCategories() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val shopId = useCase(ShopType.Blacksmith)

        val inventory = shopRepo.getInventorySnapshot(shopId)
        val expectedCategories = ShopType.Blacksmith.defaultCategories
        inventory.forEach { invItem ->
            assertTrue(
                invItem.item.category in expectedCategories,
                "Item '${invItem.item.name}' (${invItem.item.category}) should be in $expectedCategories"
            )
        }
    }

    @Test
    fun priceVarianceIsWithin10Percent() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val shopId = useCase(ShopType.Blacksmith)

        val inventory = shopRepo.getInventorySnapshot(shopId)
        inventory.forEach { invItem ->
            val baseCp = invItem.item.price.copperPieces
            val adjustedCp = invItem.adjustedPrice.copperPieces
            val lowerBound = (baseCp * 0.89).toLong() // slight tolerance for rounding
            val upperBound = (baseCp * 1.11).toLong()
            assertTrue(
                adjustedCp in lowerBound..upperBound,
                "Adjusted price $adjustedCp should be within +-10% of base $baseCp for '${invItem.item.name}'"
            )
        }
    }

    @Test
    fun quantityIsNullOrPositiveForAllItems() = runTest {
        // Use many seeds to exercise both null (unlimited) and non-null quantities
        for (seed in 1..20) {
            val shopRepo = FakeShopRepository()
            val useCase = createUseCase(shopRepo, seed = seed)
            val shopId = useCase(ShopType.Blacksmith)

            val inventory = shopRepo.getInventorySnapshot(shopId)
            inventory.forEach { invItem ->
                val qty = invItem.quantity
                assertTrue(
                    qty == null || qty > 0,
                    "Quantity should be null (unlimited) or positive, but was $qty for '${invItem.item.name}'"
                )
            }
        }
    }

    @Test
    fun generateShopNameProducesValidNamesForAllTypes() {
        val useCase = createUseCase(FakeShopRepository())

        ShopType.entries.forEach { type ->
            val name = useCase.generateShopName(type)
            assertTrue(name.isNotBlank(), "Name for $type should not be blank")
            assertTrue(name.startsWith("The "), "Name for $type should start with 'The ': $name")
        }
    }

    @Test
    fun returnsGeneratedShopId() = runTest {
        val shopRepo = FakeShopRepository()
        val useCase = createUseCase(shopRepo)

        val id = useCase(ShopType.MagicShop)

        assertEquals(1L, id, "Should return the ID from the repository")
    }

    // --- Helpers ---

    private fun createUseCase(
        shopRepository: FakeShopRepository = FakeShopRepository(),
        seed: Int = 42,
    ): GenerateShopUseCase {
        val itemRepo = FakeItemRepository(catalogItems)
        val generateInventoryUseCase = GenerateInventoryUseCase(itemRepo)
        return GenerateShopUseCase(
            shopRepository = shopRepository,
            generateInventoryUseCase = generateInventoryUseCase,
            random = Random(seed),
            clock = { 1000L },
        )
    }
}

private class FakeItemRepository(private val items: List<Item>) : ItemRepository {
    override fun getAllItems(): Flow<List<Item>> = flowOf(items)
    override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
        items.filter { it.category == category }
    override suspend fun getItemsByRarity(rarity: Rarity): List<Item> =
        items.filter { it.rarity == rarity }
    override suspend fun searchItems(query: String): List<Item> =
        items.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun getItemById(id: Long): Item? = items.find { it.id == id }
    override suspend fun createCustomItem(item: Item): Long = item.id
    override suspend fun updateCustomItem(item: Item) {}
    override suspend fun deleteCustomItem(id: Long) {}
}
