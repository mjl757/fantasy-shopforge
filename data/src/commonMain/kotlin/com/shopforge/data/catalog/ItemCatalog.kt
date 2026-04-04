package com.shopforge.data.catalog

import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity

/**
 * The built-in item catalog for Fantasy ShopForge.
 * Contains 40 original generic fantasy items across all categories.
 *
 * All prices are stored in copper pieces (CP).
 * Conversion: 1 GP = 100 CP, 1 SP = 10 CP, 1 PP = 1000 CP.
 *
 * Pricing guidelines:
 *   Common:    0.1 - 50 GP    (10 - 5,000 CP)
 *   Uncommon:  50 - 500 GP    (5,000 - 50,000 CP)
 *   Rare:      500 - 5,000 GP (50,000 - 500,000 CP)
 *   Very Rare: 5,000 - 50,000 GP (500,000 - 5,000,000 CP)
 *   Legendary: 50,000+ GP     (5,000,000+ CP)
 */
object ItemCatalog {

    val items: List<CatalogItem> = listOf(
        // ---- Weapons (5 items) ----
        CatalogItem(
            name = "Longsword",
            description = "A versatile blade favored by knights and sellswords alike.",
            category = ItemCategory.Weapon,
            priceCopper = 1_500L, // 15 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Shortsword",
            description = "A light, quick blade ideal for close-quarters combat.",
            category = ItemCategory.Weapon,
            priceCopper = 1_000L, // 10 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Greataxe",
            description = "A massive two-handed axe that cleaves through armor and bone.",
            category = ItemCategory.Weapon,
            priceCopper = 3_000L, // 30 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Dagger",
            description = "A small, concealable blade useful for both combat and utility.",
            category = ItemCategory.Weapon,
            priceCopper = 200L, // 2 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Warhammer",
            description = "A heavy hammer designed to crush shields and dent plate armor.",
            category = ItemCategory.Weapon,
            priceCopper = 1_500L, // 15 GP
            rarity = Rarity.Common,
        ),
        // ---- Armor (4 items) ----
        CatalogItem(
            name = "Chain Mail",
            description = "Interlocking metal rings that provide solid protection against slashing attacks.",
            category = ItemCategory.Armor,
            priceCopper = 7_500L, // 75 GP
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Leather Armor",
            description = "Lightweight armor crafted from cured hides, offering mobility with modest protection.",
            category = ItemCategory.Armor,
            priceCopper = 1_000L, // 10 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Shield",
            description = "A sturdy wooden shield reinforced with iron bands.",
            category = ItemCategory.Armor,
            priceCopper = 1_000L, // 10 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Plate Armor",
            description = "Full suit of interlocking metal plates offering the finest mundane protection available.",
            category = ItemCategory.Armor,
            priceCopper = 150_000L, // 1,500 GP
            rarity = Rarity.Rare,
        ),

        // ---- Ammunition (2 items) ----
        CatalogItem(
            name = "Arrows (20)",
            description = "A quiver of twenty standard wooden arrows with iron tips.",
            category = ItemCategory.Ammunition,
            priceCopper = 100L, // 1 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Crossbow Bolts (20)",
            description = "Twenty short, heavy bolts designed for crossbows.",
            category = ItemCategory.Ammunition,
            priceCopper = 100L, // 1 GP
            rarity = Rarity.Common,
        ),

        // ---- Potions (5 items) ----
        CatalogItem(
            name = "Healing Potion",
            description = "A small vial of glowing red liquid that mends wounds when consumed.",
            category = ItemCategory.Potion,
            priceCopper = 5_000L, // 50 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Greater Healing Potion",
            description = "A larger flask of potent crimson elixir capable of closing grievous injuries.",
            category = ItemCategory.Potion,
            priceCopper = 15_000L, // 150 GP
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Antidote",
            description = "A bitter herbal brew that neutralizes most common poisons.",
            category = ItemCategory.Potion,
            priceCopper = 5_000L, // 50 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Invisibility Potion",
            description = "A shimmering, nearly transparent liquid that renders the drinker unseen.",
            category = ItemCategory.Potion,
            priceCopper = 50_000L, // 500 GP
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Potion of Giant Strength",
            description = "A thick, earthy draught that grants the drinker immense physical power.",
            category = ItemCategory.Potion,
            priceCopper = 100_000L, // 1,000 GP
            rarity = Rarity.Rare,
        ),

        // ---- Adventuring Gear (6 items) ----
        CatalogItem(
            name = "Rope (50ft)",
            description = "Fifty feet of sturdy hempen rope, essential for climbing and binding.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 100L, // 1 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Torch",
            description = "A wooden shaft wrapped in oil-soaked cloth that burns for about an hour.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 1L, // 1 CP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Rations (1 day)",
            description = "Dried meat, hard bread, and nuts sufficient for one day of travel.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 50L, // 5 SP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Backpack",
            description = "A sturdy leather pack with multiple compartments for gear.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 200L, // 2 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Bedroll",
            description = "A warm, rolled sleeping mat for camping in the wilderness.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 100L, // 1 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Tinderbox",
            description = "A small kit containing flint, firesteel, and tinder for starting fires.",
            category = ItemCategory.AdventuringGear,
            priceCopper = 50L, // 5 SP
            rarity = Rarity.Common,
        ),

        // ---- Magic Items (5 items) ----
        CatalogItem(
            name = "Cloak of Protection",
            description = "An enchanted cloak woven with protective wards that deflect harm.",
            category = ItemCategory.MagicItem,
            priceCopper = 350_000L, // 3,500 GP
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Ring of Resistance",
            description = "A silver ring imbued with elemental magic, granting resistance to a chosen element.",
            category = ItemCategory.MagicItem,
            priceCopper = 600_000L, // 6,000 GP
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Wand of Sparks",
            description = "A slender wand that crackles with arcane energy, discharging bolts of lightning.",
            category = ItemCategory.MagicItem,
            priceCopper = 75_000L, // 750 GP
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Amulet of Health",
            description = "A golden amulet bearing a ruby that fortifies the wearer's vitality.",
            category = ItemCategory.MagicItem,
            priceCopper = 800_000L, // 8,000 GP
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Staff of the Archmage",
            description = "An ancient staff of immense power, sought by the most powerful spellcasters.",
            category = ItemCategory.MagicItem,
            priceCopper = 10_000_000L, // 100,000 GP
            rarity = Rarity.Legendary,
        ),

        // ---- Food & Drink (4 items) ----
        CatalogItem(
            name = "Ale (Mug)",
            description = "A frothy mug of hearty ale brewed by the local tavern.",
            category = ItemCategory.Food,
            priceCopper = 4L, // 4 CP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Wine (Bottle)",
            description = "A bottle of red wine from a reputable vineyard.",
            category = ItemCategory.Food,
            priceCopper = 30L, // 3 SP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Bread & Stew",
            description = "A bowl of thick stew served with a chunk of fresh bread.",
            category = ItemCategory.Food,
            priceCopper = 5L, // 5 CP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Fine Meal",
            description = "A lavish multi-course dinner with roast meats, fine cheese, and dessert.",
            category = ItemCategory.Food,
            priceCopper = 100L, // 1 GP
            rarity = Rarity.Common,
        ),

        // ---- Holy Items (3 items) ----
        CatalogItem(
            name = "Holy Symbol",
            description = "A consecrated emblem of a deity, used as a focus for divine magic.",
            category = ItemCategory.HolyItem,
            priceCopper = 500L, // 5 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Blessed Water",
            description = "A flask of water sanctified by a priest, harmful to undead and fiends.",
            category = ItemCategory.HolyItem,
            priceCopper = 2_500L, // 25 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Divine Scroll",
            description = "A parchment inscribed with a holy prayer that can be invoked once.",
            category = ItemCategory.HolyItem,
            priceCopper = 30_000L, // 300 GP
            rarity = Rarity.Uncommon,
        ),

        // ---- Exotic / Rare (3 items) ----
        CatalogItem(
            name = "Mysterious Map",
            description = "A weathered map marked with cryptic symbols pointing to unknown locations.",
            category = ItemCategory.ExoticItem,
            priceCopper = 50_000L, // 500 GP
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Crystal Ball",
            description = "A flawless sphere of crystal used for scrying distant places and people.",
            category = ItemCategory.ExoticItem,
            priceCopper = 2_500_000L, // 25,000 GP
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Dragon Scale",
            description = "A single iridescent scale shed by an ancient dragon, radiating latent power.",
            category = ItemCategory.ExoticItem,
            priceCopper = 5_000_000L, // 50,000 GP
            rarity = Rarity.Legendary,
        ),

        // ---- Alchemical (3 items) ----
        CatalogItem(
            name = "Alchemist's Fire",
            description = "A volatile flask that bursts into sticky flames on impact.",
            category = ItemCategory.AlchemicalSupply,
            priceCopper = 5_000L, // 50 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Smokestick",
            description = "A treated stick that produces a thick cloud of obscuring smoke when lit.",
            category = ItemCategory.AlchemicalSupply,
            priceCopper = 2_500L, // 25 GP
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Acid Flask",
            description = "A sealed glass vial of corrosive acid that eats through metal and flesh.",
            category = ItemCategory.AlchemicalSupply,
            priceCopper = 2_500L, // 25 GP
            rarity = Rarity.Common,
        ),
    )

    /** Total number of items in the built-in catalog. */
    val size: Int get() = items.size

    /** Returns all catalog items for a given category. */
    fun byCategory(category: ItemCategory): List<CatalogItem> =
        items.filter { it.category == category }

    /** Returns all catalog items for a given rarity. */
    fun byRarity(rarity: Rarity): List<CatalogItem> =
        items.filter { it.rarity == rarity }
}
