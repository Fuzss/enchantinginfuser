package fuzs.enchantinginfuser.client;

import fuzs.puzzleslib.client.core.ClientCoreServices;
import net.fabricmc.api.ClientModInitializer;

public class EnchantingInfuserFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor().accept(new EnchantingInfuserClient());
    }
}
