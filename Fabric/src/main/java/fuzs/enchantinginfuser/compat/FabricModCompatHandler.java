package fuzs.enchantinginfuser.compat;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.CommonConfig;
import fuzs.puzzleslib.core.CoreServices;

public class FabricModCompatHandler {

    public static void setup() {
        if (CoreServices.ENVIRONMENT.isModLoaded("apotheosis") && EnchantingInfuser.CONFIG.get(CommonConfig.class).apotheosisCompat) {
//            EnchantingInfuserAPI.setEnchantStatsProvider(new ApotheosisEnchantStatsProvider());
        }
    }
}
