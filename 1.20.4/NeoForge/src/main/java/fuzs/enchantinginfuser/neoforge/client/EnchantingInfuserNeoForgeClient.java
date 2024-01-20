package fuzs.enchantinginfuser.neoforge.client;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.EnchantingInfuserClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = EnchantingInfuser.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EnchantingInfuserNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuserClient::new);
    }
}
