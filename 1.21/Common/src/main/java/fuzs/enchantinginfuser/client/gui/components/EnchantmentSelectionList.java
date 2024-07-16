package fuzs.enchantinginfuser.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

public class EnchantmentSelectionList extends ContainerObjectSelectionList<EnchantmentSelectionList.Entry> {
    private static final int SCROLLER_SIZE = 12;

    private final int scrollbarOffset;

    public EnchantmentSelectionList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, int scrollbarOffset) {
        super(minecraft, width, height, y, itemHeight);
        this.scrollbarOffset = scrollbarOffset;
        this.setX(x);
    }

    @Override
    public int getRowWidth() {
        return this.getWidth();
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int posX = this.getScrollbarPosition();
        int posY = (int) this.getScrollAmount() * (this.getHeight() - SCROLLER_SIZE) / this.getMaxScroll() + this.getY();
        RenderSystem.enableBlend();
        int scrollerSize = SCROLLER_SIZE + 6;
        guiGraphics.blitSprite(InfuserScreen.INFUSER_LOCATION, posX - 3, posY - 3, scrollerSize, scrollerSize);
        RenderSystem.disableBlend();
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
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.getHeight());
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRowRight() + this.scrollbarOffset;
    }

    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = this.isValidClickButton(button) &&
                ScreenHelper.isHovering(this.getScrollbarPosition(), this.getY(), SCROLLER_SIZE, this.getHeight(), mouseX, mouseY);
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
    protected int getRowTop(int index) {
        return super.getRowTop(index) - 4;
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
}
