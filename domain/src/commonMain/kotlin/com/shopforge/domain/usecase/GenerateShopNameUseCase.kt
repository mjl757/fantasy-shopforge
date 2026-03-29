package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.random.Random

/**
 * Generates a thematic shop name based on the given [ShopType].
 *
 * Each shop type has a pool of name templates for variety.
 * Accepts a [Random] instance for testability.
 */
class GenerateShopNameUseCase(
    private val random: Random = Random.Default,
) {

    operator fun invoke(shopType: ShopType): String {
        val templates = nameTemplates[shopType] ?: error("No name templates for $shopType")
        return templates[random.nextInt(templates.size)]
    }

    companion object {
        val nameTemplates: Map<ShopType, List<String>> = mapOf(
            ShopType.Blacksmith to listOf(
                "The Gilded Anvil",
                "The Iron Forge",
                "Hammer & Tongs",
                "The Steel Gauntlet",
                "The Burning Edge",
                "The Crimson Smithy",
                "The Dwarven Hammer",
                "Ironheart Armory",
                "The Tempered Blade",
                "The Molten Crown",
            ),
            ShopType.MagicShop to listOf(
                "The Arcane Emporium",
                "Mystic Wonders",
                "The Enchanted Quill",
                "The Shimmering Sanctum",
                "Starweave Magics",
                "The Crystal Spire",
                "The Wizard's Trove",
                "The Ethereal Bazaar",
                "Moonlit Arcana",
                "The Runewood Cabinet",
            ),
            ShopType.GeneralStore to listOf(
                "The Adventurer's Pack",
                "The Copper Kettle",
                "Goodman's Goods",
                "The Dusty Lantern",
                "The Wayfarer's Rest",
                "Pathfinder Provisions",
                "The Supply Post",
                "The Sturdy Satchel",
                "The Tinker's Shelf",
                "Crossroads General",
            ),
            ShopType.Alchemist to listOf(
                "The Bubbling Cauldron",
                "Elixirs & Essences",
                "The Green Flask",
                "The Alchemist's Den",
                "Vial & Virtue",
                "The Smoking Mortar",
                "The Distilled Dragon",
                "Potion Masters",
                "The Herbal Remedy",
                "The Volatile Vat",
            ),
            ShopType.Fletcher to listOf(
                "The True Arrow",
                "The Feathered Shaft",
                "Bowstring & Bolt",
                "The Eagle's Eye",
                "The Silver Quiver",
                "The Ranger's Mark",
                "Swiftstrike Archery",
                "The Longbow Lodge",
                "The Piercing Point",
                "Windwalker Bows",
            ),
            ShopType.Tavern to listOf(
                "The Golden Flagon",
                "The Prancing Pony",
                "The Rusty Tankard",
                "The Drunken Dragon",
                "The Hearthstone Inn",
                "The Jolly Bard",
                "The Warm Hearth",
                "The Sleeping Giant",
                "The Merry Minstrel",
                "The Barley & Hops",
            ),
            ShopType.Temple to listOf(
                "The Sacred Flame",
                "The Blessed Reliquary",
                "The Holy Chalice",
                "The Shining Altar",
                "Sanctuary of Light",
                "The Healing Hand",
                "The Divine Covenant",
                "The Silver Censer",
                "The Hallowed Grove",
                "The Pilgrim's Rest",
            ),
            ShopType.ExoticGoods to listOf(
                "The Curious Cabinet",
                "Wonders of the World",
                "The Rare Find",
                "The Midnight Market",
                "The Peculiar Purveyor",
                "The Wanderer's Collection",
                "The Gilded Curiosity",
                "Treasures Untold",
                "The Oddments Emporium",
                "The Far Shore Trading Co.",
            ),
        )
    }
}
