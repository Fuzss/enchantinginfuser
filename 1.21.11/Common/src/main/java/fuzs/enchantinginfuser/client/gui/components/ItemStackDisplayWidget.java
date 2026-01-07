package fuzs.enchantinginfuser.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.ItemStack;

public class ItemStackDisplayWidget extends AbstractWidget {
    private final Font font;
    private final ItemStack itemStack;

    public ItemStackDisplayWidget(int x, int y, Font font, ItemStack itemStack) {
        super(x, y, 16, 16, CommonComponents.EMPTY);
        this.font = font;
        this.itemStack = itemStack;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.hasHighlight()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE,
                    this.getX() - 4,
                    this.getY() - 4,
                    24,
                    24);
        }

        guiGraphics.renderFakeItem(this.itemStack, this.getX(), this.getY());
        int posX = this.getX() + 19 - 2 - this.font.width(this.getMessage());
        int posY = this.getY() + 6 + 3;
        guiGraphics.drawString(this.font, this.getMessage(), posX, posY, -1);
        if (this.hasHighlight()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE,
                    this.getX() - 4,
                    this.getY() - 4,
                    24,
                    24);
        }
    }

    private boolean hasHighlight() {
        return this.isHoveredOrFocused() && this.tooltip.get() != null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // NO-OP
    }
}
