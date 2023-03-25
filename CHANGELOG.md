# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v5.0.0-1.19.3] - 2023-03-25
- Ported to Minecraft 1.19.3
### Added
- Enchanting infuser supports chiseled bookshelves, for every 3 contained books one level of enchanting power is added to the infuser
  - Books are counted from all chiseled bookshelves, there must not be at least 3 books in every single one
  - Chiseled bookshelves must be facing the enchanting infuser to contribute their books
### Changed
- Improved a few tooltip descriptions, especially when the [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) mod is installed
- Enchanting infusers ignore all blocks that do not have a full block collision shape (like carpet) when counting bookshelves, before this would only ignore blocks without a collision shape (like torches)

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
