# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v3.3.3-1.18.2] - 2022-09-23
### Fixed
- Fixed no enchantment levels being taken when repairing items in an advanced infuser

## [v3.3.2-1.18.2] - 2022-07-30
### Fixed
- Fixed enchanting power not increasing above 15 bookshelves when a higher value is set in the config

## [v3.3.1-1.18.2] - 2022-07-24
### Added
- Added support for bookshelves from Apotheosis, they will now provide enchanting power (Eterna) to the infuser
- They will also allow the infuser to reach higher enchanting power values, depending on the type of bookshelf that is used
### Changed
- Made repair button more descriptive
### Fixed
- Fixed log spam due to server config not being loaded during start-up

## [v3.3.0-1.18.2] - 2022-07-18
### Added
- Added the ability to change/remove existing enchantments on items (by default only enabled for the advanced infuser)
- Added a button for repairing items in the infuser for the cost of a few levels (by default only enabled for the advanced infuser)
- Added support for the Apotheosis mod, now you can finally apply enchantments with their new max level to your gear using an enchanting infuser
### Changed
- All config options are now separate for each infuser, so you can separately customize the normal and advanced variant to your desires
- Increased amount of required levels for maxed out enchantments to 30 (from 25) for normal infuser, and to 20 (from 15) for advanced infuser
- Only 15 bookshelves are now required around an infuser, just like vanilla (this can be increased again back to 30 in the config if desired)
- Overhauled tooltips in the infuser screen, they now contain a lot more information
- Books can no longer be enchanted using a normal infuser (toggleable in the config)
- Enchanting infuser items now have a tooltip to explain what they are capable of

## [v3.2.0-1.18.2] - 2022-03-03
- Compiled for Minecraft 1.18.2

## [v3.1.1-1.18.1] - 2022-01-06
### Fixed
- Fixed trying to enchant a book leading to a crash when many mods that add enchantments are installed
- Fixed config option for curse enchantments not working as expected

## [v3.1.0-1.18.1] - 2021-12-17
- Compiled for Minecraft 1.18.1

## [v3.0.2-1.18] - 2021-12-03
### Changed
- The enchanting infuser now emit the same light level as vanilla enchanting tables do
### Fixed
- Fixed wrong check being used for when a gui button is hovered

## [v3.0.1-1.18] - 2021-12-02
### Changed
- Switched block entity data syncing to new vanilla methods

## [v3.0.0-1.18] - 2021-12-01
- Ported to Minecraft 1.18

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
