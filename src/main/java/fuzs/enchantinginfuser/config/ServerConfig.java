package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig extends AbstractConfig {
    @Config(description = {"Maximum enchanting power provided by bookshelves to scale infuser costs by.", "This is basically how many bookshelves you need around the infuser to be able to apply maximum level enchantments.", "All bookshelves on the same level as the infuser and one block above are counted, all need to have a taxicab distance of exactly 2 blocks (meaning corners count too).", "This option only affects normal enchanting infusers."})
    @Config.IntRange(min = 0)
    public int maximumPowerNormal = 30;
    @Config(description = {"Maximum enchanting power provided by bookshelves to scale infuser costs by.", "This is basically how many bookshelves you need around the infuser to be able to apply maximum level enchantments.", "All bookshelves on the same level as the infuser and one block above are counted, all need to have a taxicab distance of exactly 2 blocks (meaning corners count too).", "This option only affects advanced enchanting infusers."})
    @Config.IntRange(min = 0)
    public int maximumPowerAdvanced = 30;
    @Config
    public CostsConfig costs = new CostsConfig();
    @Config
    public PowerConfig power = new PowerConfig();
    @Config
    public TypesConfig types = new TypesConfig();

    public ServerConfig() {
        super("");
    }

    public static class CostsConfig extends AbstractConfig {
        @Config(description = "Base cost multiplier for each level for common enchantments.")
        public int commonCost = 2;
        @Config(description = "Base cost multiplier for each level for uncommon enchantments.")
        public int uncommonCost = 3;
        @Config(description = "Base cost multiplier for each level for rare enchantments.")
        public int rareCost = 4;
        @Config(description = "Base cost multiplier for each level for very rare enchantments.")
        public int veryRareCost = 5;
        @Config(description = "Double prices for enchantments normally unobtainable from enchanting tables (e.g. mending, soul speed).")
        public boolean doubleUniques = true;
        @Config(description = {"Cost level to scale prices by. This is not a strict value, meaning it can be exceeded (e.g. when applying treasure enchantments).", "This option only affects normal enchanting infusers."})
        public int maximumCostNormal = 25;
        @Config(description = {"Cost level to scale prices by. This is not a strict value, meaning it can be exceeded (e.g. when applying treasure enchantments).", "This option only affects advanced enchanting infusers."})
        public int maximumCostAdvanced = 15;
        @Config(description = "When scaling costs, only account for vanilla enchantments. Otherwise enchanting costs will become ludicrously cheap with many modded enchantments present.")
        public boolean vanillaCostOnly = true;

        public CostsConfig() {
            super("costs");
        }
    }

    public static class PowerConfig extends AbstractConfig {
        @Config(description = "Multiplier for maximum enchanting power for when common enchantments become available.")
        @Config.DoubleRange(min = -1.0, max = 1.0)
        public double commonMultiplier = -0.2;
        @Config(description = "Multiplier for maximum enchanting power for when uncommon enchantments become available.")
        @Config.DoubleRange(min = -1.0, max = 1.0)
        public double uncommonMultiplier = 0.2;
        @Config(description = "Multiplier for maximum enchanting power for when rare enchantments become available.")
        @Config.DoubleRange(min = -1.0, max = 1.0)
        public double rareMultiplier = 0.4;
        @Config(description = "Multiplier for maximum enchanting power for when very rare enchantments become available.")
        @Config.DoubleRange(min = -1.0, max = 1.0)
        public double veryRareMultiplier = 0.6;
        @Config(description = "Multiplier for maximum enchanting power for how much power is required to max out an enchantment.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double levelMultiplier = 0.4;
        @Config(description = {"Multiplier for maximum enchanting power for when treasure enchantments become available.", "They also need to be enabled in the \"Types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double treasureMultiplier = 0.95;
        @Config(description = {"Multiplier for maximum enchanting power for when undiscoverable enchantments become available.", "They also need to be enabled in the \"Types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double undiscoverableMultiplier = 0.9;
        @Config(description = {"Multiplier for maximum enchanting power for when curse enchantments become available.", "They also need to be enabled in the \"Types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double curseMultiplier = 1.0;

        public PowerConfig() {
            super("power");
        }
    }

    public static class TypesConfig extends AbstractConfig {
        @Config(description = "Allow treasure enchantments (e.g. mending) to be applied using the enchanting infuser.")
        public boolean treasure = false;
        @Config(description = {"Allow undiscoverable enchantments (e.g. soul speed) to be applied using the enchanting infuser.", "This option takes precedence over other options for treasure and curse enchantments."})
        public boolean undiscoverable = false;
        @Config(description = {"Allow curses (e.g. curse of vanishing) to be applied using the enchanting infuser.", "This option takes precedence over option for treasure enchantments."})
        public boolean curses = false;

        public TypesConfig() {
            super("types");
        }
    }
}