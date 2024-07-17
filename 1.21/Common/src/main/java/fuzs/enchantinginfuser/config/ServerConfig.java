package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    @Config
    public InfuserConfig normalInfuser = new InfuserConfig();
    @Config
    public InfuserConfig advancedInfuser = new InfuserConfig();

    public ServerConfig() {
        this.advancedInfuser.allowRepairing = AllowedRepairItems.TOOLS_AND_ARMOR;
        this.advancedInfuser.allowBooks = true;
        this.advancedInfuser.allowModifyingEnchantments = ModifiableItems.ALL;
        this.advancedInfuser.costs.maximumCost = 20;
        this.advancedInfuser.allowAnvilEnchantments = true;
    }

    public static class InfuserConfig implements ConfigCore {
        @Config(description = {"How many bookshelves you need around the infuser to be able to apply maximum level enchantments.", "Filling in corners is important to reach higher values.", "Setting very high values may require modded bookshelves that provide more than one enchanting power per block (such as Apotheosis' bookshelves).", "This value will be fixed at 50 when Apotheosis is installed."})
        @Config.IntRange(min = 0)
        public int maximumBookshelves = 15;
        @Config(description = "Allow enchantments on an already enchanted item to be increased / removed.")
        public ModifiableItems allowModifyingEnchantments = ModifiableItems.UNENCHANTED;
        @Config(description = "Allow books to be enchanted in an infuser.")
        public boolean allowBooks = false;
        @Config(description = "Can the enchanting infuser repair items using levels in addition to enchanting.")
        public AllowedRepairItems allowRepairing = AllowedRepairItems.NOTHING;
        @Config(description = "Working an item in an infuser increases the prior work penalty stat used by anvils for that item, meaning future anvil repairs will become more expensive, possibly even too expensive.")
        public boolean increaseAnvilRepairCost = false;
        @Config
        public RepairConfig repair = new RepairConfig();
        @Config(description = {"The main option in this section is \"maximum_cost\" as it determines how many levels you'll have to pay for fully enchanting an item with all possible enchantments it can have.", "Cost multipliers mainly control how this maximum cost will be spread out between enchantments of different rarities."})
        public CostsConfig costs = new CostsConfig();
        @Config(description = "Allow enchantments that can normally not be obtained from an enchanting table, but can be put on the item in an anvil (e.g. sharpness on an axe).")
        public boolean allowAnvilEnchantments = false;
    }

    public static class RepairConfig implements ConfigCore {
        @Config(description = "How many percentage points of an items total durability a single repair will restore.")
        @Config.DoubleRange(min = 0.1, max = 1.0)
        public double repairPercentageStep = 0.25;
        @Config(description = "Cost multiplier in levels for each repair step, result will be rounded up.")
        @Config.DoubleRange(min = 0.0)
        public double repairStepMultiplier = 2.0;
    }

    public static class CostsConfig implements ConfigCore {
        @Config(description = "Base cost multiplier for each level for common enchantments.")
        @Config.IntRange(min = 1)
        public int commonCostMultiplier = 2;
        @Config(description = "Base cost multiplier for each level for uncommon enchantments.")
        @Config.IntRange(min = 1)
        public int uncommonCostMultiplier = 3;
        @Config(description = "Base cost multiplier for each level for rare enchantments.")
        @Config.IntRange(min = 1)
        public int rareCostMultiplier = 4;
        @Config(description = "Base cost multiplier for each level for very rare enchantments.")
        @Config.IntRange(min = 1)
        public int veryRareCostMultiplier = 5;
        @Config(description = "Double prices for enchantments normally unobtainable from enchanting tables if they are enabled (e.g. mending, soul speed) when they are enabled.")
        public boolean doubleUniques = true;
        @Config(description = {"Cost level to scale prices by. This is not a strict value, meaning it can be exceeded (e.g. when applying treasure enchantments)."})
        @Config.IntRange(min = 1)
        public int maximumCost = 30;
        @Config(description = "When scaling costs, only account for vanilla enchantments. Otherwise enchanting costs will become ludicrously cheap with many modded enchantments present.")
        public boolean scaleCostsByVanillaOnly = true;
    }
}