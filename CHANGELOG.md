# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.2.3-1.19.2] - 2023-03-15
### Added
- Added `ja_jp` thanks to [Kalavika](https://github.com/Kalavika)
- Added `es_es` thanks to [Broxy](https://github.com/BroxyZF)
### Changed
- Updated `zh_cn` thanks to [Andows96](https://github.com/Andows96)

## [v4.2.2-1.19.2] - 2022-10-22
### Changed
- Re-enabled Apotheosis integration

## [v4.2.1-1.19.2] - 2022-08-27
### Fixed
- Fixed no enchantment levels being taken when repairing items in an advanced infuser

## [v4.2.0-1.19.2] - 2022-08-21
- Compiled for Minecraft 1.19.2
- Updated to Puzzles Lib v4.2.0

## [v4.1.1-1.19.1] - 2022-07-30
### Fixed
- Fixed enchanting power not increasing above 15 bookshelves when a higher value is set in the config

## [v4.1.0-1.19.1] - 2022-07-30
- Compiled for Minecraft 1.19.1
- Updated to Puzzles Lib v4.1.0

## [v4.0.3-1.19] - 2022-07-24
### Added
- Added support for bookshelves from Apotheosis, they will now provide enchanting power (Eterna) to the infuser
- They will also allow the infuser to reach higher enchanting power values, depending on the type of bookshelf that is used
- Added `ko_kr` translation by [Gyular], thanks!
### Fixed
- Fixed log spam due to server config not being loaded during start-up

## [v4.0.2-1.19] - 2022-07-24
### Changed
- Made repair button more descriptive

## [v4.0.1-1.19] - 2022-07-19
### Added
- Added a config option to control if anvil exclusive enchantments (e.g. sharpness on axes) are allowed to be applied in the infuser
- This is enabled by default for the advanced infuser
### Fixed
- Fixed the infuser always allowing to apply enchantments that usually require an anvil

## [v4.0.0-1.19] - 2022-07-19
- Ported to Minecraft 1.19
- Split into multi-loader project

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Gyular]: https://github.com/gyular
