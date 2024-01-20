package fuzs.enchantinginfuser;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class EnchantingInfuserFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
    }
}
