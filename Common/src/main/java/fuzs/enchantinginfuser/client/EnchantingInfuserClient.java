package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserItemRenderer;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.client.core.ClientModConstructor;

public class EnchantingInfuserClient implements ClientModConstructor {

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.get(), InfuserItemRenderer::new);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.INFUSING_MENU_TYPE.get(), InfuserScreen::new);
        context.registerMenuScreen(ModRegistry.ADVANCED_INFUSING_MENU_TYPE.get(), InfuserScreen::new);
    }

    @Override
    public void onRegisterAtlasSprites(AtlasSpritesContext context) {
        context.registerMaterial(InfuserRenderer.BOOK_LOCATION);
    }
}
