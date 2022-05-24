package fuzs.enchantinginfuser.compat;

import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.puzzleslib.core.ModLoaderEnvironment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ModCompatHandler {
    public static ForgeConfigSpec.BooleanValue apotheosis;

    public static void init() {
        setupConfig();
    }

    private static void setupConfig() {
        // should be built into puzzles in the future (1.19 hopefully)
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("compat");
        apotheosis = builder.comment("Enable compat for Apotheosis if it is installed. Allows for using the full range of changes Apotheosis applies to vanilla enchantments.", "Should only really be disabled if compat breaks due to internal changes.").define("apotheosis", true);
        builder.pop();
        // need to make this a common config instead of server as the value is used during start-up
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
    }

    public static void setup() {
        if (ModLoaderEnvironment.isModLoaded("apotheosis") && apotheosis.get()) {
            EnchantingInfuserAPI.setEnchantStatsProvider(new ApotheosisEnchantStatsProvider());
        }
    }
}
