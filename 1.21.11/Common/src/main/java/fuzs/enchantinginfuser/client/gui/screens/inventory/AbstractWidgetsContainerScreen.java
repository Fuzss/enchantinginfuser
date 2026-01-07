package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * A custom {@link AbstractContainerScreen} extension that fully supports adding
 * {@link net.minecraft.client.gui.components.AbstractWidget}, but adding back the missing super calls to
 * {@link net.minecraft.client.gui.components.events.ContainerEventHandler}.
 */
public abstract class AbstractWidgetsContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public AbstractWidgetsContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /**
     * @see net.minecraft.client.gui.components.events.ContainerEventHandler#mouseScrolled(double, double, double,
     *         double)
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // AbstractContainerMenu::mouseScrolled does not call super, so this is copied from ContainerEventHandler::mouseScrolled
        if (this.getChildAt(mouseX, mouseY)
                .filter(listener -> listener.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                .isPresent()) {
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }

    /**
     * @see net.minecraft.client.gui.components.events.ContainerEventHandler#mouseDragged(MouseButtonEvent, double,
     *         double)
     */
    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        // AbstractContainerMenu::mouseDragged does not call super, so this is copied from ContainerEventHandler::mouseDragged
        // Fabric Api patches that in though so we only need it for NeoForge
        if (!ModLoaderEnvironment.INSTANCE.getModLoader().isFabricLike()) {
            if (this.getFocused() != null && this.isDragging()
                    && mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT && this.getFocused()
                    .mouseDragged(mouseButtonEvent, dragX, dragY)) {
                return true;
            }
        }

        return super.mouseDragged(mouseButtonEvent, dragX, dragY);
    }
}
