package com.shopforge.domain.model

/**
 * The type of a shop, which determines its default item categories.
 */
enum class ShopType {
    Blacksmith {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.Weapon,
            ItemCategory.Armor,
        )
    },
    MagicShop {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.MagicItem,
            ItemCategory.Potion,
        )
    },
    GeneralStore {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.AdventuringGear,
            ItemCategory.Food,
        )
    },
    Alchemist {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.Potion,
            ItemCategory.AlchemicalSupply,
        )
    },
    Fletcher {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.Weapon,
            ItemCategory.Ammunition,
        )
    },
    Tavern {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.Food,
        )
    },
    Temple {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.HolyItem,
            ItemCategory.Potion,
        )
    },
    ExoticGoods {
        override val defaultCategories: List<ItemCategory> = listOf(
            ItemCategory.ExoticItem,
            ItemCategory.MagicItem,
        )
    };

    abstract val defaultCategories: List<ItemCategory>
}
