# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.8]

### Fixed
- **Trashcan storage now uses `isSimilar` linear scan** instead of `HashMap<ItemSign, TrashInfo>` with custom `hashCode`. `ItemSign.hashCode` cannot faithfully replicate Paper `ItemStack.isSimilar`'s internal comparison across all `DataComponent`s, causing identical items to scatter into different hash buckets and never stack. Switched to a `CopyOnWriteArrayList<TrashInfo>` with `isSimilar` linear lookup on `upsert` — O(n) is negligible at the expected cap of a few hundred unique entries, and correctly matches Paper's component-aware equality.
- **Repository now clones `ItemStack` on `upsert` to isolate external references**: `DropCleanupExecutor` passes live entity `ItemStack` references; after `item.remove()`, Paper may reclaim the underlying NBT, corrupting the stored `origin` and polluting retrieved items with stray lore/NBT. The clone in `upsert` ensures data isolation.
- **`TrashInfo` lore now deserializes via `MiniMessage.deserialize()` to native `Component`**: the deprecated `List<String>` lore API treated MiniMessage tags as legacy `§` codes, rendering `<white>共64个</white>` literally. Switched to the modern `lore(List<Component>)` API so lore displays with proper formatting.
- **Removed defunct `Trashcan.cleanTrash()` logic referencing removed `trashData` field**.
- **Added `@Synchronized` to all Repository write methods** (`upsert`, `removeByItem`, `clear`) to guard against concurrent Folia multi-region access that would cause `ArrayIndexOutOfBoundsException` or silent data loss.
- **`TrashcanTicker` now resets countdown to 0 when disabled**: previously returned without clearing, causing PAPI `%eclean_trashcan_countdown%` to leak stale values.
- **`TrashcanMenu` now calls `unregister()` on close**: each `/ecl trash` opened a new `TrashcanMenu` registered as a Bukkit `Listener`, but closing only removed it from the `opened` set — leaking listeners indefinitely.
- **Trashcan player inventory click isolation**: replaced `event.currentItem` manipulation in `onClickSelfInv` with direct `player.inventory.setItem(rawSlot-54, …)` — Bukkit reverts cancelled `InventoryClickEvent.currentItem` changes, causing items to appear in inventory while being registered in trashcan (lost on GUI close/reopen). `TrashInfo.generateItem` now clones `origin` and explicitly gets/sets `ItemMeta` through the Bukkit API boundary instead of `editItemMeta`, preventing `ItemMeta` sharing between display item and stored origin on Paper 1.21+.
- **Trashcan player inventory interaction restored**: routed player inventory clicks through `UiMenu.onPlayerInvClick` to restore deposit/stack/lore behavior after the paginated-view removal.
- **Merged per-world cleanup tickers into a single global ticker**: each world previously had its own ticking task calling `announceCountdown()` every second and `cleanNow()` on expiry — with 3 worlds this meant countdown messages and finish announcements were broadcast 3×. Now uses a single global ticker keyed to the minimum interval across all enabled per-world configs.
- **Merged `check.kt` and `Players.kt` into `Commands.kt`**: eliminated multi-file class loading failures on certain server platforms where separate command handler files fail to resolve across classloader boundaries. Also replaced `EntityType.entries` (Kotlin 2.0+ only) with `.values()` for JDK compatibility.
- **Replaced all legacy `&` color codes with MiniMessage tags** in Commands, Players, check command handlers; fixed `sendUsage` to use `MessageService.send` instead of raw `sender.sendMessage`.
- **Fixed `MLang.flatten()` to use `ConfigurationSection` instead of `YamlConfiguration`** for correct nested key traversal. Previously `readIntoCache` converted the YAML string to a `YamlConfiguration` and then re-wrapped sections, losing intermediate nesting.
- **`MLang.load()` now always populates cache**: previously when `LegacyLangMigrator` did not trigger (no legacy codes found), `readIntoCache` was never called, leaving the cache empty and all `MLang[key]` lookups returning raw keys.

