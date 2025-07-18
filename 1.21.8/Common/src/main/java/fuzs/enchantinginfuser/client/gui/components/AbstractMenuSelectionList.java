package fuzs.enchantinginfuser.client.gui.components;

import fuzs.puzzleslib.api.client.gui.v2.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A selection list implementation that can be used as part of a screen anywhere, without having to cover the whole
 * screen width.
 * <p>
 * Also, the scroll bar is mostly handled separately, and is placed outside the bounds of the actual list.
 */
public abstract class AbstractMenuSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/creative_inventory/scroller_disabled");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;

    private final int scrollbarOffset;

    public AbstractMenuSelectionList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, int scrollbarOffset) {
        super(minecraft, width, height, y, itemHeight);
        this.scrollbarOffset = scrollbarOffset;
        this.setX(x);
    }

    @Override
    protected E getEntryAtPosition(double mouseX, double mouseY) {
        // a trick to get around vanilla subtracting 4 without having to copy the whole method
        this.headerHeight -= 4;
        E entry = super.getEntryAtPosition(mouseX, mouseY);
        this.headerHeight += 4;
        return entry;
    }

    @Override
    public int getRowWidth() {
        return this.getWidth();
    }

    @Override
    protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        // add back height subtracted by vanilla for item outline, which we have removed
        super.renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, width, height + 4);
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int posX = this.scrollBarX();
        double scrollAmount = this.maxScrollAmount() > 0 ? this.scrollAmount() / this.maxScrollAmount() : 0;
        int posY = this.getY() + (int) (scrollAmount * (this.getHeight() - SCROLLER_HEIGHT));
        ResourceLocation resourceLocation = this.maxScrollAmount() > 0 ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                resourceLocation,
                posX,
                posY,
                SCROLLER_WIDTH,
                SCROLLER_HEIGHT);
    }

    @Override
    protected boolean scrollbarVisible() {
        return false;
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        // NO-OP
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // NO-OP
    }

    @Override
    protected int contentHeight() {
        return super.contentHeight() - 4;
    }

    @Override
    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.getHeight());
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + this.scrollbarOffset;
    }

    @Override
    public boolean updateScrolling(double mouseX, double mouseY, int button) {
        return this.scrolling = this.isValidClickButton(button) && this.isMouseOverScrollbar(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isValidClickButton(button)) {
            return false;
        }
        this.updateScrolling(mouseX, mouseY, button);
        if (this.scrolling) {
            this.setScrollAmountFromMouse(mouseY);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            this.setScrollAmountFromMouse(mouseY);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    protected void setScrollAmountFromMouse(double mouseY) {
        double scrollOffs = (mouseY - this.getY() - SCROLLER_HEIGHT / 2.0) / (this.getHeight() - SCROLLER_HEIGHT);
        this.setScrollAmount(Mth.clamp(scrollOffs, 0.0, 1.0) * this.maxScrollAmount());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || this.isMouseOverScrollbar(mouseX, mouseY);
    }

    protected boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return ScreenHelper.isHovering(this.scrollBarX(),
                this.getY(),
                SCROLLER_WIDTH,
                this.getHeight(),
                mouseX,
                mouseY);
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
        // NO-OP
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    public int getRowTop(int index) {
        return super.getRowTop(index) - 4;
    }
}
