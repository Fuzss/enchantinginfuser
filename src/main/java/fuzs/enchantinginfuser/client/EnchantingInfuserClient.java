package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserItemRenderer;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.enchantinginfuser.registry.ModRegistry;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EnchantingInfuser.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EnchantingInfuserClient {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        ScreenManager.register(ModRegistry.INFUSING_MENU_TYPE, InfuserScreen::new);
        ClientRegistry.bindTileEntityRenderer(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, InfuserItemRenderer::new);
    }

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre evt) {
        if (evt.getMap().location().equals(InfuserRenderer.BOOK_LOCATION.atlasLocation())) {
            evt.addSprite(InfuserRenderer.BOOK_LOCATION.texture());
        }
    }
}
