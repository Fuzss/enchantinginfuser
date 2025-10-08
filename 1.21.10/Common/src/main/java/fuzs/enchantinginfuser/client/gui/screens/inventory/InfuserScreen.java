package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.components.*;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.ClientComponentSplitter;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> implements ContainerListener {
    public static final ResourceLocation INFUSER_LOCATION = EnchantingInfuser.id(
            "textures/gui/container/enchanting_infuser.png");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/slot_highlight_front");
    private static final int BUTTONS_OFFSET_X = 7;
    private static final int ENCHANT_BUTTON_OFFSET_Y = 44;
    private static final int ENCHANT_ONLY_BUTTON_OFFSET_Y = 55;
    private static final int REPAIR_BUTTON_OFFSET_Y = 66;

    private static boolean isPowerTooLow;
    public final int enchantmentSeed = new Random().nextInt();
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
        this.menu.addSlotListener(this);
    }

    public static void setIsPowerTooLow(boolean isPowerTooLow) {
        InfuserScreen.isPowerTooLow = isPowerTooLow;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(this.font,
                this.leftPos + 67,
                this.topPos + 6,
                116,
                9,
                Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(-1);
        this.addWidget(this.searchBox);
        this.scrollingList = new EnchantmentSelectionList(this.leftPos + 30, this.topPos + 18);
        this.addRenderableWidget(this.scrollingList);
        this.powerWidget = this.addRenderableOnly(new AbstractWidget(this.leftPos + 196,
                this.topPos + 161,
                16,
                16,
                CommonComponents.EMPTY) {

            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (this.isHoveredOrFocused()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            SLOT_HIGHLIGHT_BACK_SPRITE,
                            this.getX() - 4,
                            this.getY() - 4,
                            24,
                            24);
                }
                guiGraphics.renderFakeItem(new ItemStack(Items.BOOKSHELF), this.getX(), this.getY());
                int posX = this.getX() + 19 - 2 - InfuserScreen.this.font.width(this.getMessage());
                int posY = this.getY() + 6 + 3;
                guiGraphics.drawString(InfuserScreen.this.font,
                        this.getMessage(),
                        posX,
                        posY,
                        ARGB.opaque(this.getStringColor().getColor()));
                if (this.isHoveredOrFocused()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            SLOT_HIGHLIGHT_FRONT_SPRITE,
                            this.getX() - 4,
                            this.getY() - 4,
                            24,
                            24);
                }
            }

            private ChatFormatting getStringColor() {
                if (InfuserScreen.this.menu.getEnchantmentPower()
                        >= InfuserScreen.this.menu.getEnchantmentPowerLimit()) {
                    return ChatFormatting.YELLOW;
                } else if (isPowerTooLow) {
                    return ChatFormatting.RED;
                } else {
                    return ChatFormatting.WHITE;
                }
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
                return false;
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
                // NO-OP
            }
        });
        this.enchantButton = this.addRenderableWidget(new InfuserEnchantButton(this.leftPos + BUTTONS_OFFSET_X,
                this.topPos + (this.menu.getConfig().allowRepairing.isActive() ? ENCHANT_BUTTON_OFFSET_Y :
                        ENCHANT_ONLY_BUTTON_OFFSET_Y),
                (Button button) -> {
                    if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.ENCHANT_BUTTON)) {
                        this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0);
                    }
                    this.searchBox.setValue("");
                }));
        if (this.menu.getConfig().allowRepairing.isActive()) {
            this.repairButton = this.addRenderableWidget(new InfuserRepairButton(this.leftPos + BUTTONS_OFFSET_X,
                    this.topPos + REPAIR_BUTTON_OFFSET_Y,
                    (Button button) -> {
                        if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.REPAIR_BUTTON)) {
                            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
                        }
                    }));
        } else {
            this.repairButton = null;
        }
        this.refreshButton(InfuserMenu.ENCHANTMENT_POWER_DATA_SLOT);
        this.refreshButton(InfuserMenu.ENCHANTING_COST_DATA_SLOT);
        this.refreshButton(InfuserMenu.REPAIR_COST_DATA_SLOT);
    }

    private void refreshButton(int dataSlot) {
        switch (dataSlot) {
            case InfuserMenu.ENCHANTMENT_POWER_DATA_SLOT ->
                    this.refreshEnchantingPower(this.menu.getEnchantmentPower());
            case InfuserMenu.ENCHANTING_COST_DATA_SLOT -> {
                this.refreshButton(this.enchantButton,
                        this.menu.getEnchantingCost(),
                        this.menu.canEnchant(this.minecraft.player));
            }
            case InfuserMenu.REPAIR_COST_DATA_SLOT -> {
                if (this.repairButton != null) {
                    this.refreshButton(this.repairButton,
                            this.menu.getRepairCost(),
                            this.menu.canRepair(this.minecraft.player));
                }
            }
        }
    }

    private void refreshButton(InfuserMenuButton button, int value, boolean mayApply) {
        button.refreshMessage(value, mayApply);
        button.refreshTooltip(this.menu.getEnchantableStack(), this.menu.getItemEnchantments(), value, mayApply);
        button.active = mayApply;
    }

    private void refreshEnchantingPower(int enchantmentPower) {
        this.powerWidget.setMessage(Component.literal(String.valueOf(enchantmentPower)));
        int enchantmentPowerLimit = this.menu.getEnchantmentPowerLimit();
        TooltipBuilder builder = TooltipBuilder.create()
                .splitLines(200)
                .addLines(Component.translatable(EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER,
                        enchantmentPower,
                        enchantmentPowerLimit).withStyle(ChatFormatting.YELLOW));
        if (enchantmentPower < enchantmentPowerLimit) {
            builder.addLines(Component.translatable(InfuserMenuButton.KEY_TOOLTIP_HINT).withStyle(ChatFormatting.GRAY));
        }
        builder.build(this.powerWidget);
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
    public boolean charTyped(CharacterEvent characterEvent) {
        if (this.ignoreTextInput) {
            return false;
        } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.charTyped(characterEvent)) {
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
    public boolean keyPressed(KeyEvent keyEvent) {
        this.ignoreTextInput = false;
        if (!this.searchBox.isFocused()) {
            if (this.minecraft.options.keyChat.matches(keyEvent)) {
                this.ignoreTextInput = true;
                this.searchBox.setFocused(true);
                return true;
            } else {
                return super.keyPressed(keyEvent);
            }
        } else {
            boolean flag = this.hoveredSlot != null && this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(keyEvent).getNumericKeyValue().isPresent();
            if (flag && flag1 && this.checkHotbarKeyPressed(keyEvent)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String s = this.searchBox.getValue();
                if (this.searchBox.keyPressed(keyEvent)) {
                    if (!Objects.equals(s, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }
                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && !keyEvent.isEscape()
                            || super.keyPressed(keyEvent);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        this.ignoreTextInput = false;
        return super.keyReleased(keyEvent);
    }

    public void refreshSearchResults() {
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
            return enchantment.value().description().getString().toLowerCase(Locale.ROOT).contains(s);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        isPowerTooLow = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                INFUSER_LOCATION,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        // render slot manually and do not include it as part of the background texture file,
        // so it can be placed further down when repairing is disabled
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                INFUSER_LOCATION,
                this.leftPos + 8 - 1,
                this.topPos + (this.menu.getConfig().allowRepairing.isActive() ? 23 : 34) - 1,
                196,
                185,
                18,
                18,
                256,
                256);
    }

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

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (dataSlotIndex == InfuserMenu.ENCHANT_ITEM_SLOT) {
            this.refreshButton(InfuserMenu.ENCHANTING_COST_DATA_SLOT);
            this.refreshButton(InfuserMenu.REPAIR_COST_DATA_SLOT);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        this.refreshButton(dataSlotIndex);
    }

    private class EnchantmentSelectionList extends AbstractMenuSelectionList<EnchantmentSelectionList.Entry> {

        public EnchantmentSelectionList(int x, int y) {
            super(InfuserScreen.this.minecraft, x, y, 160, 70, 18, 8);
        }

        public void addEntry(Holder<Enchantment> enchantment, EnchantmentComponent enchantmentComponent) {
            this.addEntry(new Entry(enchantment, enchantmentComponent));
        }

        @Override
        public void clearEntries() {
            super.clearEntries();
        }

        class Entry extends AbstractMenuSelectionList.Entry<Entry> {
            private final EnchantmentComponent enchantmentComponent;
            private final Component component;
            private final List<FormattedCharSequence> tooltip;
            private final AbstractWidget removeButton;
            private final AbstractWidget addButton;

            public Entry(Holder<Enchantment> enchantment, EnchantmentComponent enchantmentComponent) {
                this.enchantmentComponent = enchantmentComponent;
                this.component = enchantmentComponent.getDisplayName(enchantment,
                        EnchantmentSelectionList.this.getWidth() - 18 * 2,
                        EnchantmentSelectionList.this.minecraft.font,
                        InfuserScreen.this.enchantmentSeed);
                this.tooltip = ClientComponentSplitter.splitTooltipLines(enchantmentComponent.getTooltip(enchantment))
                        .toList();
                this.removeButton = new EnchantingOperationButton.Remove(enchantmentComponent,
                        EnchantmentSelectionList.this.getX(),
                        EnchantmentSelectionList.this.getY(),
                        (Button button) -> {
                            if (InfuserScreen.this.getMenu()
                                    .clickClientEnchantmentLevelButton(enchantment,
                                            enchantmentComponent.enchantmentLevel(),
                                            ServerboundEnchantmentLevelMessage.Operation.remove())) {
                                InfuserScreen.this.refreshSearchResults();
                            }
                        });
                this.addButton = new EnchantingOperationButton.Add(enchantmentComponent,
                        EnchantmentSelectionList.this.getX() + EnchantmentSelectionList.this.getWidth() - 18,
                        EnchantmentSelectionList.this.getY(),
                        (Button button) -> {
                            if (InfuserScreen.this.getMenu()
                                    .clickClientEnchantmentLevelButton(enchantment,
                                            enchantmentComponent.enchantmentLevel(),
                                            ServerboundEnchantmentLevelMessage.Operation.add())) {
                                InfuserScreen.this.refreshSearchResults();
                            }
                        });
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                        InfuserScreen.INFUSER_LOCATION,
                        this.getContentX(),
                        this.getContentY(),
                        0,
                        185 + this.getYImage() * EnchantmentSelectionList.this.defaultEntryHeight,
                        this.getContentWidth(),
                        this.getContentHeight(),
                        256,
                        256);
                AbstractWidget.renderScrollingString(guiGraphics,
                        EnchantmentSelectionList.this.minecraft.font,
                        this.component,
                        this.getContentX() + 18 + 2,
                        this.getContentY(),
                        this.getContentRight() - 18 - 2,
                        this.getContentBottom(),
                        ARGB.opaque(this.getFontColor()));
                for (AbstractWidget abstractWidget : this.children()) {
                    abstractWidget.setY(this.getContentY());
                    abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick);
                }

                if (hovering && (this.enchantmentComponent.isInactive()
                        || mouseX >= this.getContentX() + 18 && mouseX < this.getContentRight() - 18)) {
                    guiGraphics.setTooltipForNextFrame(this.tooltip, mouseX, mouseY);
                    if (this.enchantmentComponent.isNotAvailable()) {
                        InfuserScreen.setIsPowerTooLow(true);
                    }
                }
            }

            private int getYImage() {
                return this.enchantmentComponent.isInactive() ? 0 : this.enchantmentComponent.isPresent() ? 2 : 1;
            }

            private int getFontColor() {
                return this.enchantmentComponent.isInactive() ? 0x685E4A :
                        this.enchantmentComponent.isPresent() ? ChatFormatting.YELLOW.getColor() : -1;
            }

            @Override
            public List<? extends AbstractWidget> children() {
                return List.of(this.removeButton, this.addButton);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.removeButton, this.addButton);
            }
        }
    }
}
