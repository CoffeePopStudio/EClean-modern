# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.2.4

### Changed
- **Removed the global `RuntimeServices` singleton**: services are now a plain class owned by the plugin instance and accessed through `EClean.services` / `PL.services`.
- All command, cleanup, config, listener, menu, and PAPI code now use the plugin-owned services container.

## 0.2.3

### Changed
- **Unified menu lifecycle**: all open menus are now tracked by `MenuManager` and cleaned up on close, quit, and plugin disable.
- Reused the update-check `HttpClient` instead of creating a new one each check.
- Dense-chunk alerts now parse each message once before sending to all admins.
- Trash-can item storage reads config values outside the write lock.
- Reused a single MiniMessage regex for stripping tags.
- Removed leftover dead legacy tests.

## 0.2.2

### Fixed
- **Chunk cleanup preview now matches real cleanup**: the dry-run count no longer shows the total number of dense entities; it shows how many would actually be removed.
- **Stats/entity/players commands now require the admin permission**, matching the plugin's permission design.
- **Async callbacks are consistent for empty worlds/chunks**: chunk scanning now calls back on the global scheduler just like drop and living cleanup.

### Changed
- Optimized dense-chunk checks by grouping entities by type before applying limits.
- Avoided unnecessary menu refresh scheduling when no trash-can menu is open.
- Menus only rebuild item stacks when the item actually needs updating.
- Reused a single MiniMessage instance instead of creating it repeatedly.

## 0.2.1

### Changed
- **Big internal cleanup and refactor** — no player-facing behavior changed.
- Removed dead code and duplicate classes.
- Split the command handling into smaller files so it is easier to maintain.
- Unified repeated logic across drop/living cleanup, world planning, pager buttons, cleanup announcements, and scheduler gateways.
- Moved hardcoded command messages into `lang.yml`.
- Updated the Gradle wrapper.
- Cleaned up disabled/dead tests.

## 0.2.0

### Added
- **Trash can item stacking**: identical items are merged into one slot automatically. One slot shows one item type with its total count (e.g., Dirt × 2345). No capacity limit — you can store as many as you want.
- **Per-item countdown**: each item type has its own lifetime and disappears on its own when it expires, without affecting other items. The remaining time is shown right in the item description.
- **New stats command**: `/eclean trash stats` (admin only) shows every item type in the trash can with its count and remaining time.
- **New config options**: `trashcan.yml` gains three commented options — stacking on/off, sorting, and showing the remaining time.

### Changed
- The trash can is no longer limited to 54 slots × 64 items each — capacity is unlimited.
- The whole bin is no longer cleared on a fixed timer; each item type now expires on its own.
- The item description now shows the total count (can exceed 64) and the remaining time.

### Removed
- Removed the capacity limit setting (capacity is unlimited now; the old setting in existing config files is ignored automatically).
- Removed the server-wide "bin will be cleared soon" reminder messages.

## 0.1.9

### Fixed
- **Fixed several problems with the automatic language file upgrade**: quoted text, reset marks, and multi-line text could be converted wrongly when switching from old color codes to the new text format — all handled correctly now.
- **Fixed the upgrade continuing after a backup failure**: the upgrade now stops if the old file cannot be backed up, so the original file is never lost.
- **Fixed the plugin failing to start when the language file is damaged**: it now falls back to the built-in default file automatically.
- Removed duplicate code.

## 0.1.8

### Fixed
- **Fixed identical items not merging** in the trash can — each one took its own slot.
- **Fixed items taken from the trash can carrying leftover information** (strange description lines).
- **Fixed item description text showing format tags literally** instead of formatting.
- **Fixed possible item loss** when the server handles multiple regions at the same time.
- **Fixed the countdown staying visible after the feature is disabled**.
- **Fixed memory usage growing** when opening and closing the trash can repeatedly.
- **Fixed the inventory display getting out of sync** when putting items in from the player's backpack.
- **Fixed duplicate reminder messages** with multiple worlds — countdown and cleanup announcements are no longer sent several times.
- **Fixed commands not working on some servers**.
- **Fixed color codes not showing**: old color codes displayed as garbage text on new servers; everything now uses the new text format.
- **Fixed the language file being read incompletely or coming up empty**.

### Changed
- **Rewrote the plugin's base structure** — no longer relies on the old plugin framework.
- **Rewrote the trash can feature**: the menu is now chest-style, and items stay in the bin after closing and reopening the menu.
- **Default cleanup interval changed from 6000 s (100 min) to 600 s (10 min)**.
- **The "bin cleared" notice is now sent to admins only**.
- **Faster config reload**: only the changed parts are reloaded.

### Added
- **Capacity limit setting** for the trash can: configure how many slots it holds; new items are discarded when full (removed in a later version).
- Added automated tests to keep the feature stable.

## 0.1.7

### Changed
- **Rewrote the menu framework** — no longer relies on the old plugin framework.
- **Rewrote the countdown variable display**.
- **Rewrote language file loading**.
- Greatly reduced dependency on the old plugin framework.

## 0.1.6

### Added
- **Automatic text color upgrade**: old color codes are converted to the new text format on first startup.
- **Preview mode**: `/ecl clean --preview` shows what would be cleaned without removing anything.
- **Per-world settings**: `per-world.yml` lets you set a separate cleanup interval for each world.

### Changed
- **Each world counts down separately**.
- Messages now use the new text format.
- Removed old color-handling code.

## 0.1.5

### Changed
- **Rewrote the command system** — no longer relies on the old plugin framework.
- **Rewrote server task scheduling** for compatibility with new server versions.

## 0.1.4

### Added
- Added a one-command way to start a local server for testing.

### Changed
- The debug command, update check, and event handling no longer rely on the old plugin framework.
- Files generated by local testing are no longer tracked in version control.

### Fixed
- **Fixed an error when counting chunks on new-architecture servers**.
- **Fixed an error when loading chunks on new-architecture servers**.
- **Fixed garbled console logs**: logs now go through the plugin logger; Chinese text was replaced with English to avoid garbled output.
- Announcements sent to players are no longer duplicated in the console.

## 0.1.3

### Added
- **Support for new-architecture servers (Folia)**.
- **Rewrote the config system**: default settings split into multiple files, every option commented.
- **New world statistics feature**.
- Added automated tests.

### Changed
- **Renamed the project to EClean-Modern** and upgraded to the new server interface.
- **Major internal rewrite**: cleanup logic split into parts, task scheduling rewritten, statistics collected chunk by chunk to fit the new server architecture.
- Updated documentation.
