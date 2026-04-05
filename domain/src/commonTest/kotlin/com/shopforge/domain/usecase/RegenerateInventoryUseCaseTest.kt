package com.shopforge.domain.usecase

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.repository.ItemRepository
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RegenerateInventoryUseCaseTest {

    private val repository = FakeShopRepository()
    private val createUseCase = CreateShopUseCase(repository, clock = { TestFixtures.FIXED_TIME })
    private val addItemUseCase = AddItemToShopUseCase(repository)

    /**
     * A fake ItemRepository that records which categories were queried and returns
     * a fixed list of items, allowing us to verify category filtering behavior.
     */
    private class RecordingItemRepository(
        private val itemsByCategory: Map<ItemCategory, List<Item>> = emptyMap(),
    ) : ItemRepository {
        val queriedCategories = mutableListOf<ItemCategory>()

        override fun getAllItems() = flowOf(itemsByCategory.values.flatten())
        override suspend fun getItemsByCategory(category: ItemCategory): List<Item> {
            queriedCategories.add(category)
            return itemsByCategory[category] ?: emptyList()
        }
        override suspend fun getItemsByRarity(rarity: Rarity) = emptyList<Item>()
        override suspend fun searchItems(query: String) = emptyList<Item>()
        override suspend fun getItemById(id: Long) = null
        override suspend fun createCustomItem(item: Item) = 0L
        override suspend fun updateCustomItem(item: Item) {}
        override suspend fun deleteCustomItem(id: Long) {}
    }

    private fun makeItem(id: Long, name: String, category: ItemCategory) = Item(
        id = id,
        name = name,
        description = null,
        category = category,
        price = Price.ofGold(10),
        rarity = Rarity.Common,
        isCustom = false,
    )

    @Test
    fun `replaces existing inventory with generated items`() = runTest {
        val shopId = createUseCase("Smithy", ShopType.Blacksmith, null)
        val oldItem = TestFixtures.sampleItem(id = 1L, name = "Old Sword")
        addItemUseCase(shopId, oldItem, 5, oldItem.price)

        // Provide enough Weapon and Armor items (Blacksmith categories) so that
        // GenerateInventoryUseCase can fill an 8-15 item inventory
        val weapons = (1L..10L).map { makeItem(it, "Weapon $it", ItemCategory.Weapon) }
        val armors = (11L..20L).map { makeItem(it, "Armor $it", ItemCategory.Armor) }
        val itemRepo = RecordingItemRepository(
            mapOf(
                ItemCategory.Weapon to weapons,
                ItemCategory.Armor to armors,
            )
        )
        val generator = GenerateInventoryUseCase(itemRepo)
        val useCase = RegenerateInventoryUseCase(repository, generator)
        val shop = repository.getShopSnapshot(shopId)!!
        useCase(shopId, shop.type)

        val inventory = repository.getInventorySnapshot(shopId)
        // Old item should be gone; new inventory should contain 8-15 items
        assertTrue(inventory.none { it.item.name == "Old Sword" }, "Old item should have been replaced")
        assertTrue(inventory.size in 8..15, "Expected 8-15 items but got ${inventory.size}")
    }

    @Test
    fun `uses shop type for generation`() = runTest {
        val shopId = createUseCase("Magic Place", ShopType.MagicShop, null)

        val potions = (1L..15L).map { makeItem(it, "Potion $it", ItemCategory.Potion) }
        val magicItems = (16L..25L).map { makeItem(it, "Magic Item $it", ItemCategory.MagicItem) }
        val recordingRepo = RecordingItemRepository(
            mapOf(
                ItemCategory.Potion to potions,
                ItemCategory.MagicItem to magicItems,
            )
        )
        val generator = GenerateInventoryUseCase(recordingRepo)
        val useCase = RegenerateInventoryUseCase(repository, generator)
        val shop = repository.getShopSnapshot(shopId)!!
        useCase(shopId, shop.type)

        // MagicShop default categories should have been queried
        val expectedCategories = ShopType.MagicShop.defaultCategories.toSet()
        assertTrue(
            recordingRepo.queriedCategories.isNotEmpty(),
            "Expected generator to query categories for MagicShop"
        )
        recordingRepo.queriedCategories.forEach { category ->
            assertTrue(
                category in expectedCategories,
                "Category $category should be in MagicShop's default categories"
            )
        }
    }

    @Test
    fun `throws for nonexistent shop`() = runTest {
        val emptyRepo = RecordingItemRepository()
        val generator = GenerateInventoryUseCase(emptyRepo)
        val useCase = RegenerateInventoryUseCase(repository, generator)

        assertFailsWith<NoSuchElementException> {
            useCase(999L, ShopType.Blacksmith)
        }
    }

    @Test
    fun `clears inventory when generator returns empty list`() = runTest {
        val shopId = createUseCase("Shop", ShopType.Tavern, null)
        val item = TestFixtures.sampleItem(id = 1L)
        addItemUseCase(shopId, item, 5, item.price)

        // Provide no items for Tavern categories so generator returns empty
        val emptyRepo = RecordingItemRepository()
        val generator = GenerateInventoryUseCase(emptyRepo)
        val useCase = RegenerateInventoryUseCase(repository, generator)
        val shop = repository.getShopSnapshot(shopId)!!
        useCase(shopId, shop.type)

        val inventory = repository.getInventorySnapshot(shopId)
        assertEquals(0, inventory.size)
    }
}
