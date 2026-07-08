# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.3] - Unreleased

### Added
- Added Folia support declaration in `plugin.yml`.
- Added a modern multi-file configuration system based on `Kaml` and `kotlinx.serialization`.
- Added dedicated configuration models, snapshot loading, runtime apply hooks, and config reload flow.

### Changed
- Updated the plugin to Paper API `26.1.2`.
- Split default configuration templates into multiple files under `src/main/resources/config/`.
- Updated tests and Gradle setup for the modernized configuration workflow.
- Updated MockBukkit and related test dependencies to match the current Paper API line.
- Restored the test suite so `gradlew test` passes again.

### Refactored
- Replaced the legacy `eplugin config`-based configuration layer with the new internal config system.
- Migrated runtime config access in cleanup, trashcan, commands, listeners, and update checks to the new config facade.

### Notes
- This version is not released yet.
