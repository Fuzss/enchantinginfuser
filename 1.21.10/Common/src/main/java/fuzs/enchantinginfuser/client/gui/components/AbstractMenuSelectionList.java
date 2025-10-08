package fuzs.enchantinginfuser.client.gui.components;

import fuzs.puzzleslib.api.client.gui.v2.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A selection list implementation that can be used as part of a screen anywhere, without having to cover the whole
 * screen width.
 * <p>
 * Also, the scroll bar is mostly handled separately and is placed outside the bounds of the actual list.
 */
public abstract class AbstractMenuSelectionList<E extends AbstractMenuSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {
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
    public int getRowWidth() {
        return this.getWidth();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
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
    protected int getFirstEntryY() {
        return this.getY();
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
    public boolean updateScrolling(MouseButtonEvent mouseButtonEvent) {
        return this.scrolling = this.isValidClickButton(mouseButtonEvent.buttonInfo()) && this.isMouseOverScrollbar(
                mouseButtonEvent.x(),
                mouseButtonEvent.y());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (!this.isValidClickButton(mouseButtonEvent.buttonInfo())) {
            return false;
        }
        this.updateScrolling(mouseButtonEvent);
        if (this.scrolling) {
            this.setScrollAmountFromMouse(mouseButtonEvent.y());
            return true;
        } else {
            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (this.scrolling) {
            this.setScrollAmountFromMouse(mouseButtonEvent.y());
            return true;
        } else {
            return super.mouseDragged(mouseButtonEvent, dragX, dragY);
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
    protected void renderSelection(GuiGraphics guiGraphics, E entry, int innerColor) {
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

    protected abstract static class Entry<E extends AbstractMenuSelectionList.Entry<E>> extends ContainerObjectSelectionList.Entry<E> {

        @Override
        public int getContentX() {
            return this.getX();
        }

        @Override
        public int getContentY() {
            return this.getY();
        }

        @Override
        public int getContentHeight() {
            return this.getHeight();
        }

        @Override
        public int getContentWidth() {
            return this.getWidth();
        }
    }
}
