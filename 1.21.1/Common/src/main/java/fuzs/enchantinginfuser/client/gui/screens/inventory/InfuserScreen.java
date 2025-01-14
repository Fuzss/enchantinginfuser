package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.components.EnchantmentSelectionList;
import fuzs.enchantinginfuser.client.gui.components.InfuserEnchantButton;
import fuzs.enchantinginfuser.client.gui.components.InfuserMenuButton;
import fuzs.enchantinginfuser.client.gui.components.InfuserRepairButton;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.TooltipBuilder;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> implements ContainerListener {
    public static final ResourceLocation INFUSER_LOCATION = EnchantingInfuser.id(
            "textures/gui/container/enchanting_infuser.png");
    private static final int BUTTONS_OFFSET_X = 7;
    private static final int ENCHANT_BUTTON_OFFSET_Y = 44;
    private static final int ENCHANT_ONLY_BUTTON_OFFSET_Y = 55;
    private static final int REPAIR_BUTTON_OFFSET_Y = 66;

    public final int enchantmentSeed = new Random().nextInt();
    private static boolean isPowerTooLow;
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    private EnchantmentSelectionList scrollingList;
    private boolean ignoreTextInput;
    private AbstractWidget powerWidget;
    private InfuserMenuButton enchantButton;
    @Nullable
    private InfuserMenuButton repairButton;

    public InfuserScreen(InfuserMenu infuserMenu, Inventory inventory, Component title) {
        super(infuserMenu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 185;
        this.inventoryLabelX = 30;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public static void setIsPowerTooLow(boolean isPowerTooLow) {
        InfuserScreen.isPowerTooLow = isPowerTooLow;
    }

    @Override
    public void containerTick() {
        this.refreshButtons();
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(this.font, this.leftPos + 67, this.topPos + 6, 116, 9,
                Component.translatable("itemGroup.search")
        );
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0XFFFFFF);
        this.addWidget(this.searchBox);
        this.scrollingList = new EnchantmentSelectionList(this,this.leftPos + 30, this.topPos + 18);
        this.addRenderableWidget(this.scrollingList);
        this.powerWidget = this.addRenderableOnly(new AbstractWidget(this.leftPos + 196, this.topPos + 161, 16, 16, CommonComponents.EMPTY) {

            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.pose().pushPose();
                guiGraphics.renderFakeItem(new ItemStack(Items.BOOKSHELF), this.getX(), this.getY());
                int posX = this.getX() + 19 - 2 - InfuserScreen.this.font.width(this.getMessage());
                int posY = this.getY() + 6 + 3;
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                guiGraphics.drawString(InfuserScreen.this.font, this.getMessage(), posX, posY,
                        this.getStringColor().getColor()
                );
                if (this.isHoveredOrFocused()) {
                    renderSlotHighlight(guiGraphics, this.getX(), this.getY(), 0);
                }
                guiGraphics.pose().popPose();
            }

            private ChatFormatting getStringColor() {
                if (InfuserScreen.this.menu.getEnchantmentPower() >= InfuserScreen.this.menu.getEnchantmentPowerLimit()) {
                    return ChatFormatting.YELLOW;
                } else if (InfuserScreen.this.isPowerTooLow) {
                    return ChatFormatting.RED;
                } else {
                    return ChatFormatting.WHITE;
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return false;
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
                // NO-OP
            }
        });
        this.enchantButton = this.addRenderableWidget(new InfuserEnchantButton(this, this.leftPos + BUTTONS_OFFSET_X,
                this.topPos + (this.menu.behavior.getConfig().allowRepairing.isActive() ? ENCHANT_BUTTON_OFFSET_Y :
                        ENCHANT_ONLY_BUTTON_OFFSET_Y), (Button button) -> {
            if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.ENCHANT_BUTTON)) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0);
            }
            this.searchBox.setValue("");
        }
        ));
        if (this.menu.behavior.getConfig().allowRepairing.isActive()) {
            this.repairButton = this.addRenderableWidget(new InfuserRepairButton(this, this.leftPos + BUTTONS_OFFSET_X,
                    this.topPos + REPAIR_BUTTON_OFFSET_Y, (Button button) -> {
                if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.REPAIR_BUTTON)) {
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
                }
            }
            ));
        } else {
            this.repairButton = null;
        }
        this.refreshEnchantingPower(this.menu.getEnchantmentPower());
        this.refreshButtons();
        this.menu.addSlotListener(this);
    }

    private void refreshEnchantingPower(int enchantmentPower) {
        this.powerWidget.setMessage(Component.literal(String.valueOf(enchantmentPower)));
        int enchantmentPowerLimit = this.menu.getEnchantmentPowerLimit();
        TooltipBuilder builder = TooltipBuilder.create().splitLines(200).addLines(Component.translatable(EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER, enchantmentPower,
                enchantmentPowerLimit
        ).withStyle(ChatFormatting.YELLOW));
        if (enchantmentPower < enchantmentPowerLimit) {
            builder.addLines(Component.translatable(InfuserMenuButton.KEY_TOOLTIP_HINT).withStyle(ChatFormatting.GRAY));
        }
        builder.build(this.powerWidget);
    }

    private void refreshButtons() {
        this.enchantButton.active = this.menu.canEnchant(this.minecraft.player);
        if (this.repairButton != null) {
            this.repairButton.active = this.menu.canRepair(this.minecraft.player);
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.searchBox.getValue();
        super.resize(minecraft, width, height);
        this.searchBox.setValue(s);
        this.refreshSearchResults();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.charTyped(codePoint, modifiers)) {
                if (!Objects.equals(s, this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        if (!this.searchBox.isFocused()) {
            if (this.minecraft.options.keyChat.matches(keyCode, scanCode)) {
                this.ignoreTextInput = true;
                this.searchBox.setFocused(true);
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            boolean flag = this.hoveredSlot != null && this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent();
            if (flag && flag1 && this.checkHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String s = this.searchBox.getValue();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(s, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }
                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != InputConstants.KEY_ESCAPE || super.keyPressed(
                            keyCode, scanCode, modifiers);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void refreshSearchResults() {
        // TODO this should use our new scroll bar
        int size = this.scrollingList.children().size();
        this.scrollingList.clearEntries();
        ItemEnchantments itemEnchantments = this.menu.getItemEnchantments();
        Set<Holder<Enchantment>> enchantments = this.menu.getAllEnchantments();
        HolderLookup.Provider registries = this.minecraft.getConnection().registryAccess();
        HolderSet<Enchantment> holders = registries.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(EnchantmentTags.TOOLTIP_ORDER);
        for (Holder<Enchantment> enchantment : holders) {
            if (enchantments.contains(enchantment) && this.matchesSearch(enchantment)) {
                InfuserMenu.EnchantmentValues enchantmentValues = this.menu.getEnchantmentValues(enchantment);
                EnchantmentComponent enchantmentComponent = EnchantmentComponent.create(enchantment,
                        enchantmentValues,
                        itemEnchantments);
                this.scrollingList.addEntry(enchantment, enchantmentComponent);
            }
        }
        for (Holder<Enchantment> enchantment : enchantments) {
            if (!holders.contains(enchantment) && this.matchesSearch(enchantment)) {
                InfuserMenu.EnchantmentValues enchantmentValues = this.menu.getEnchantmentValues(enchantment);
                EnchantmentComponent enchantmentComponent = EnchantmentComponent.create(enchantment,
                        enchantmentValues,
                        itemEnchantments);
                this.scrollingList.addEntry(enchantment, enchantmentComponent);
            }
        }
        if (size != this.scrollingList.children().size()) {
//            this.scrollOffs = 0.0F;
            this.scrollingList.setScrollAmount(0.0);
        }
    }

    private boolean matchesSearch(Holder<Enchantment> enchantment) {
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        if (s.isEmpty()) {
            return true;
        } else if (this.menu.getAvailableEnchantmentLevel(enchantment) == 0) {
            return false;
        } else {
            return EnchantmentTooltipHelper.getDisplayName(enchantment).getString().toLowerCase(Locale.ROOT)
                    .contains(s);
        }
    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (button == 0) {
//            if (this.insideScrollbar(mouseX, mouseY)) {
//                this.scrolling = this.scrollingList.canScroll();
//                return true;
//            }
//        }
//        return super.mouseClicked(mouseX, mouseY, button);
//    }
//
//    protected boolean insideScrollbar(double mouseX, double mouseY) {
//        int fromX = this.leftPos + 197;
//        int fromY = this.topPos + 17;
//        int toX = fromX + 14;
//        int toY = fromY + 72;
//        return mouseX >= (double) fromX && mouseY >= (double) fromY && mouseX < (double) toX && mouseY < (double) toY;
//    }
//
//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        if (button == 0) {
//            this.scrolling = false;
//        }
//        return super.mouseReleased(mouseX, mouseY, button);
//    }
//
//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
//        if (!this.scrollingList.canScroll()) {
//            return false;
//        } else {
//            this.scrollOffs = (float) ((double) this.scrollOffs - scrollY / (this.scrollingList.getItemCount() - 4));
//            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
//            this.scrollingList.scrollTo(this.scrollOffs);
//            return true;
//        }
//    }
//
//    @Override
//    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
//        if (this.scrolling) {
//            int i = this.topPos + 17;
//            int j = i + 72;
//            this.scrollOffs = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
//            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
//            this.scrollingList.scrollTo(this.scrollOffs);
//            return true;
//        } else {
//            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
//        }
//    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.isPowerTooLow = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
//        this.scrollingList.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(INFUSER_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        int sliderX = this.leftPos + 197 - 2;
//        int sliderY = this.topPos + 17 - 2;
//        int sliderRange = sliderY + 72 + 2 + 2;
//        guiGraphics.blit(INFUSER_LOCATION, sliderX,
//                sliderY + (int) ((float) (sliderRange - sliderY - 18) * this.scrollOffs), 220,
//                54 + (this.scrollingList.canScroll() ? 18 : 0), 18, 18
//        );
        // render slot manually and do not include it as part of the background texture file,
        // so it can be placed further down when repairing is disabled
        guiGraphics.blit(INFUSER_LOCATION, this.leftPos + 8 - 1,
                this.topPos + (this.menu.behavior.getConfig().allowRepairing.isActive() ? 23 : 34) - 1, 196, 185, 18, 18
        );
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        // NO-OP
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        switch (dataSlotIndex) {
            case InfuserMenu.ENCHANTMENT_POWER_DATA_SLOT -> this.refreshEnchantingPower(value);
            case InfuserMenu.ENCHANTING_COST_DATA_SLOT ->
                    this.enchantButton.refreshTooltip(this.menu.getEnchantableStack());
            case InfuserMenu.REPAIR_COST_DATA_SLOT -> {
                if (this.repairButton != null) {
                    this.repairButton.refreshTooltip(this.menu.getEnchantableStack());
                }
            }
        }
    }

    private class ScrollingList extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private final List<EnchantmentListEntry> children = new ArrayList<>();
        private final int posX;
        private final int posY;
        private final int itemWidth;
        private final int itemHeight;
        private final int length;
        private int scrollPosition;

        public ScrollingList(int posX, int posY, int itemWidth, int itemHeight, int length) {
            this.posX = posX;
            this.posY = posY;
            this.itemWidth = itemWidth;
            this.itemHeight = itemHeight;
            this.length = length;
        }

        public void scrollTo(float pos) {
            if (this.canScroll()) {
                // important to round instead of int cast
                this.scrollPosition = Math.round((this.getItemCount() - this.length) * pos);
            } else {
                this.scrollPosition = 0;
            }
        }

        public boolean canScroll() {
            return this.getItemCount() > this.length;
        }

        protected final void clearEntries() {
            this.children.clear();
        }

        protected void addEntry(EnchantmentListEntry entry) {
            this.children.add(entry);
            entry.setParentList(this);
            this.markOthersIncompatible();
        }

        protected int getItemCount() {
            return this.children.size();
        }

        public void markOthersIncompatible() {
            final List<EnchantmentListEntry> activeEnchants = this.children.stream()
                    .filter(EnchantmentListEntry::isApplied)
                    .toList();
            for (EnchantmentListEntry entry : this.children) {
                if (!entry.isApplied()) {
                    entry.markIncompatible(activeEnchants.stream()
                            .filter(e -> e.isIncompatibleWith(entry))
                            .collect(Collectors.toSet()));
                }
            }
        }

        @Nullable
        protected final EnchantmentListEntry getEntryAtPosition(double mouseX, double mouseY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                final int index = this.scrollPosition + (int) ((mouseY - this.posY) / this.itemHeight);
                return index < this.children.size() ? this.children.get(index) : null;
            } else {
                return null;
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return ScreenHelper.isHovering(this.posX, this.posY, this.itemWidth, this.itemHeight * this.length, mouseX,
                    mouseY
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                EnchantmentListEntry entry = this.getEntryAtPosition(mouseX, mouseY);
                if (entry != null) {
                    if (entry.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(entry);
                        this.setDragging(true);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(mouseX, mouseY, button);
            } else {
                return false;
            }
        }

        @Override
        public List<EnchantmentListEntry> children() {
            return this.children;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            for (int i = 0; i < Math.min(this.length, this.getItemCount()); i++) {
                this.children.get(this.scrollPosition + i)
                        .render(guiGraphics, this.posX, this.posY + this.itemHeight * i, this.itemWidth,
                                this.itemHeight, mouseX, mouseY, partialTick
                        );
            }
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarratableEntry.NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
            // NO-OP
        }
    }

    private class EnchantmentListEntry implements ContainerEventHandler {
        private final Holder<Enchantment> enchantment;
        private final Button removeButton;
        private final Button addButton;
        private int enchantmentLevel;
        private ScrollingList parentList;
        @Nullable
        private GuiEventListener focused;
        private boolean dragging;
        private Collection<Holder<Enchantment>> incompatibleEnchantments = Collections.emptySet();

        public EnchantmentListEntry(Holder<Enchantment> enchantment, int enchantmentLevel) {
            this.enchantment = enchantment;
            this.enchantmentLevel = enchantmentLevel;
            this.removeButton = new SpritelessImageButton(0, 0, 18, 18, 220, 0, INFUSER_LOCATION, button -> {
                do {
                    int newLevel = InfuserScreen.this.menu.clickEnchantmentLevelButton(this.enchantment,
                            ServerboundEnchantmentLevelMessage.Operation.REMOVE
                    );
                    if (newLevel != this.enchantmentLevel) {
                        this.enchantmentLevel = newLevel;
                        EnchantingInfuser.NETWORK.sendToServer(
                                new ServerboundEnchantmentLevelMessage(InfuserScreen.this.menu.containerId,
                                        this.enchantment, ServerboundEnchantmentLevelMessage.Operation.REMOVE
                                ));
                        this.updateButtons();
                        this.parentList.markOthersIncompatible();
                    }
                } while (button.active && button.visible && Screen.hasShiftDown());
            }) {

                @Override
                public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                    if (this.active && Screen.hasShiftDown()) {
                        RenderSystem.enableDepthTest();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                        int index = !this.active ? 0 : this.isHoveredOrFocused() ? 2 : 1;
                        guiGraphics.blit(this.resourceLocation, this.getX() + 2, this.getY(), this.xTexStart,
                                this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                                this.textureHeight
                        );
                        guiGraphics.blit(this.resourceLocation, this.getX() - 4, this.getY(), this.xTexStart,
                                this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                                this.textureHeight
                        );
                        this.refreshTooltip();
                    } else {
                        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                        this.refreshTooltip();
                    }
                }

                private void refreshTooltip() {
                    if (this.isHoveredOrFocused()) {
                        if (EnchantmentListEntry.this.enchantmentLevel - 1 >= EnchantmentListEntry.this.getMaximumLevel() && !EnchantmentListEntry.this.isObfuscated()) {
                            EnchantmentListEntry.this.setWeakPowerTooltip(
                                    EnchantmentTooltipHelper.MODIFY_LEVEL_COMPONENT);
                        }
                    }
                }
            }.setTextureLayout(SpritelessImageButton.LEGACY_TEXTURE_LAYOUT);
            this.addButton = new SpritelessImageButton(0, 0, 18, 18, 238, 0, INFUSER_LOCATION, button -> {
                do {
                    int newLevel = InfuserScreen.this.menu.clickEnchantmentLevelButton(this.enchantment,
                            ServerboundEnchantmentLevelMessage.Operation.ADD
                    );
                    if (newLevel != this.enchantmentLevel) {
                        this.enchantmentLevel = newLevel;
                        EnchantingInfuser.NETWORK.sendToServer(
                                new ServerboundEnchantmentLevelMessage(InfuserScreen.this.menu.containerId,
                                        this.enchantment, ServerboundEnchantmentLevelMessage.Operation.ADD
                                ));
                        this.updateButtons();
                        this.parentList.markOthersIncompatible();
                    }
                } while (button.active && button.visible && Screen.hasShiftDown());
            }) {

                @Override
                public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                    if (this.active && Screen.hasShiftDown()) {
                        RenderSystem.enableDepthTest();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                        int index = !this.active ? 0 : this.isHoveredOrFocused() ? 2 : 1;
                        guiGraphics.blit(this.resourceLocation, this.getX() - 2, this.getY(), this.xTexStart,
                                this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                                this.textureHeight
                        );
                        guiGraphics.blit(this.resourceLocation, this.getX() + 4, this.getY(), this.xTexStart,
                                this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                                this.textureHeight
                        );
                        this.refreshTooltip();
                    } else {
                        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                        this.refreshTooltip();
                    }
                }

                private void refreshTooltip() {
                    if (this.isHoveredOrFocused()) {
                        if (EnchantmentListEntry.this.enchantmentLevel >= EnchantmentListEntry.this.getMaximumLevel() && !EnchantmentListEntry.this.isObfuscated()) {
                            EnchantmentListEntry.this.setWeakPowerTooltip(
                                    EnchantmentTooltipHelper.INCREASE_LEVEL_COMPONENT);
                        }
                    }
                }
            }.setTextureLayout(SpritelessImageButton.LEGACY_TEXTURE_LAYOUT);
            this.updateButtons();
        }

        private void setWeakPowerTooltip(Component component) {
//            List<FormattedCharSequence> lines = EnchantmentTooltipHelper.getWeakPowerTooltip(
//                    InfuserScreen.this.menu.getEnchantmentPower(), menu.getRequiredEnchantmentPower(this.enchantment),
//                    component
//            );
//            InfuserScreen.this.setTooltipForNextRenderPass(lines);
            InfuserScreen.this.isPowerTooLow = true;
        }

        public void setParentList(ScrollingList parentList) {
            this.parentList = parentList;
        }

        private int getMaximumLevel() {
            return menu.getAvailableEnchantmentLevel(this.enchantment);
        }

        public void markIncompatible(Collection<EnchantmentListEntry> incompatibleList) {
            this.incompatibleEnchantments = incompatibleList.stream()
                    .map(entry -> entry.enchantment)
                    .collect(Collectors.toSet());
            final boolean compatible = incompatibleList.isEmpty();
            if (!compatible) this.enchantmentLevel = 0;
            this.updateButtons();
            this.removeButton.active = compatible;
            this.addButton.active &= compatible;
        }

        private void updateButtons() {
            this.removeButton.visible = this.enchantmentLevel > 0;
            this.addButton.visible = this.enchantmentLevel < EnchantmentAdapter.get().getMaxLevel(this.enchantment);
            this.removeButton.active = this.enchantmentLevel - 1 < this.getMaximumLevel();
            this.addButton.active = this.enchantmentLevel < this.getMaximumLevel();
        }

        public boolean isApplied() {
            return this.enchantmentLevel > 0;
        }

        public boolean isIncompatible() {
            return !this.incompatibleEnchantments.isEmpty();
        }

        public boolean isObfuscated() {
            return this.getMaximumLevel() == 0;
        }

        private int getYImage() {
            return this.isIncompatible() || this.isObfuscated() ? 0 : this.isApplied() ? 2 : 1;
        }

        public boolean isIncompatibleWith(EnchantmentListEntry other) {
            if (other == this) return false;
            return (this.isApplied() || other.isApplied()) && !EnchantmentAdapter.get()
                    .areCompatible(this.enchantment, other.enchantment);
        }

        public void render(GuiGraphics guiGraphics, int leftPos, int topPos, int width, int height, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, INFUSER_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(INFUSER_LOCATION, leftPos + 18, topPos, 0, 185 + this.getYImage() * 18, 126, 18);
            FormattedCharSequence formattedCharSequence = this.getDisplayName(this.enchantment, width);
            guiGraphics.drawCenteredString(InfuserScreen.this.font, formattedCharSequence, leftPos + width / 2,
                    topPos + 5, this.isIncompatible() || this.isObfuscated() ? 0X685E4A : -1
            );
            this.refreshTooltip(leftPos, topPos, mouseX, mouseY);
            this.removeButton.setX(leftPos);
            this.removeButton.setY(topPos);
            this.removeButton.render(guiGraphics, mouseX, mouseY, partialTicks);
            this.addButton.setX(leftPos + width - 18);
            this.addButton.setY(topPos);
            this.addButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        private void refreshTooltip(int leftPos, int topPos, int mouseX, int mouseY) {
            if (mouseX >= leftPos + 18 && mouseX < leftPos + 18 + 126 && mouseY >= topPos && mouseY < topPos + 18) {
                if (this.isObfuscated()) {
                    this.setWeakPowerTooltip(EnchantmentTooltipHelper.UNKNOWN_ENCHANT_COMPONENT);
                } else if (this.isIncompatible()) {
//                    InfuserScreen.this.setTooltipForNextRenderPass(
//                            EnchantmentTooltipHelper.getIncompatibleEnchantmentsTooltip(this.incompatibleEnchantments));
                } else {
//                    InfuserScreen.this.setTooltipForNextRenderPass(
//                            EnchantmentTooltipHelper.getEnchantmentTooltip(this.enchantment));
                }
            }
        }

        private FormattedCharSequence getDisplayName(Holder<Enchantment> enchantment, int maxWidth) {
            if (this.isObfuscated()) {
                int enchantmentId = InfuserScreen.this.minecraft.getConnection()
                        .registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getIdOrThrow(enchantment.value());
                EnchantmentNames.getInstance().initSeed(InfuserScreen.this.enchantmentSeed + enchantmentId);
                maxWidth = (int) (maxWidth * 0.72F);
                FormattedText randomName = EnchantmentNames.getInstance()
                        .getRandomName(InfuserScreen.this.font, maxWidth);
                List<FormattedCharSequence> lines = InfuserScreen.this.font.split(randomName, maxWidth);
                if (!lines.isEmpty()) {
                    return lines.getFirst();
                } else {
                    return Component.literal("???????").getVisualOrderText();
                }
            }
            if (this.isApplied()) {
                return EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, this.enchantmentLevel)
                        .getVisualOrderText();
            } else {
                return EnchantmentTooltipHelper.getDisplayName(enchantment).getVisualOrderText();
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.removeButton, this.addButton);
        }

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean isDragging) {
            this.dragging = isDragging;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            this.focused = focused;
        }

        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }
    }
}
