package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserItemRenderer;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.ItemTooltipRegistry;

public class EnchantingInfuserClient implements ClientModConstructor {

    @Override
    public void onClientSetup() {
        ItemTooltipRegistry.registerItemTooltip(InfuserBlock.class, InfuserBlock::getDescriptionComponent);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.INFUSING_MENU_TYPE.value(), InfuserScreen::new);
        context.registerMenuScreen(ModRegistry.ADVANCED_INFUSING_MENU_TYPE.value(), InfuserScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.value(), InfuserItemRenderer::new);
    }
}
