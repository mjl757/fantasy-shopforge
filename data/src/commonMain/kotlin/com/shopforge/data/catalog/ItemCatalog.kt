package com.shopforge.data.catalog

import com.shopforge.domain.model.Denomination
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Rarity

/**
 * The built-in item catalog for Fantasy ShopForge.
 * Contains 40 original generic fantasy items across all categories.
 */
object ItemCatalog {

    val items: List<CatalogItem> = listOf(
        // ---- Weapons (5 items) ----
        CatalogItem(
            name = "Longsword",
            description = "A versatile blade favored by knights and sellswords alike.",
            category = ItemCategory.Weapon,
            priceAmount = 15, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Shortsword",
            description = "A light, quick blade ideal for close-quarters combat.",
            category = ItemCategory.Weapon,
            priceAmount = 10, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Greataxe",
            description = "A massive two-handed axe that cleaves through armor and bone.",
            category = ItemCategory.Weapon,
            priceAmount = 30, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Dagger",
            description = "A small, concealable blade useful for both combat and utility.",
            category = ItemCategory.Weapon,
            priceAmount = 2, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Warhammer",
            description = "A heavy hammer designed to crush shields and dent plate armor.",
            category = ItemCategory.Weapon,
            priceAmount = 15, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        // ---- Armor (4 items) ----
        CatalogItem(
            name = "Chain Mail",
            description = "Interlocking metal rings that provide solid protection against slashing attacks.",
            category = ItemCategory.Armor,
            priceAmount = 75, priceDenomination = Denomination.Gold,
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Leather Armor",
            description = "Lightweight armor crafted from cured hides, offering mobility with modest protection.",
            category = ItemCategory.Armor,
            priceAmount = 10, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Shield",
            description = "A sturdy wooden shield reinforced with iron bands.",
            category = ItemCategory.Armor,
            priceAmount = 10, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Plate Armor",
            description = "Full suit of interlocking metal plates offering the finest mundane protection available.",
            category = ItemCategory.Armor,
            priceAmount = 1_500, priceDenomination = Denomination.Gold,
            rarity = Rarity.Rare,
        ),

        // ---- Ammunition (2 items) ----
        CatalogItem(
            name = "Arrows (20)",
            description = "A quiver of twenty standard wooden arrows with iron tips.",
            category = ItemCategory.Ammunition,
            priceAmount = 1, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Crossbow Bolts (20)",
            description = "Twenty short, heavy bolts designed for crossbows.",
            category = ItemCategory.Ammunition,
            priceAmount = 1, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),

        // ---- Potions (5 items) ----
        CatalogItem(
            name = "Healing Potion",
            description = "A small vial of glowing red liquid that mends wounds when consumed.",
            category = ItemCategory.Potion,
            priceAmount = 50, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Greater Healing Potion",
            description = "A larger flask of potent crimson elixir capable of closing grievous injuries.",
            category = ItemCategory.Potion,
            priceAmount = 150, priceDenomination = Denomination.Gold,
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Antidote",
            description = "A bitter herbal brew that neutralizes most common poisons.",
            category = ItemCategory.Potion,
            priceAmount = 50, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Invisibility Potion",
            description = "A shimmering, nearly transparent liquid that renders the drinker unseen.",
            category = ItemCategory.Potion,
            priceAmount = 500, priceDenomination = Denomination.Gold,
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Potion of Giant Strength",
            description = "A thick, earthy draught that grants the drinker immense physical power.",
            category = ItemCategory.Potion,
            priceAmount = 1_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.Rare,
        ),

        // ---- Adventuring Gear (6 items) ----
        CatalogItem(
            name = "Rope (50ft)",
            description = "Fifty feet of sturdy hempen rope, essential for climbing and binding.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 1, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Torch",
            description = "A wooden shaft wrapped in oil-soaked cloth that burns for about an hour.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 1, priceDenomination = Denomination.Copper,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Rations (1 day)",
            description = "Dried meat, hard bread, and nuts sufficient for one day of travel.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 5, priceDenomination = Denomination.Silver,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Backpack",
            description = "A sturdy leather pack with multiple compartments for gear.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 2, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Bedroll",
            description = "A warm, rolled sleeping mat for camping in the wilderness.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 1, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Tinderbox",
            description = "A small kit containing flint, firesteel, and tinder for starting fires.",
            category = ItemCategory.AdventuringGear,
            priceAmount = 5, priceDenomination = Denomination.Silver,
            rarity = Rarity.Common,
        ),

        // ---- Magic Items (5 items) ----
        CatalogItem(
            name = "Cloak of Protection",
            description = "An enchanted cloak woven with protective wards that deflect harm.",
            category = ItemCategory.MagicItem,
            priceAmount = 3_500, priceDenomination = Denomination.Gold,
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Ring of Resistance",
            description = "A silver ring imbued with elemental magic, granting resistance to a chosen element.",
            category = ItemCategory.MagicItem,
            priceAmount = 6_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Wand of Sparks",
            description = "A slender wand that crackles with arcane energy, discharging bolts of lightning.",
            category = ItemCategory.MagicItem,
            priceAmount = 750, priceDenomination = Denomination.Gold,
            rarity = Rarity.Rare,
        ),
        CatalogItem(
            name = "Amulet of Health",
            description = "A golden amulet bearing a ruby that fortifies the wearer's vitality.",
            category = ItemCategory.MagicItem,
            priceAmount = 8_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Staff of the Archmage",
            description = "An ancient staff of immense power, sought by the most powerful spellcasters.",
            category = ItemCategory.MagicItem,
            priceAmount = 100_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.Legendary,
        ),

        // ---- Food & Drink (4 items) ----
        CatalogItem(
            name = "Ale (Mug)",
            description = "A frothy mug of hearty ale brewed by the local tavern.",
            category = ItemCategory.Food,
            priceAmount = 4, priceDenomination = Denomination.Copper,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Wine (Bottle)",
            description = "A bottle of red wine from a reputable vineyard.",
            category = ItemCategory.Food,
            priceAmount = 3, priceDenomination = Denomination.Silver,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Bread & Stew",
            description = "A bowl of thick stew served with a chunk of fresh bread.",
            category = ItemCategory.Food,
            priceAmount = 5, priceDenomination = Denomination.Copper,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Fine Meal",
            description = "A lavish multi-course dinner with roast meats, fine cheese, and dessert.",
            category = ItemCategory.Food,
            priceAmount = 1, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),

        // ---- Holy Items (3 items) ----
        CatalogItem(
            name = "Holy Symbol",
            description = "A consecrated emblem of a deity, used as a focus for divine magic.",
            category = ItemCategory.HolyItem,
            priceAmount = 5, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Blessed Water",
            description = "A flask of water sanctified by a priest, harmful to undead and fiends.",
            category = ItemCategory.HolyItem,
            priceAmount = 25, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Divine Scroll",
            description = "A parchment inscribed with a holy prayer that can be invoked once.",
            category = ItemCategory.HolyItem,
            priceAmount = 300, priceDenomination = Denomination.Gold,
            rarity = Rarity.Uncommon,
        ),

        // ---- Exotic / Rare (3 items) ----
        CatalogItem(
            name = "Mysterious Map",
            description = "A weathered map marked with cryptic symbols pointing to unknown locations.",
            category = ItemCategory.ExoticItem,
            priceAmount = 500, priceDenomination = Denomination.Gold,
            rarity = Rarity.Uncommon,
        ),
        CatalogItem(
            name = "Crystal Ball",
            description = "A flawless sphere of crystal used for scrying distant places and people.",
            category = ItemCategory.ExoticItem,
            priceAmount = 25_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.VeryRare,
        ),
        CatalogItem(
            name = "Dragon Scale",
            description = "A single iridescent scale shed by an ancient dragon, radiating latent power.",
            category = ItemCategory.ExoticItem,
            priceAmount = 50_000, priceDenomination = Denomination.Gold,
            rarity = Rarity.Legendary,
        ),

        // ---- Alchemical (3 items) ----
        CatalogItem(
            name = "Alchemist's Fire",
            description = "A volatile flask that bursts into sticky flames on impact.",
            category = ItemCategory.AlchemicalSupply,
            priceAmount = 50, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Smokestick",
            description = "A treated stick that produces a thick cloud of obscuring smoke when lit.",
            category = ItemCategory.AlchemicalSupply,
            priceAmount = 25, priceDenomination = Denomination.Gold,
            rarity = Rarity.Common,
        ),
        CatalogItem(
            name = "Acid Flask",
            description = "A sealed glass vial of corrosive acid that eats through metal and flesh.",
            category = ItemCategory.AlchemicalSupply,
            priceAmount = 25, priceDenomination = Denomination.Gold,
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
