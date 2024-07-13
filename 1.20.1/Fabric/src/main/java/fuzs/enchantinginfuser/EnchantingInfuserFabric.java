package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.integration.apotheosis.ApotheosisEnchantStatsProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.fabricmc.api.ModInitializer;

public class EnchantingInfuserFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
        registerModIntegrations();
    }

    private static void registerModIntegrations() {
        EnchantingInfuser.CONFIG.getHolder(ServerConfig.class).accept(() -> {
            if (!EnchantingInfuser.CONFIG.get(ServerConfig.class).apotheosisIntegration) {
                EnchantingInfuserAPI.setEnchantStatsProvider(EnchantStatsProvider.INSTANCE);
            } else if (ModLoaderEnvironment.INSTANCE.isModLoaded("zenith")) {
                EnchantingInfuserAPI.setEnchantStatsProvider(ApotheosisEnchantStatsProvider.INSTANCE);
            }
        });
    }
}
