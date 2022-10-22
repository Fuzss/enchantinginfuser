package fuzs.enchantinginfuser;

import fuzs.puzzleslib.core.CommonFactories;
import net.fabricmc.api.ModInitializer;

public class EnchantingInfuserFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonFactories.INSTANCE.modConstructor(EnchantingInfuser.MOD_ID).accept(new EnchantingInfuser());
    }
}
