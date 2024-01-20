package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserItemRenderer;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantingInfuserClient implements ClientModConstructor {

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        MenuScreens.register(ModRegistry.INFUSING_MENU_TYPE.get(), InfuserScreen::new);
        MenuScreens.register(ModRegistry.ADVANCED_INFUSING_MENU_TYPE.get(), InfuserScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.get(), InfuserItemRenderer::new);
    }
}
