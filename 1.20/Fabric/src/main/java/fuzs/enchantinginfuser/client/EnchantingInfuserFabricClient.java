package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class EnchantingInfuserFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuserClient::new);
    }
}
