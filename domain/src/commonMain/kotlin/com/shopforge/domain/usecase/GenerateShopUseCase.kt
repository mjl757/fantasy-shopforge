package com.shopforge.domain.usecase

import com.shopforge.domain.model.Shop
import com.shopforge.domain.model.ShopType
import com.shopforge.domain.model.ShopWithInventory
import com.shopforge.domain.repository.ShopRepository
import kotlin.random.Random

/**
 * Orchestrates full shop generation: picks a type (if random), generates
 * a thematic name, generates inventory, persists the shop, and returns
 * the complete [Shop] with its inventory.
 *
 * @param shopRepository Repository for persisting the generated shop.
 * @param generateShopName Use case for generating a thematic shop name.
 * @param generateInventory Use case for generating shop inventory items.
 * @param clock Provides the current time in epoch milliseconds.
 */
class GenerateShopUseCase(
    private val shopRepository: ShopRepository,
    private val generateShopName: GenerateShopNameUseCase,
    private val generateInventory: GenerateInventoryUseCase,
    private val clock: () -> Long,
) {

    /**
     * Generates a complete shop with inventory.
     *
     * @param shopType The type of shop to generate. If null, a random type is chosen.
     * @param random A [Random] instance for deterministic testing.
     * @return A [ShopWithInventory] containing the persisted shop and its generated inventory.
     */
    suspend operator fun invoke(
        shopType: ShopType? = null,
        random: Random = Random,
    ): ShopWithInventory {
        val type = shopType ?: randomShopType(random)
        val name = generateShopName(type, random)
        val now = clock()

        val shop = Shop(
            id = 0L, // Will be replaced by the database-generated id
            name = name,
            type = type,
            description = null,
            createdAt = now,
            updatedAt = now,
        )

        val shopId = shopRepository.createShop(shop)
        val persistedShop = shop.copy(id = shopId)

        val inventory = generateInventory(type, random)
        shopRepository.replaceInventory(shopId, inventory)

        return ShopWithInventory(shop = persistedShop, inventory = inventory)
    }

    private fun randomShopType(random: Random): ShopType {
        val types = ShopType.entries
        return types[random.nextInt(types.size)]
    }
}