### Changed
- Replaced `EPlugin` base class with direct `JavaPlugin` extension — all EPlugin features (debugPrefix, prefix, debug, debuggers, bstats) self-implemented in EClean.
- Deleted `MLangHost` — now that EPlugin's `langManager` type constraint is gone, the last eplugin import in lang is eliminated.
- Optimized config reload to use section-level diff — only restarts affected services (cleanup ticker on `cleanup`/`perWorld` change, trashcan ticker on `trashcan` change) instead of full pipeline rebuild.
- **Trashcan subsystem rewritten to 4-layer architecture**: `TrashcanItemStore` (thread-safe data with `ReentrantReadWriteLock`) → `TrashcanManager` (business façade) → `TrashcanTicker` (owns countdown) → `TrashcanMenu` (GUI with lore-injected `UiButton`s). Deleted `TrashcanService.kt` and `TrashcanRepository.kt`.
- **TrashcanMenu replaced paginated view with vanilla chest-like inventory**: 54-slot grid with direct load/save via `TrashcanItemStore`. Items persist across open/close cycles. Custom `TrashcanItemButton` injects operation lore (count, left=1, right=half-stack, shift+left=full stack) and intercepts clicks to give exact amounts instead of vanilla shift-click/right-click behavior. Player inventory drag-and-drop into the trashcan is now fully supported.
- **Trashcan default clear interval changed from 6000s (100min) to 600s (10min)**.
- **`clearAll()` notification changed from global broadcast to admin-only** (players with `eclean.admin` permission).
- **Trashcan UI title updated to `"共享垃圾桶 - 先到先得, 定期清空"`** to reflect the shared, first-come-first-served nature.
- **`UiMenu.buttons` visibility changed from `private` to `protected`** to allow subclasses like `TrashcanMenu` to access button state during click handling.

### Added
- Integration tests for DropCleanupService, LivingCleanupService, and ChunkDensityScanner using MockBukkit (end-to-end pipeline validation).
- **`TrashcanItemStore` unit tests** (8 tests) covering: same-type merging, maxStackSize overflow, capacity rejection, removeItem subtraction/exhaustion, clear, loadInto/saveFrom round-trip, air-slot filtering.
- **`maxSlots` config key** (`trashcan.yml`, default 54) — configurable capacity limit for the trashcan. When full, new items are discarded with a debug log.
- **`command.trash_full` lang key** for capacity-exceeded notification.

#### Zero eplugin dependencies
The plugin now has **zero** `import top.e404.eplugin` statements in `src/main/kotlin/`.

## [0.1.7]

### Changed
- Extracted standalone `ui/` menu framework (UiMenu, UiButton, UiPager, UiDisplayable, util) to replace all `eplugin.menu` dependencies.
- Migrated DenseMenu and TrashcanMenu from ChestMenu/MenuButton/MenuButtonZone to UiMenu/UiButton/UiPager.
- Replaced `EMenuManager`-based MenuManager with standalone Bukkit `Listener` registration — `List enable/disable` no longer depends on eplugin.
- Replaced eplugin PlaceholderAPI hook (EHookManager, PlaceholderAPIHook, PapiExpansion) with native `me.clip.placeholderapi.expansion.PlaceholderExpansion`.
- Decoupled `MLang` from `ELangManager` — now uses independent YAML loading with thin `MLangHost` shim for EPlugin compatibility only.
- Reduced eplugin imports from 31 to 3 (`EPlugin` in EClean/RuntimeServices, `ELangManager` in MLangHost).

## [0.1.6]

### Added
- Added Adventure MiniMessage messaging with automatic migration from legacy `&c` color codes — `LegacyLangMigrator` detects `&` codes on first load, renames to `lang.old.yml`, converts to MiniMessage, and deletes the backup.
- Added `/ecl clean --preview` dry-run mode — reports what would be cleaned without removing any entities.
- Added per-world cleanup configuration via `per-world.yml` — each world can override `intervalSeconds` or set `enabled: false`.

