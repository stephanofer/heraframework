# 4.0.0.6 (unreleased)

# 4.0.0.5

- **Added** `ZAUCTIONHOUSE_CLAIM` button - displays pending money per economy with dynamic placeholders and allows players to claim directly from the auction GUI
- **Added** Configurable `loading-item` for the claim button, shown while pending money data is being fetched
- **Added** `PRICE_WITHOUT_DECIMAL` price format - displays prices without decimal places (e.g., `10000.50` -> `10000`)
- **Added** `%price-price-without-decimal%` placeholder - displays the price without decimals in item lore
- **Added** `/ah admin forceopen` can now be executed from the console
- **Added** `timezone` configuration option - allows changing the timezone used for all date placeholders (`%date%`, `%formatted-expire-date%`, `%expires_at%`). Supports all Java TimeZone IDs (e.g., `Europe/Paris`, `America/New_York`, `UTC`). Defaults to `auto` (server timezone)
- **Fixed** `/ah admin open` and `/ah admin history` tab completion no longer loads all offline players, preventing lag on servers with many players
- **Fixed** `updateListedItems` crash on Folia/Canvas - inventory holder access was running on an async thread instead of the main tick thread
- **Fixed** economy name argument configuration for the sell command - when the economy argument index was not configured, the default value was ignored causing a null economy name

### `ZAUCTIONHOUSE_CLAIM` button

Allows players to claim their pending money directly from the auction house inventory. The button displays per-economy pending amounts using dynamic placeholders and supports a loading state while data is being fetched.

**Available placeholders:**

| Placeholder | Description |
|-------------|-------------|
| `%pending_total%` | Total pending money across all economies (formatted) |
| `%pending_<economy_name>%` | Pending money for a specific economy (e.g., `%pending_vault%`) |
| `%has_pending%` | `true` or `false` |

The economy name corresponds to the name defined in your `economies.yml` configuration.

**Example configuration:**

```yaml
claim-money:
  type: ZAUCTIONHOUSE_CLAIM
  slot: 48
  loading-item:
    material: CLOCK
    name: "#2CCED2<bold>ᴄʟᴀɪᴍ ᴍᴏɴᴇʏ"
    lore:
      - "#8c8c8c• #ff3535Loading, please wait..."
  item:
    material: GOLD_INGOT
    name: "#2CCED2<bold>ᴄʟᴀɪᴍ ᴍᴏɴᴇʏ"
    lore:
      - ""
      - "#92ffffPending money: #2CCED2%pending_total%"
      - "#92ffffVault: #2CCED2%pending_vault%"
      - "#92ffffTokens: #2CCED2%pending_tokens%"
      - ""
      - "#8c8c8c• #2CCED2Click to claim your money"
```

# 4.0.0.4

- **Added** ZelAuction migration - migrate data from ZelAuction plugin to zAuctionHouse V4 using `/ah admin migrate zelauction confirm`
- **Added** Search system - players can search items by name, material, lore, or seller directly from the auction house GUI or via `/ah search <query>`
- **Added** `ZAUCTIONHOUSE_SEARCH` button - opens a chat-based search input with support for advanced filter operators
- **Added** `ZAUCTIONHOUSE_CLEAR_SEARCH` button - clears the active search filter, only visible when a search is active
- **Added** `/ah search <query>` command - search items directly from chat without opening the GUI first
- **Added** Advanced search filters with operators: `~` (contains), `=` (exact), `~=` (contains, ignore case), `==` (exact, ignore case)
- **Added** Searchable fields: `name`, `material`, `lore`, `seller` (e.g., `seller = Notch`, `name ~ Diamond`)
- **Added** `/ah admin forceopen <player> <inventory> [page]` - Open any inventory for a player at a specific page. Supports all inventory names (e.g., `auction`, `admin-selling-items`, `history`, `admin-logs`, etc.) with tab completion. Page defaults to 1
- **Added** `reset-search-on-open` config option - When enabled (default: `true`), the search filter is cleared every time a player opens the auction house, matching the existing `reset-category-on-open` behavior

### Search system

Players can search for items in the auction house using the search button or the `/ah search` command. The default search checks item name, material, lore, and seller name (case-insensitive substring).

**Advanced filters:**

```
field operator value
```

| Operator | Description |
|----------|-------------|
| `~` | Contains (case-sensitive) |
| `=` | Exact match (case-sensitive) |
| `~=` | Contains (ignore case) |
| `==` | Exact match (ignore case) |

| Field | Description |
|-------|-------------|
| `name` | Item display name |
| `material` | Item material type |
| `lore` | Item lore text |
| `seller` | Seller player name |

