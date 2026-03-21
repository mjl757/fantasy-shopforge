# PRD: Fantasy ShopForge — TTRPG Shop Generator & Inventory Manager

## 1. Overview

### Problem
Game Masters (GMs) running tabletop RPGs (D&D, Pathfinder, etc.) need a quick way to create, populate, and reference item shops during gameplay. Manually building shop inventories is tedious and breaks the flow of a session. Existing tools are either web-only, too complex, or lack generation capabilities.

### Solution
**Fantasy ShopForge** — an Android application that lets GMs **create or auto-generate** named shops with typed inventories. Shops are categorized by type (e.g., blacksmith, magic shop, general store), and their inventories are populated accordingly. GMs can reference shops on their phone/tablet during sessions for quick price lookups and stock checks.

### Target Users
Game Masters who run TTRPGs such as Dungeons & Dragons 5e, Pathfinder 2e, and similar systems.

---

## 2. Core Features

### 2.1 Shop Management
| Capability | Description |
|---|---|
| **Create Shop** | Manually create a shop with a name, type, and optional description |
| **Generate Shop** | Auto-generate a shop — name, type, and inventory populated automatically |
| **Generate Shop Name** | Generate a thematic name for a shop based on its type (e.g., "The Gilded Anvil" for a blacksmith) |
| **Edit Shop** | Rename, change type, update description |
| **Add Items to Shop** | Add items from catalog or custom items to any shop's inventory |
| **Regenerate Inventory** | Regenerate a shop's inventory based on its type (with confirmation) |
| **Delete Shop** | Remove a shop and its inventory |
| **List/Browse Shops** | View all saved shops, filter/sort by type or name |

### 2.2 Shop Types
Shops have a **type** that determines the default categories of items in their inventory. Initial types:

| Shop Type | Typical Inventory |
|---|---|
| **Blacksmith / Armorer** | Weapons, armor, shields |
| **Magic Shop / Arcane Emporium** | Magic items, wands, scrolls, spell components |
| **General Store** | Rations, rope, torches, adventuring gear, tools |
| **Alchemist / Apothecary** | Potions, poisons, herbs, alchemical supplies |
| **Fletcher / Bowyer** | Bows, crossbows, ammunition |
| **Tavern / Inn** | Food, drink, lodging |
| **Temple / Shrine** | Holy symbols, healing potions, divine scrolls |
| **Exotic Goods / Curiosities** | Rare items, trinkets, unusual wares |
| **Custom** | GM defines what item types the shop carries |

GMs can assign any type to a shop, or create custom types.

### 2.3 Item Management
Each item has the following attributes:

| Attribute | Required | Description |
|---|---|---|
| **Name** | Yes | Item name (e.g., "Longsword", "Potion of Healing") |
| **Description** | No | Flavor text or mechanical description |
| **Type / Category** | Yes | E.g., Weapon, Armor, Potion, Adventuring Gear, Magic Item |
| **Price** | Yes | Cost using D&D standard currency: copper (CP), silver (SP), gold (GP), platinum (PP). Stored as CP internally, displayed in most readable denomination |
| **Rarity** | No | Common, Uncommon, Rare, Very Rare, Legendary (primarily for magic items) |

When an item is **in a shop's inventory**, it also has:

| Attribute | Description |
|---|---|
| **Quantity** | How many are in stock (nullable = unlimited) |

### 2.4 Inventory Generation
- When generating a shop, the app populates inventory based on shop type
- Generation uses a built-in item catalog seeded with standard TTRPG items
- **Post-generation editing**: GMs can add items from the catalog (or custom items) to an already-generated shop, remove individual items, and adjust quantities
- **Regenerate inventory**: GMs can regenerate a shop's entire inventory at any time (with a confirmation prompt to avoid accidental loss)
- Generated prices can have slight variance to feel more realistic
- Rarity distribution should skew toward common items, with rare/legendary items appearing infrequently

### 2.5 Item Catalog
- The app ships with a **built-in catalog** of original generic fantasy items (weapons, armor, gear, potions, magic items) — no licensing concerns
- **Optional SRD/OGL item packs** can be enabled for D&D 5e SRD or Pathfinder OGL items (familiar names and stats)
- GMs can create **custom items** that are saved to their personal catalog
- Custom items are available for future shop generation and manual addition

### 2.6 Session / Reference Mode
- Quick-access view of a shop's inventory during gameplay
- Search/filter items within a shop
- Decrement quantity when a player purchases an item
- Mark items as sold out

---

## 3. User Flows

### 3.1 Create a Shop Manually
1. Tap "New Shop"
2. Enter name (or tap "Generate Name" for a suggestion)
3. Select shop type
4. Optionally add description
5. Shop is created with empty inventory
6. Add items manually from catalog or create new items

### 3.2 Generate a Shop
1. Tap "Generate Shop"
2. Optionally select shop type (or let it be random)
3. App generates name, populates inventory based on type
4. GM reviews and adjusts as needed

### 3.3 Reference During a Session
1. Open app, see list of shops
2. Tap a shop to see its inventory
3. Player asks "how much is a longsword?" — GM searches/scrolls to find it
4. Player buys it — GM taps to decrement quantity

---

## 4. Technical Architecture

