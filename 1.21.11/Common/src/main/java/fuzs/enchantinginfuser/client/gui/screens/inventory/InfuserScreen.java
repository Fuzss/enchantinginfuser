package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.components.*;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class InfuserScreen extends AbstractWidgetsContainerScreen<InfuserMenu> implements ContainerListener {
    public static final Identifier TEXTURE_LOCATION = EnchantingInfuser.id("textures/gui/container/infuser.png");
    public static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(EnchantingInfuser.id("container/infuser/button"),
            EnchantingInfuser.id("container/infuser/button_disabled"),
            EnchantingInfuser.id("container/infuser/button_highlighted"));
    public static final WidgetSprites ENCHANT_BUTTON_SPRITES = new WidgetSprites(EnchantingInfuser.id(
            "container/infuser/enchant_button"),
            EnchantingInfuser.id("container/infuser/enchant_button_disabled"),
            EnchantingInfuser.id("container/infuser/enchant_button_highlighted"));
    public static final WidgetSprites REPAIR_BUTTON_SPRITES = new WidgetSprites(EnchantingInfuser.id(
            "container/infuser/repair_button"),
            EnchantingInfuser.id("container/infuser/repair_button_disabled"),
            EnchantingInfuser.id("container/infuser/repair_button_highlighted"));
    public static final WidgetSprites REMOVE_BUTTON_SPRITES = new WidgetSprites(EnchantingInfuser.id(
            "container/infuser/remove_button"),
            EnchantingInfuser.id("container/infuser/remove_button_disabled"),
            EnchantingInfuser.id("container/infuser/remove_button_highlighted"));
    public static final WidgetSprites ADD_BUTTON_SPRITES = new WidgetSprites(EnchantingInfuser.id(
            "container/infuser/add_button"),
            EnchantingInfuser.id("container/infuser/add_button_disabled"),
            EnchantingInfuser.id("container/infuser/add_button_highlighted"));
    public static final String KEY_TOOLTIP_HINT = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("infusing.tooltip.enchanting_power_hint"));
    public static final int SQUARE_BUTTON_SIZE = 18;
    private static final int BUTTONS_OFFSET_X = 7;
    private static final int ENCHANT_BUTTON_OFFSET_Y = 44;
    private static final int ENCHANT_ONLY_BUTTON_OFFSET_Y = 55;
    private static final int REPAIR_BUTTON_OFFSET_Y = 66;

    private static boolean isPowerTooLow;
    private final int enchantmentSeed = new Random().nextInt();
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
        this.getMenu().addSlotListener(this);
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
        this.searchBox.setInvertHighlightedTextColor(false);
        this.addRenderableWidget(this.searchBox);
        this.scrollingList = new EnchantmentSelectionList(this.leftPos + 30, this.topPos + 18);
        this.addRenderableWidget(this.scrollingList);
        this.powerWidget = this.addRenderableOnly(new ItemStackDisplayWidget(this.leftPos + 196,
                this.topPos + 161,
                this.font,
                new ItemStack(Items.BOOKSHELF)) {
            private Component maximumEnchantmentPowerMessage = CommonComponents.EMPTY;
            private Component powerIsTooLowMessage = CommonComponents.EMPTY;

            @Override
            public Component getMessage() {
                if (InfuserScreen.this.getMenu().getEnchantmentPower() >= InfuserScreen.this.getMenu()
                        .getEnchantmentPowerLimit()) {
                    return this.maximumEnchantmentPowerMessage;
                } else if (isPowerTooLow) {
                    return this.powerIsTooLowMessage;
                } else {
                    return super.getMessage();
                }
            }

            @Override
            public void setMessage(Component message) {
                super.setMessage(message);
                this.maximumEnchantmentPowerMessage = ComponentUtils.mergeStyles(message,
                        Style.EMPTY.withColor(ChatFormatting.YELLOW));
                this.powerIsTooLowMessage = ComponentUtils.mergeStyles(message,
                        Style.EMPTY.withColor(ChatFormatting.RED));
            }
        });
        this.enchantButton = this.addRenderableWidget(new InfuserEnchantButton(this.leftPos + BUTTONS_OFFSET_X,
                this.topPos + (this.getMenu().getConfig().allowRepairing.isActive() ? ENCHANT_BUTTON_OFFSET_Y :
                        ENCHANT_ONLY_BUTTON_OFFSET_Y),
                (Button button) -> {
                    if (this.getMenu().clickMenuButton(this.minecraft.player, InfuserMenu.ENCHANT_BUTTON)) {
                        this.minecraft.gameMode.handleInventoryButtonClick((this.getMenu()).containerId, 0);
                    }
                    this.searchBox.setValue("");
                }));
        if (this.getMenu().getConfig().allowRepairing.isActive()) {
            this.repairButton = this.addRenderableWidget(new InfuserRepairButton(this.leftPos + BUTTONS_OFFSET_X,
                    this.topPos + REPAIR_BUTTON_OFFSET_Y,
                    (Button button) -> {
                        if (this.getMenu().clickMenuButton(this.minecraft.player, InfuserMenu.REPAIR_BUTTON)) {
                            this.minecraft.gameMode.handleInventoryButtonClick((this.getMenu()).containerId, 1);
                        }
                    }));
        } else {
            this.repairButton = null;
        }

        this.refreshAllButtons();
    }

    private void refreshAllButtons() {
        this.refreshButton(InfuserMenu.ENCHANTMENT_POWER_DATA_SLOT);
        this.refreshButton(InfuserMenu.ENCHANTING_COST_DATA_SLOT);
        this.refreshButton(InfuserMenu.REPAIR_COST_DATA_SLOT);
    }

    private void refreshButton(int dataSlot) {
        switch (dataSlot) {
            case InfuserMenu.ENCHANTMENT_POWER_DATA_SLOT ->
                    this.refreshEnchantingPower(this.getMenu().getEnchantmentPower());
            case InfuserMenu.ENCHANTING_COST_DATA_SLOT -> {
                this.refreshButton(this.enchantButton,
                        this.getMenu().getEnchantingCost(),
                        this.getMenu().canEnchant(this.minecraft.player));
            }
            case InfuserMenu.REPAIR_COST_DATA_SLOT -> {
                if (this.repairButton != null) {
                    this.refreshButton(this.repairButton,
                            this.getMenu().getRepairCost(),
                            this.getMenu().canRepair(this.minecraft.player));
                }
            }
        }
    }

    private void refreshButton(InfuserMenuButton button, int value, boolean mayApply) {
        button.refreshMessage(value, mayApply);
        button.refreshTooltip(this.getMenu().getEnchantableStack(),
                this.getMenu().getItemEnchantments(),
                value,
                mayApply);
        button.active = mayApply;
    }

    private void refreshEnchantingPower(int enchantmentPower) {
        this.powerWidget.setMessage(Component.literal(String.valueOf(enchantmentPower)));
        int enchantmentPowerLimit = this.getMenu().getEnchantmentPowerLimit();
        TooltipBuilder builder = TooltipBuilder.create()
                .splitLines(200)
                .addLines(Component.translatable(EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER,
                        enchantmentPower,
                        enchantmentPowerLimit).withStyle(ChatFormatting.GREEN));
        if (enchantmentPower < enchantmentPowerLimit) {
            builder.addLines(Component.translatable(KEY_TOOLTIP_HINT).withStyle(ChatFormatting.GRAY));
        }

        builder.build(this.powerWidget);
    }

    @Override
    public void removed() {
        super.removed();
        this.getMenu().removeSlotListener(this);
    }

    @Override
    public void resize(int width, int height) {
        String string = this.searchBox.getValue();
        super.resize(width, height);
        this.searchBox.setValue(string);
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
        ItemEnchantments itemEnchantments = this.getMenu().getItemEnchantments();
        Set<Holder<Enchantment>> allEnchantments = this.getMenu().getAllEnchantments();
        HolderLookup.Provider registries = this.minecraft.getConnection().registryAccess();
        HolderSet<Enchantment> sortedEnchantments = registries.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(EnchantmentTags.TOOLTIP_ORDER);
        for (Holder<Enchantment> enchantment : sortedEnchantments) {
            if (allEnchantments.contains(enchantment) && this.matchesSearch(enchantment)) {
                InfuserMenu.EnchantmentValues enchantmentValues = this.getMenu().getEnchantmentValues(enchantment);
                EnchantmentLevelEntry enchantmentLevelEntry = EnchantmentLevelEntry.create(enchantment,
                        enchantmentValues,
                        itemEnchantments);
                this.scrollingList.addEntry(enchantment, enchantmentLevelEntry);
            }
        }

        for (Holder<Enchantment> enchantment : allEnchantments) {
            if (!sortedEnchantments.contains(enchantment) && this.matchesSearch(enchantment)) {
                InfuserMenu.EnchantmentValues enchantmentValues = this.getMenu().getEnchantmentValues(enchantment);
                EnchantmentLevelEntry enchantmentLevelEntry = EnchantmentLevelEntry.create(enchantment,
                        enchantmentValues,
                        itemEnchantments);
                this.scrollingList.addEntry(enchantment, enchantmentLevelEntry);
            }
        }

        if (size != this.scrollingList.children().size()) {
            this.scrollingList.setScrollAmount(0.0);
        }
    }

    private boolean matchesSearch(Holder<Enchantment> holder) {
        String searchQuery = this.searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        if (searchQuery.isEmpty()) {
            return true;
        } else if (this.getMenu().getAvailableEnchantmentLevel(holder) == 0) {
            return false;
        } else {
            return holder.value().description().getString().toLowerCase(Locale.ROOT).contains(searchQuery);
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
                TEXTURE_LOCATION,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
        Slot slot = this.getMenu().getSlot(0);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                SLOT_SPRITE,
                this.leftPos + slot.x - 1,
                this.topPos + slot.y - 1,
                SQUARE_BUTTON_SIZE,
                SQUARE_BUTTON_SIZE);
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
            super(InfuserScreen.this.minecraft, x, y, 160, 70, SQUARE_BUTTON_SIZE, 8);
        }

        public void addEntry(Holder<Enchantment> holder, LevelBasedEntry<Enchantment> levelBasedEntry) {
            this.addEntry(new Entry(holder, levelBasedEntry));
        }

        @Override
        public void clearEntries() {
            super.clearEntries();
        }

        class Entry extends AbstractMenuSelectionList.Entry<Entry> {
            private final Identifier backgroundSprite;
            private final boolean isPowerTooLow;

            public Entry(Holder<Enchantment> holder, LevelBasedEntry<Enchantment> levelBasedEntry) {
                this.backgroundSprite = BUTTON_SPRITES.get(!levelBasedEntry.isInactive(), levelBasedEntry.isPresent());
                this.isPowerTooLow = levelBasedEntry.isNotAvailable();
                Component component = ComponentUtils.mergeStyles(levelBasedEntry.getDisplayName(holder,
                        EnchantmentSelectionList.this.getWidth() - SQUARE_BUTTON_SIZE * 2,
                        InfuserScreen.this.enchantmentSeed), this.getStyle(levelBasedEntry));
                AbstractWidget abstractWidget = this.addRenderableWidget(new ScrollingStringWidget(
                        EnchantmentSelectionList.this.getX() + SQUARE_BUTTON_SIZE,
                        EnchantmentSelectionList.this.getY(),
                        EnchantmentSelectionList.this.getWidth() - SQUARE_BUTTON_SIZE * 2,
                        SQUARE_BUTTON_SIZE,
                        component,
                        InfuserScreen.this.font));
                TooltipBuilder.create(levelBasedEntry.getTooltip(holder)).splitLines().build(abstractWidget);
                this.addRenderableWidget(new LevelBasedOperationButton.Remove(levelBasedEntry,
                        EnchantmentSelectionList.this.getX(),
                        EnchantmentSelectionList.this.getY(),
                        (Button button) -> {
                            if (InfuserScreen.this.getMenu()
                                    .clickClientEnchantmentLevelButton(holder,
                                            levelBasedEntry.level(),
                                            ServerboundEnchantmentLevelMessage.Operation.remove())) {
                                InfuserScreen.this.refreshSearchResults();
                            }
                        }));
                this.addRenderableWidget(new LevelBasedOperationButton.Add(levelBasedEntry,
                        EnchantmentSelectionList.this.getX() + EnchantmentSelectionList.this.getWidth()
                                - SQUARE_BUTTON_SIZE,
                        EnchantmentSelectionList.this.getY(),
                        (Button button) -> {
                            if (InfuserScreen.this.getMenu()
                                    .clickClientEnchantmentLevelButton(holder,
                                            levelBasedEntry.level(),
                                            ServerboundEnchantmentLevelMessage.Operation.add())) {
                                InfuserScreen.this.refreshSearchResults();
                            }
                        }));
            }

            private Style getStyle(LevelBasedEntry<?> levelBasedEntry) {
                if (levelBasedEntry.isInactive()) {
                    return Style.EMPTY.withColor(0x685E4A);
                } else if (levelBasedEntry.isPresent()) {
                    return Style.EMPTY.withColor(ChatFormatting.YELLOW);
                } else {
                    return Style.EMPTY;
                }
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        this.backgroundSprite,
                        this.getContentX(),
                        this.getContentY(),
                        this.getContentWidth(),
                        this.getContentHeight());
                super.renderContent(guiGraphics, mouseX, mouseY, hovering, partialTick);
                if (hovering && this.isPowerTooLow) {
                    InfuserScreen.setIsPowerTooLow(true);
                }
            }
        }
    }
}
