package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ForgeModRegistry;
import fuzs.enchantinginfuser.integration.ApotheosisEnchantStatsProvider;
import fuzs.puzzleslib.core.CommonFactories;
import fuzs.puzzleslib.core.ModLoaderEnvironment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuserForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        CommonFactories.INSTANCE.modConstructor(EnchantingInfuser.MOD_ID).accept(new EnchantingInfuser());
        ForgeModRegistry.touch();
        EnchantingInfuser.CONFIG.getHolder(ServerConfig.class).accept(() -> {
            if (!EnchantingInfuser.CONFIG.get(ServerConfig.class).apotheosisIntegration) {
                EnchantingInfuserAPI.setEnchantStatsProvider(EnchantStatsProvider.INSTANCE);
            } else if (ModLoaderEnvironment.INSTANCE.isModLoaded("apotheosis")) {
                EnchantingInfuserAPI.setEnchantStatsProvider(ApotheosisEnchantStatsProvider.INSTANCE);
            }
        });
    }
}
