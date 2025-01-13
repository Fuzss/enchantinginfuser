package fuzs.enchantinginfuser.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentSelectionList extends ContainerObjectSelectionList<EnchantmentSelectionList.Entry> {
    static final int SCROLLBAR_WIDTH = 12;
    static final int SCROLLER_SIZE = 18;

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
        int posY = (int) this.getScrollAmount() * (this.getHeight() - SCROLLBAR_WIDTH) / this.getMaxScroll() + this.getY();
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(InfuserScreen.INFUSER_LOCATION, posX - 3, posY - 3, SCROLLER_SIZE, SCROLLER_SIZE);
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
        this.scrolling = this.isValidClickButton(button) && this.isMouseOverScrollbar(mouseX, mouseY);
    }

    protected boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return ScreenHelper.isHovering(this.getScrollbarPosition(), this.getY(), SCROLLER_SIZE, this.getHeight(),
                mouseX, mouseY
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isMouseOver(mouseX, mouseY) || this.isMouseOverScrollbar(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        } else {
            return false;
        }
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
        private final List<AbstractWidget> children = new ArrayList<>();
        private final Holder<Enchantment> enchantment;

        public Entry(Holder<Enchantment> enchantment) {
            this.enchantment = enchantment;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
