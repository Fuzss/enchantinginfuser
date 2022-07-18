package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.compat.FabricModCompatHandler;
import fuzs.enchantinginfuser.init.FabricModRegistry;
import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;

public class EnchantingInfuserFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor().accept(new EnchantingInfuser());
        FabricModRegistry.touch();
        FabricModCompatHandler.setup();
    }
}
