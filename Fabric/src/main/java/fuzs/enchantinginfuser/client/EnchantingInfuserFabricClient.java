package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import net.fabricmc.api.ClientModInitializer;

public class EnchantingInfuserFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor(EnchantingInfuser.MOD_ID).accept(new EnchantingInfuserClient());
    }
}
