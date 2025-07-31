# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v21.1.1-1.21.1] - 2025-08-01

### Fixed

- Fix crash with [Combat+ Core](https://modrinth.com/mod/combatplus-core) mod

## [v21.1.0-1.21.1] - 2025-01-17

### Changed

- Port to Minecraft 1.21.1
- Controlling which enchantments show up in an infuser is now done via enchantment tags:
    - `enchantinginfuser:in_enchanting_infuser`
    - `enchantinginfuser:in_advanced_enchanting_infuser`
- Modernize enchanting infuser interface
- Rework how enchantments are made available in an infuser based on enchanting power (surrounding bookshelves)
- Rework enchantment cost calculations to fix some edge cases where adding an enchantment level would not necessarily
  cost any experience levels and disenchanting sometimes costing experience, instead of providing some to the player

### Removed

- Remove a bunch of old config options that are no longer necessary / supported for the new implementation