### 4.1 High-Level Architecture
The app follows **Clean Architecture** with a KMP-first approach for future iOS/Web reuse.

**Repository strategy**: Monorepo — all modules live in a single repository. This avoids versioning/publishing overhead while there's a single platform (Android). The KMP modules (`:domain`, `:data`) are already isolated Gradle modules, making a future split straightforward if iOS/Web repos are added later.

```
┌─────────────────────────────────────┐
│  :app (Android)                     │
│  - Single Activity                  │
│  - Jetpack Compose UI               │
│  - Jetpack Navigation (Compose)     │
│  - ViewModels                       │
│  - Android-specific DI (Metro)      │
├─────────────────────────────────────┤
│  :domain (KMP)                      │
│  - Use Cases                        │
│  - Domain Models (Shop, Item, etc.) │
│  - Repository Interfaces            │
│  - No platform dependencies         │
├─────────────────────────────────────┤
│  :data (KMP)                        │
│  - Repository Implementations       │
│  - Local Database (SQLDelight)      │
│  - Built-in Item Catalog            │
│  - Data Models / Mappers            │
└─────────────────────────────────────┘
```

### 4.2 Module Breakdown

| Module | Type | Responsibilities |
|---|---|---|
| `:app` | Android | UI (Compose), Navigation, ViewModels, Android DI wiring |
| `:domain` | KMP (Common) | Use cases, domain entities, repository interfaces, business logic for generation |
| `:data` | KMP (Common) | Repository implementations, database, catalog data, data mappers |

### 4.3 Technology Stack

| Concern | Technology | Notes |
|---|---|---|
| **Language** | Kotlin | KMP for shared modules |
| **UI** | Jetpack Compose | Material 3 |
| **Navigation** | Jetpack Navigation (Compose) | Single-activity, type-safe navigation |
| **DI** | Metro | Compile-time DI, full KMP support. Maintained by Zac Sweers, production-proven at Cash App. Kotlin compiler plugin (no KAPT/KSP). |
| **Database** | SQLDelight | KMP-compatible, generates type-safe Kotlin from SQL |
| **Async** | Kotlin Coroutines + Flow | Reactive data streams |
| **Build** | Gradle (KTS) | Convention plugins for shared config |
| **Min SDK** | 26 (Android 8.0) | Covers ~95%+ of active devices |

### 4.4 Metro DI Notes
Metro is a compile-time DI framework for Kotlin Multiplatform by Zac Sweers. Key considerations:
- **KMP support**: Full support for JVM/Android, JS, WASM, and Native targets
- **Compose integration**: Has a `metrox-viewmodel-compose` module for ViewModel injection
- **API style**: Familiar to Dagger users but with Kotlin-first ergonomics
- **Trade-off**: Smaller community than Koin/Kodein, but compile-time safety is valuable as the app grows
- **Fallback plan**: If Metro proves problematic, Koin is a well-established KMP alternative with minimal migration cost

### 4.5 Data Layer Details
- **SQLDelight** for structured storage (shops, items, inventory join table)
- Built-in item catalog shipped as pre-populated database or bundled JSON
- Schema:
  - `Shop(id, name, type, description, createdAt, updatedAt)`
  - `Item(id, name, description, type, price, rarity, isCustom)`
  - `ShopInventory(shopId, itemId, quantity)`

---

## 5. Non-Functional Requirements

| Requirement | Target |
|---|---|
| **Offline-first** | App works fully offline; no network required |
| **Performance** | Shop list and inventory load in < 200ms |
| **Data persistence** | All shops/items survive app restarts |
| **Accessibility** | Follow Material 3 accessibility guidelines |
| **Theme** | Support dark mode and light mode |

---

## 6. Future Considerations (Out of Scope for V1)

- **iOS / Web apps** — KMP modules (:domain, :data) are designed for reuse
- **Game system support** — System-specific item packs (D&D 5e SRD, PF2e, etc.)
- **Import/Export** — Share shops as JSON or between devices
- **Cloud sync** — Sync shops across devices
- **Campaign organization** — Group shops by campaign or world
- **NPC shopkeepers** — Generate shopkeeper names, personalities, quirks
- **Haggling / economy simulation** — Dynamic pricing based on supply/demand
- **AI-powered generation** — Use LLMs for richer item descriptions and shop flavor

---

## 7. Success Metrics

| Metric | Target |
|---|---|
| Shop generation time | < 2 seconds from tap to populated shop |
| Items in built-in catalog | 150+ items across all categories at launch |
| App startup to usable | < 1.5 seconds |

---

## 8. Decisions Log

| Decision | Choice | Rationale |
|---|---|---|
| **Currency system** | D&D standard (GP, SP, CP, PP) — no Electrum | Covers D&D and Pathfinder; EP excluded as it's rarely used |
| **Item catalog** | Original items + optional SRD/OGL packs | Ship with original generic fantasy items by default; offer SRD/OGL item packs as optional add-ons |
| **Generation complexity** | Random spread by shop type | Keep V1 simple — GMs curate after generation. Level-aware generation is a future enhancement |
| **Repository strategy** | Monorepo | Single repo for all modules. KMP modules are isolated Gradle modules, easy to extract later if needed |