### Changed
- Replaced `ELangManager`-based `Lang.kt` with standalone `MLang` singleton (zero eplugin dependency in message loading).
- `MessageService.send()` and `.broadcast()` now output Adventure `Component` via `MiniMessage.deserialize()`.
- `CleanupTickService` expanded from a single global ticker to a per-world `Map<String, ScheduledTask>`.
- Config message defaults (countdown, finish) converted to MiniMessage format.
- Removed `String.color` and `String.removeColor()` extensions — MiniMessage eliminates `§` code usage.

## [0.1.5]

### Changed
- Replaced the `ECommand`/`ECommandManager` framework with a single native Bukkit `CommandExecutor` + `TabCompleter` in `Commands.kt`, inlining all 8 subcommands (debug, reload, clean, stats, entity, trash, players, show) as private handler methods.
- Removed `SchedulerFacade`/`FoliaSchedulerFacade`/`PaperSchedulerFacade` abstractions and replaced with `Schedulers` singleton — a direct wrapper around Bukkit's Folia-compatible scheduler APIs (`GlobalRegionScheduler`, `RegionScheduler`, `AsyncScheduler`, `EntityScheduler`), which Paper 1.21+ polyfills internally.
- `SchedulerHandle` replaced with `io.papermc.paper.threadedregions.scheduler.ScheduledTask` throughout.
- `RuntimeServices` no longer creates scheduler instances; `isSchedulerReady` guard removed.

## [0.1.4]

### Added
- Added `xyz.jpenilla.run-paper` Gradle plugin with `runFolia` and `runServer` tasks for one-command local integration testing.

### Changed
- Migrated `parseSecondAsDuration` from eplugin into a standalone `Long` extension in `util/Text`.
- Removed `EListener` dependency: `DespawnListener` and `Trashcan` now implement `Listener` directly, registered via `Bukkit.getPluginManager().registerEvents` in `EClean.onEnable`.
- Removed `AbstractDebugCommand` dependency: `Debug` is now a regular `ECommand` with inline debugger management.
- Removed `EUpdater` dependency: `Update` now uses `java.net.http.HttpClient` + `JsonParser` for GitHub releases checks, scheduled via `AsyncScheduler`.
- Excluded `run/` from version control (artifacts generated by `gradlew runFolia` / `runServer`).

### Fixed
- Fixed `IllegalStateException` reading `chunk.isForceLoaded` on Folia region threads — force-loaded count is now collected on the global tick thread.
- Fixed `Thread failed main thread check: Async chunk retrieval` in Folia 26.1 (`Moonrise`) — live `Chunk` references are now captured during region-thread dispatch instead of calling `getChunkAt` on the global thread.
- Routed all console output (`info`, `debug`, `buildDebug`, `warn`) through `plugin.logger` so messages appear in `logs/latest.log` and respect server logging levels (replaced `Bukkit.getConsoleSender().sendMessage()`).
- Replaced all hardcoded Chinese strings in `debug`/`info`/`warn` with English to prevent garbled log output on terminals without UTF-8 support.
- `broadcast()` no longer logs player-facing announcements to the console.

## [0.1.3]

### Added
- Added Folia support declaration in `plugin.yml`.
- Added a modern multi-file configuration system based on `Kaml` and `kotlinx.serialization`.
- Added dedicated configuration models, snapshot loading, runtime apply hooks, and config reload flow.
- Added initial `RuntimePlatform` and `ExecutionGateway` abstractions for the Folia-first runtime boundary.
- Added chunk-density snapshot and policy tests to lock the new workflow behavior.
- Added drop and living cleanup policy tests to lock the protected-entity and matcher behavior after the cleanup split.
- Added focused `TemporaryReturnService` tests covering delayed return, replacement, and quit handling.
- Added `WorldStatsService`, `WorldStatsCollector`, and `WorldStatsResult` for Folia-safe chunk-by-chunk distributed stats collection and aggregation via `ChunkTaskCoordinator`.
- Added `util/Text` (standalone `color`, `formatAsConst`, `placeholder` extensions) and `app/MessageService` (message, broadcast, debug, log facade) to decouple the business code from `EPlugin`'s companion object and instance messaging API.

