package fuzs.enchantinginfuser.forge;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.forge.init.ForgeModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuserForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
        ForgeModRegistry.touch();
    }
}
