package com.shopforge.data.repository

import com.shopforge.domain.model.Item
import com.shopforge.domain.model.ItemCategory
import com.shopforge.domain.model.Price
import com.shopforge.domain.model.Rarity

/**
 * Built-in catalog of generic fantasy items shipped with the app.
 * These are original items (no SRD/OGL content) covering all shop types.
 *
 * Prices are stored in copper pieces (CP). Conversion: 1 GP = 100 CP, 1 SP = 10 CP.
 */
internal object CatalogItems {

    val all: List<Item> = listOf(
        // ---- Weapons ----
        Item(id = 0, name = "Longsword", description = "A versatile steel blade favored by knights", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Shortsword", description = "A light, quick blade for close combat", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Dagger", description = "A simple but reliable stabbing weapon", category = ItemCategory.Weapon, price = Price.ofGold(2), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Battleaxe", description = "A heavy axe built for war", category = ItemCategory.Weapon, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Warhammer", description = "A crushing weapon of forged iron", category = ItemCategory.Weapon, price = Price.ofGold(15), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Longbow", description = "A tall bow of yew for ranged combat", category = ItemCategory.Weapon, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),

        // ---- Armor ----
        Item(id = 0, name = "Chain Mail", description = "Interlocking metal rings offering solid protection", category = ItemCategory.Armor, price = Price.ofGold(75), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Leather Armor", description = "Cured hide shaped into a protective vest", category = ItemCategory.Armor, price = Price.ofGold(10), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Plate Armor", description = "Full suit of forged steel plates", category = ItemCategory.Armor, price = Price.ofGold(1500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 0, name = "Wooden Shield", description = "A sturdy round shield of oak", category = ItemCategory.Armor, price = Price.ofGold(5), rarity = Rarity.Common, isCustom = false),

        // ---- Potions ----
        Item(id = 0, name = "Potion of Healing", description = "A crimson liquid that mends wounds", category = ItemCategory.Potion, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Potion of Greater Healing", description = "A potent restorative draught", category = ItemCategory.Potion, price = Price.ofGold(150), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 0, name = "Potion of Fire Resistance", description = "Grants temporary resistance to flames", category = ItemCategory.Potion, price = Price.ofGold(300), rarity = Rarity.Rare, isCustom = false),
        Item(id = 0, name = "Antitoxin Vial", description = "Neutralizes most common poisons", category = ItemCategory.Potion, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),

        // ---- Adventuring Gear ----
        Item(id = 0, name = "Hempen Rope (50 ft)", description = "Strong rope for climbing and binding", category = ItemCategory.AdventuringGear, price = Price.ofGold(1), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Torch", description = "A wooden stick wrapped in oil-soaked cloth", category = ItemCategory.AdventuringGear, price = Price.ofCopper(1), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Backpack", description = "A sturdy leather pack for carrying supplies", category = ItemCategory.AdventuringGear, price = Price.ofGold(2), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Bedroll", description = "A padded roll for camping in the wild", category = ItemCategory.AdventuringGear, price = Price.ofGold(1), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Tinderbox", description = "Flint, steel, and tinder for starting fires", category = ItemCategory.AdventuringGear, price = Price.ofSilver(5), rarity = Rarity.Common, isCustom = false),

        // ---- Magic Items ----
        Item(id = 0, name = "Wand of Sparks", description = "A wand that produces small bolts of lightning", category = ItemCategory.MagicItem, price = Price.ofGold(500), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 0, name = "Cloak of Shadows", description = "A dark cloak that bends light around the wearer", category = ItemCategory.MagicItem, price = Price.ofGold(5000), rarity = Rarity.Rare, isCustom = false),
        Item(id = 0, name = "Ring of Protection", description = "A silver ring that wards against harm", category = ItemCategory.MagicItem, price = Price.ofGold(3500), rarity = Rarity.Rare, isCustom = false),
        Item(id = 0, name = "Amulet of Vitality", description = "A golden amulet that bolsters the wearer's endurance", category = ItemCategory.MagicItem, price = Price.ofGold(15000), rarity = Rarity.VeryRare, isCustom = false),

        // ---- Food ----
        Item(id = 0, name = "Rations (1 day)", description = "Dried meat, hardtack, and dried fruit", category = ItemCategory.Food, price = Price.ofSilver(5), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Ale (Mug)", description = "A frothy mug of hearty ale", category = ItemCategory.Food, price = Price.ofCopper(4), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Fine Wine (Bottle)", description = "An aged vintage from distant vineyards", category = ItemCategory.Food, price = Price.ofGold(10), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 0, name = "Traveler's Stew", description = "A warm bowl of hearty stew", category = ItemCategory.Food, price = Price.ofSilver(3), rarity = Rarity.Common, isCustom = false),

        // ---- Holy Items ----
        Item(id = 0, name = "Holy Symbol", description = "A sacred emblem of divine faith", category = ItemCategory.HolyItem, price = Price.ofGold(5), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Scroll of Blessing", description = "A parchment inscribed with a divine prayer", category = ItemCategory.HolyItem, price = Price.ofGold(100), rarity = Rarity.Uncommon, isCustom = false),
        Item(id = 0, name = "Vial of Holy Water", description = "Water blessed by a high priest", category = ItemCategory.HolyItem, price = Price.ofGold(25), rarity = Rarity.Common, isCustom = false),

        // ---- Exotic Items ----
        Item(id = 0, name = "Crystal Ball", description = "A smooth orb of polished quartz for scrying", category = ItemCategory.ExoticItem, price = Price.ofGold(10000), rarity = Rarity.VeryRare, isCustom = false),
        Item(id = 0, name = "Dragon Scale", description = "A single iridescent scale from a true dragon", category = ItemCategory.ExoticItem, price = Price.ofGold(25000), rarity = Rarity.Legendary, isCustom = false),
        Item(id = 0, name = "Mysterious Trinket", description = "A curious object that hums faintly with magic", category = ItemCategory.ExoticItem, price = Price.ofGold(250), rarity = Rarity.Uncommon, isCustom = false),

        // ---- Ammunition ----
        Item(id = 0, name = "Arrows (20)", description = "A bundle of wooden-shafted arrows", category = ItemCategory.Ammunition, price = Price.ofGold(1), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Crossbow Bolts (20)", description = "Short iron-tipped bolts for crossbows", category = ItemCategory.Ammunition, price = Price.ofGold(1), rarity = Rarity.Common, isCustom = false),

        // ---- Alchemical Supplies ----
        Item(id = 0, name = "Alchemist's Fire", description = "A volatile flask that bursts into flame on impact", category = ItemCategory.AlchemicalSupply, price = Price.ofGold(50), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Smokestick", description = "A stick that emits thick concealing smoke", category = ItemCategory.AlchemicalSupply, price = Price.ofGold(25), rarity = Rarity.Common, isCustom = false),
        Item(id = 0, name = "Thunderstone", description = "A small stone that creates a deafening bang", category = ItemCategory.AlchemicalSupply, price = Price.ofGold(30), rarity = Rarity.Uncommon, isCustom = false),
    )
}
