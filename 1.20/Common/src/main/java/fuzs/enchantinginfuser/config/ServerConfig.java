package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.ValueCallback;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig implements ConfigCore {
    @Config
    public InfuserConfig normalInfuser = new InfuserConfig();
    @Config
    public InfuserConfig advancedInfuser = new InfuserConfig();
    public boolean apotheosisIntegration = true;

    public ServerConfig() {
        this.advancedInfuser.allowRepairing = AllowedRepairItems.TOOLS_AND_ARMOR;
        this.advancedInfuser.allowBooks = true;
        this.advancedInfuser.allowModifyingEnchantments = ModifiableItems.ALL;
        this.advancedInfuser.costs.maximumCost = 20;
        this.advancedInfuser.types.allowAnvilEnchantments = true;
    }

    @Override
    public void addToBuilder(ForgeConfigSpec.Builder builder, ValueCallback callback) {
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isForge()) {
            builder.push("integration");
            callback.accept(builder.comment("Enable compat for Apotheosis if it is installed. Allows for using the full range of changes Apotheosis applies to vanilla enchantments.", "Should only really be disabled if compat breaks due to internal changes.").define("apotheosis", true), v -> this.apotheosisIntegration = v);
            builder.pop();
        }
    }

    public enum ModifiableItems {
        ALL, FULL_DURABILITY, UNENCHANTED
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
        @Config
        public RepairConfig repair = new RepairConfig();
        @Config(description = {"The main option in this section is \"maximum_cost\" as it determines how many levels you'll have to pay for fully enchanting an item with all possible enchantments it can have.", "Cost multipliers mainly control how this maximum cost will be spread out between enchantments of different rarities."})
        public CostsConfig costs = new CostsConfig();
        @Config(description = {"This section allows for controlling at what percentage of the total enchanting power certain kinds of enchantments become available.", "With default settings e.g. the first level of a rare enchantment will be available at 40% enchanting power (controlled by \"rare_multiplier\", translates to 40% * 15 = 6 bookshelves), and the maximum level for that enchant will be available at 40% + 40% = 80% enchanting power (controlled by \"rare_multiplier\" and \"rarity_range_multiplier\", translates to 80% * 15 = 12 bookshelves)."})
        public PowerConfig power = new PowerConfig();
        @Config
        public TypesConfig types = new TypesConfig();
    }

    public enum AllowedRepairItems {
        EVERYTHING, TOOLS_AND_ARMOR, NOTHING;

        public boolean isAllowedToRepair(ItemStack itemStack) {
            if (itemStack.isEmpty() || !itemStack.isDamaged()) return false;
            if (this == TOOLS_AND_ARMOR) return itemStack.getItem() instanceof TieredItem || itemStack.getItem() instanceof ArmorItem;
            return this == EVERYTHING;
        }

        public boolean isActive() {
            return this != NOTHING;
        }
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

    public static class PowerConfig implements ConfigCore {
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
        public double rarityRange = 0.4;
        @Config(description = {"Multiplier for maximum enchanting power for when treasure enchantments become available.", "They also need to be enabled in the \"types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double treasureMultiplier = 0.95;
        @Config(description = {"Multiplier for maximum enchanting power for when undiscoverable enchantments become available.", "They also need to be enabled in the \"types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double undiscoverableMultiplier = 0.9;
        @Config(description = {"Multiplier for maximum enchanting power for when untradeable enchantments become available.", "They also need to be enabled in the \"types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double untradeableMultiplier = 0.9;
        @Config(description = {"Multiplier for maximum enchanting power for when curse enchantments become available.", "They also need to be enabled in the \"types\" config."})
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double curseMultiplier = 1.0;
    }

    public static class TypesConfig implements ConfigCore {
        @Config(description = "Allow enchantments that can normally not be obtained from an enchanting table, but can be put on the item in an anvil (e.g. sharpness on an axe).")
        public boolean allowAnvilEnchantments = false;
        @Config(description = {"Allow undiscoverable enchantments (e.g. soul speed) to be applied using the enchanting infuser.", "This option takes precedence over other options for treasure, curse and tradeable enchantments."})
        public boolean allowUndiscoverableEnchantments = false;
        @Config(description = {"Allow untradeable enchantments (e.g. soul speed) to be applied using the enchanting infuser.", "This option takes precedence over other options for treasure and curse enchantments."})
        public boolean allowUntradeableEnchantments = false;
        @Config(description = {"Allow curses (e.g. curse of vanishing) to be applied using the enchanting infuser.", "This option takes precedence over option for treasure enchantments (as curses are also treasure enchantments internally)."})
        public boolean allowCursesEnchantments = false;
        @Config(description = "Allow treasure enchantments (e.g. mending) to be applied using the enchanting infuser.")
        public boolean allowTreasureEnchantments = false;
    }
}