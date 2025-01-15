package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.ClientComponentSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantmentSelectionList extends AbstractCustomSelectionList<EnchantmentSelectionList.Entry> {
    private final InfuserScreen screen;

    public EnchantmentSelectionList(InfuserScreen screen, int x, int y) {
        super(screen.minecraft, x, y, 160, 70, 18, 8);
        this.screen = screen;
    }

    public void addEntry(Holder<Enchantment> enchantment, EnchantmentComponent enchantmentComponent) {
        this.addEntry(new Entry(enchantment, enchantmentComponent, this.getX(), this.getY()));
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final EnchantmentComponent enchantmentComponent;
        private final Component component;
        private final List<FormattedCharSequence> tooltip;
        private final AbstractWidget removeButton;
        private final AbstractWidget addButton;

        public Entry(Holder<Enchantment> enchantment, EnchantmentComponent enchantmentComponent, int x, int y) {
            this.enchantmentComponent = enchantmentComponent;
            this.component = enchantmentComponent.getDisplayName(enchantment,
                    124,
                    EnchantmentSelectionList.this.minecraft.font,
                    EnchantmentSelectionList.this.screen.enchantmentSeed);
            this.tooltip = ClientComponentSplitter.splitTooltipLines(enchantmentComponent.getTooltip(enchantment))
                    .toList();
            this.removeButton = new EnchantingOperationButton.Remove(enchantmentComponent, x, y, (Button button) -> {
                if (EnchantmentSelectionList.this.screen.getMenu()
                        .clickClientEnchantmentLevelButton(enchantment,
                                enchantmentComponent.enchantmentLevel(),
                                ServerboundEnchantmentLevelMessage.Operation.remove())) {
                    EnchantmentSelectionList.this.screen.refreshSearchResults();
                }
            });
            this.addButton = new EnchantingOperationButton.Add(enchantmentComponent, x + 142, y, (Button button) -> {
                if (EnchantmentSelectionList.this.screen.getMenu()
                        .clickClientEnchantmentLevelButton(enchantment,
                                enchantmentComponent.enchantmentLevel(),
                                ServerboundEnchantmentLevelMessage.Operation.add())) {
                    EnchantmentSelectionList.this.screen.refreshSearchResults();
                }
            });
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.blit(InfuserScreen.INFUSER_LOCATION, left, top, 0, 185 + this.getYImage() * 18, width, height);
            guiGraphics.drawCenteredString(EnchantmentSelectionList.this.minecraft.font,
                    this.component,
                    left + width / 2,
                    top + 5,
                    this.enchantmentComponent.isInactive() ? 0X685E4A : -1);
            for (AbstractWidget abstractWidget : this.children()) {
                abstractWidget.setY(top);
                abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            if (hovering &&
                    (this.enchantmentComponent.isInactive() || mouseX >= left + 18 && mouseX < left + 18 + 124)) {
                EnchantmentSelectionList.this.screen.setTooltipForNextRenderPass(this.tooltip);
                if (this.enchantmentComponent.isNotAvailable()) {
                    InfuserScreen.setIsPowerTooLow(true);
                }
            }
        }

        private int getYImage() {
            return this.enchantmentComponent.isIncompatible() || this.enchantmentComponent.isNotAvailable() ? 0 :
                    this.enchantmentComponent.isPresent() ? 2 : 1;
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