**Examples:**
```
seller = Notch
name ~ Diamond
material ~= sword
lore ~ Sharpness
```

# 4.0.0.3

- **Added** `ZAUCTIONHOUSE_SELL_LIMIT` button - displays remaining sell slots visually using a list of inventory slots, configurable per item type (auction, bid, rent)
- **Added** `%zauctionhouse_max_items_<type>%` placeholder - returns the maximum number of items a player can list for a specific type (auction, bid, rent)
- **Optimized** item lore placeholder resolution, placeholders are now pre-detected at config load and only resolved when referenced, significantly reducing CPU and memory usage per item render
- **Fixed** `/ah history` stuck on "Loading..." when `action.purchased-item.give-item: true` is enabled
- **Fixed** default config values
- **Fixed** default table prefix to `zauctionhousev4` (fixes compatibility with zAuctionHouse V3)

### `ZAUCTIONHOUSE_SELL_LIMIT` button

```yaml
sell-limit:
  type: ZAUCTIONHOUSE_SELL_LIMIT
  types:
    - auction
  slots:
    - 0-9
  item:
    material: LIME_STAINED_GLASS_PANE
    name: '&aAvailable slot'
```

### `%zauctionhouse_max_items_<type>%` placeholder

Returns the maximum number of items a player can list based on their permissions for the given type.

- `%zauctionhouse_max_items_auction%`
- `%zauctionhouse_max_items_bid%`
- `%zauctionhouse_max_items_rent%`

# 4.0.0.2

- **Added** `zauctionhouse_category` permissible for zMenu - allows conditional button visibility based on the player's currently selected category (defaults to `main`)
- **Added** `ZAUCTIONHOUSE_CATEGORY_SWITCHER` button - combines category cycling (left/right click) with dynamic lore showing enable/disable state per category
- **Added** `%zauctionhouse_category_id%` placeholder - returns the player's currently selected category ID
- **Added** `ZAUCTIONHOUSE_HISTORY_INVENTORY` button - opens the sales history inventory directly from any UI
- **Added** `force-amount-one` option in `config.yml` (`item-lore` section) - forces displayed item amount to 1 in the auction inventory while preserving the real amount internally
- **Changed** `AuctionEconomy` API - economy methods (`get`, `has`, `deposit`, `withdraw`) now accept `UUID` instead of `OfflinePlayer` for better compatibility
- **Updated** CurrenciesAPI dependency from 1.0.12 to 1.0.13
- **Fixed** category display name fallback - now uses the configured "all" category name instead of a hardcoded `"All"` string
- **Fixed** empty slot crash - list buttons (`ListedItems`, `ExpiredItems`, `PurchasedItems`, `SellingItems`) no longer crash when `emptySlot` is `-1` and the list is empty
- **Fixed** history loading slot - `HistoryItemsButton` now properly handles `loadingSlot` set to `-1` without throwing an error
- **Fixed** `LoadingSlotLoader` - added validation for invalid slot values with a clear error message

### `zauctionhouse_category` permissible

```yaml
requirements:
  - type: zauctionhouse_category
    category: "weapons"
```

### `ZAUCTIONHOUSE_CATEGORY_SWITCHER` button

```yaml
category-switcher:
  type: ZAUCTIONHOUSE_CATEGORY_SWITCHER
  slot: 49
  enable-text: "&a● %category%"
  disable-text: "&7○ %category%"
  categories:
    - "main"
    - "weapons"
    - "armor"
    - "tools"
    - "blocks"
    - "consumables"
    - "resources"
    - "enchanted-books"
    - "misc"
  item:
    material: COMPASS
    name: "&6Categories &7(&f%category%&7)"
    lore:
      - ""
      - "%main%"
      - "%weapons%"
      - "%armor%"
      - "%tools%"
      - "%blocks%"
      - "%consumables%"
      - "%resources%"
      - "%enchanted-books%"
      - "%misc%"
      - ""
      - "&7Left-click &8» &fNext"
      - "&7Right-click &8» &fPrevious"
```

### `force-amount-one` option

```yaml
item-lore:
  # Forces the displayed item amount to 1 in the auction inventory.
  # The real amount is preserved internally and given to the buyer on purchase.
  # Useful to keep a clean, uniform display.
  force-amount-one: false
```

# 4.0.0.1

- **Added** Thai as a supported language
- **Fixed** support for Minecraft **1.20.4**
- **Fixed** the `/zauctionhouse` command - it is no longer the default main command (this can be changed in `config.yml`)
- **Fixed** message system errors that could appear without reason
- **Added** the `reset-category-on-open` option, allowing categories to reset when reopening the inventory
- **Added** `EXCELLENTEECONOMY` economy support