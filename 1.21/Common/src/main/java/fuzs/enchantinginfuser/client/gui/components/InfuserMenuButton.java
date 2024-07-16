package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.ClientComponentSplitter;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.TooltipComponentImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class InfuserMenuButton extends SpritelessImageButton {
    public static final String KEY_TOOLTIP_EXPERIENCE = "gui.enchantinginfuser.tooltip.points";
    public static final String KEY_TOOLTIP_CHANGE = "gui.enchantinginfuser.tooltip.change";
    public static final String KEY_TOOLTIP_DURABILITY = "gui.enchantinginfuser.tooltip.durability";
    public static final String KEY_TOOLTIP_HINT = "gui.enchantinginfuser.tooltip.enchanting_power.hint";

    final InfuserScreen screen;

    public InfuserMenuButton(InfuserScreen screen, int x, int y, int xTexStart, int yTexStart, OnPress onPress) {
        super(x, y, 18, 18, xTexStart, yTexStart, InfuserScreen.INFUSER_LOCATION, onPress);
        this.screen = screen;
        this.setTextureLayout(SpritelessImageButton.LEGACY_TEXTURE_LAYOUT);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (this.mayApply() && this.getValue() != 0) {
            drawStringWithBackground(this.screen.font, guiGraphics, this.getX() + 1, this.getY() + 1,
                    this.getStringValue(), this.getStringColor().getColor()
            );
        }
    }

    abstract int getValue();

    abstract boolean mayApply();

    abstract ChatFormatting getStringColor();

    abstract String getStringValue();

    public void refreshTooltip(ItemStack itemStack) {
        new TooltipComponentImpl(this, this.getTooltipLines(itemStack)) {

            @Override
            public List<FormattedCharSequence> processTooltipLines(List<? extends FormattedText> tooltipLines) {
                return ClientComponentSplitter.processTooltipLines(tooltipLines).toList();
            }
        };
    }

    private List<FormattedText> getTooltipLines(ItemStack itemStack) {
        List<FormattedText> lines = new ArrayList<>();
        if (this.mayApply()) {
            lines.add(this.getNameComponent(itemStack));
            lines.addAll(this.getCustomLines(itemStack));
        }
        Component levelsComponent = this.getLevelsComponent();
        if (levelsComponent != null) {
            if (!lines.isEmpty()) {
                lines.add(CommonComponents.EMPTY);
            }
            lines.add(levelsComponent);
        }
        return lines;
    }

    Component getNameComponent(ItemStack itemStack) {
        MutableComponent component = Component.empty()
                .append(itemStack.getHoverName())
                .withStyle(this.getItemNameRarity(itemStack).color());
        if (itemStack.has(DataComponents.CUSTOM_NAME)) {
            component.withStyle(ChatFormatting.ITALIC);
        }
        return component;
    }

    Rarity getItemNameRarity(ItemStack itemStack) {
        return itemStack.getRarity();
    }

    abstract List<FormattedText> getCustomLines(ItemStack itemStack);

    @Nullable
    Component getLevelsComponent() {
        int value = this.getValue();
        if (this.mayApply()) {
            if (value != 0) {
                if (value == 1) {
                    return Component.translatable("container.enchant.level.one").withStyle(ChatFormatting.GRAY);
                } else {
                    return Component.translatable("container.enchant.level.many", value).withStyle(ChatFormatting.GRAY);
                }
            } else {
                return null;
            }
        } else {
            return Component.translatable("container.enchant.level.requirement", value).withStyle(ChatFormatting.RED);
        }
    }

    static void drawStringWithBackground(Font font, GuiGraphics guiGraphics, int posX, int posY, String text, int textColor) {
        drawStringWithBackground(font, guiGraphics, posX, posY, text, textColor, 0);
    }

    static void drawStringWithBackground(Font font, GuiGraphics guiGraphics, int posX, int posY, String text, int textColor, int backgroundColor) {
        posX += 19 - 2 - font.width(text);
        posY += 6 + 3;
        // render shadow on every side to avoid readability issues with colorful background
        guiGraphics.drawString(font, text, posX - 1, posY, backgroundColor, false);
        guiGraphics.drawString(font, text, posX + 1, posY, backgroundColor, false);
        guiGraphics.drawString(font, text, posX, posY - 1, backgroundColor, false);
        guiGraphics.drawString(font, text, posX, posY + 1, backgroundColor, false);
        guiGraphics.drawString(font, text, posX, posY, textColor, false);
    }
}
