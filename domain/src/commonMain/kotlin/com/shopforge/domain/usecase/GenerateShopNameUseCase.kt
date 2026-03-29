package com.shopforge.domain.usecase

import com.shopforge.domain.model.ShopType
import kotlin.random.Random

/**
 * Generates a thematic shop name based on the given [ShopType].
 *
 * Each shop type has a curated pool of name templates that evoke
 * the fantasy atmosphere appropriate for that kind of shop.
 *
 * Accepts a [Random] parameter for testability — callers can pass
 * a seeded instance to produce deterministic output.
 */
class GenerateShopNameUseCase {

    /**
     * Returns a randomly selected thematic name for the given [shopType].
     *
     * @param shopType The type of shop to generate a name for.
     * @param random A [Random] instance for selecting from the name pool.
     * @return A thematic shop name string.
     */
    operator fun invoke(shopType: ShopType, random: Random = Random): String {
        val names = nameTemplates.getValue(shopType)
        return names[random.nextInt(names.size)]
    }

    companion object {
        /**
         * Name template pools keyed by [ShopType].
         * Each type has at least 10 templates for variety.
         */
        internal val nameTemplates: Map<ShopType, List<String>> = mapOf(
            ShopType.Blacksmith to listOf(
                "The Gilded Anvil",
                "The Iron Forge",
                "Hammer & Tongs",
                "The Steel Crucible",
                "The Burning Brand",
                "The Tempered Edge",
                "Ironheart Smithy",
                "The Molten Crown",
                "Cinderfall Armory",
                "The Rusted Gauntlet",
            ),
            ShopType.MagicShop to listOf(
                "The Arcane Emporium",
                "Starfall Curios",
                "The Whispering Wand",
                "Mystic Convergence",
                "The Enchanted Quill",
                "The Shimmering Veil",
                "Astral Trinkets",
                "The Violet Sigil",
                "Moonweave Magicks",
                "The Crystal Sanctum",
            ),
            ShopType.GeneralStore to listOf(
                "The Adventurer's Pack",
                "Crossroads Supply",
                "The Sturdy Crate",
                "Hearthstone Provisions",
                "The Copper Coin Trading Post",
                "Trail & Hearth Goods",
                "The Well-Stocked Shelf",
                "Wayfarers' Supplies",
                "The Dusty Barrel",
                "Tinker & Stow",
            ),
            ShopType.Alchemist to listOf(
                "The Bubbling Flask",
                "Verdant Remedies",
                "The Philosopher's Retort",
                "Nightshade & Nettle",
                "The Smoking Cauldron",
                "Emberroot Apothecary",
                "The Distilled Spirit",
                "Foxglove Elixirs",
                "The Corroded Pipette",
                "Alchemical Wonders",
            ),
            ShopType.Fletcher to listOf(
                "The Feathered Shaft",
                "Trueshot Bowcraft",
                "The Bent Yew",
                "Hawkeye Arrowworks",
                "The Singing String",
                "Windborne Fletcher",
                "The Quiver & Stave",
                "Ashwood Bows",
                "The Piercing Point",
                "Swiftwind Archery",
            ),
            ShopType.Tavern to listOf(
                "The Prancing Pony",
                "The Rusty Flagon",
                "The Drunken Dragon",
                "Hearthfire Inn",
                "The Wanderer's Rest",
                "The Golden Tankard",
                "The Sleeping Griffin",
                "Barley & Hops Taproom",
                "The Broken Barrel",
                "The Merry Minstrel",
            ),
            ShopType.Temple to listOf(
                "The Sacred Flame",
                "Dawnshroud Sanctuary",
                "The Silver Chalice",
                "The Hallowed Threshold",
                "Radiant Grace Chapel",
                "The Blessed Reliquary",
                "The Sunlit Altar",
                "Mercy's Embrace",
                "The Anointed Path",
                "Lightkeeper's Shrine",
            ),
            ShopType.ExoticGoods to listOf(
                "The Wandering Bazaar",
                "Silken Road Curiosities",
                "The Collector's Trove",
                "Oddments & Wonders",
                "The Gilded Parrot",
                "Far Shore Imports",
                "The Curio Cabinet",
                "Mirage Market",
                "The Peculiar Emporium",
                "Windswept Rarities",
            ),
        )
    }
}