### Changed
- Renamed the project and plugin identity from `EClean` to `EClean-Modern` across all Gradle, `plugin.yml`, and documentation surfaces.
- Updated the plugin to Paper API `26.1.2`.
- Split default configuration templates into multiple files under `src/main/resources/config/`.
- Updated tests and Gradle setup for the modernized configuration workflow.
- Updated MockBukkit and related test dependencies to match the current Paper API line.
- Restored the test suite so `gradlew test` passes again.
- Updated the Shadow plugin and build lifecycle so Java 25 `shadowJar` packaging succeeds again.
- Updated `README`, the Folia adaptation spec, and the implementation plan to document the Folia-first runtime positioning and the current implementation status.

### Refactored
- Replaced the legacy `eplugin config`-based configuration layer with the new internal config system.
- Migrated runtime config access in cleanup, trashcan, commands, listeners, and update checks to the new config facade.
- Refactored runtime bootstrap to inject platform-aware execution services into `RuntimeServices`.
- Consolidated cleanup/trashcan runtime lifecycle into `RuntimeServices`, so plugin enable, reload, and disable now reuse the same start/reload/shutdown entrypoints.
- Simplified `CleanupCoordinator` injection to the dependencies it actually owns, and moved config runtime apply flow away from direct legacy `Clean.schedule()` calls.
- Switched `FoliaSchedulerFacade` from reflective scheduler lookup to direct Paper/Folia scheduler APIs, while keeping plugin task cancellation centralized behind `SchedulerFacade`.
- Refactored chunk-density cleanup into planner, snapshotter, policy, cleaner, and report components while keeping the existing entrypoints intact.
- Refactored drop and living cleanup into planner, collector, policy, executor, and report components while keeping the existing entrypoints intact.
- Extracted `PlayerTeleportService` and `TemporaryReturnService`, and routed `DenseZone` and `MenuManager` through the new services while preserving existing player-facing menu behavior.
- Moved `ChunkTaskCoordinator` from a cleanup-only package to `platform/dispatch/` for shared use by cleanup and stats features.
- Changed `Planner` layer (`*Planner.planWorlds`) to return world `String` names or `ChunkRef` lists instead of live `World` / `Chunk` objects, preventing downstream cross-region holding.
- Added `collectFromChunk(chunk)` to `Collector` layer, shifting entity/item collection from world-wide traversal to single-chunk collection.
- Changed `DropCleanupService`, `LivingCleanupService`, and `ChunkDensityScanner` to accept `SchedulerFacade` and dispatch region-safe per-chunk cleanup via `ChunkTaskCoordinator`.
- Rewrote `clean/*.kt` entrypoints: removed duplicate `Bukkit.getWorlds()` logic, delegated to Service layer; kept simple overloads externally but made internal flows async callback-based for Folia chunk-by-chunk model.
- Made `CleanupCoordinator.cleanNow()` a chained async callback, ensuring drop → living → chunk sequential completion.
- Wrapped chunk entity iteration and removal inside `DenseZone` with `regionScheduler`, and relocated `getHighestBlockYAt` into the target chunk region.
- Consolidated `world.entities` / `world.loadedChunks` stats collection and result sending in `command/check.kt` to `globalRegionScheduler`.
- Changed `command/Players.kt` location reads to per-player entity-scheduler dispatch with async aggregation.
- Changed `command/Clean.kt` per-world cleanup to create transient Service instances and report results via callbacks.
- Changed `command/Show.kt` `scanDenseEntries` to an async callback version accepting `SchedulerFacade`.
- Added `isSchedulerReady` property to `RuntimeServices` for safe `SchedulerFacade` readiness checks at cleanup entrypoints.

