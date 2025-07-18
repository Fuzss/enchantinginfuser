package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.puzzleslib.api.client.gui.v2.GuiGraphicsHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InfuserMenuButton extends SpritelessImageButton {
    public static final String KEY_TOOLTIP_EXPERIENCE = "gui.enchantinginfuser.tooltip.points";
    public static final String KEY_TOOLTIP_CHANGE = "gui.enchantinginfuser.tooltip.change";
    public static final String KEY_TOOLTIP_DURABILITY = "gui.enchantinginfuser.tooltip.durability";
    public static final String KEY_TOOLTIP_HINT = "gui.enchantinginfuser.tooltip.enchanting_power.hint";

    private int color = -1;

    public InfuserMenuButton(int x, int y, int xTexStart, int yTexStart, OnPress onPress) {
        super(x, y, 18, 18, xTexStart, yTexStart, InfuserScreen.INFUSER_LOCATION, onPress);
        this.setTextureLayout(SpritelessImageButton.LEGACY_TEXTURE_LAYOUT);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.drawStringWithBackground(guiGraphics, this.getX() + 1, this.getY() + 1, this.getMessage(), this.color);
    }

    protected void drawStringWithBackground(GuiGraphics guiGraphics, int posX, int posY, Component component, int color) {
        Font font = Minecraft.getInstance().font;
        GuiGraphicsHelper.drawInBatch8xOutline(guiGraphics,
                font,
                component,
                posX + (19 - 2 - font.width(component)),
                posY + (6 + 3),
                ARGB.opaque(color),
                ARGB.opaque(0));
    }

    public void refreshMessage(int value, boolean mayApply) {
        this.setMessage(value != 0 ? Component.literal(this.getStringValue(value)) : CommonComponents.EMPTY);
        this.color = this.getStringColor(value, mayApply).getColor();
    }

    abstract ChatFormatting getStringColor(int value, boolean mayApply);

    abstract String getStringValue(int value);

    public void refreshTooltip(ItemStack itemStack, ItemEnchantments itemEnchantments, int value, boolean mayApply) {
        List<FormattedText> tooltipLines = this.getTooltipLines(itemStack, itemEnchantments, value, mayApply);
        TooltipBuilder.create().addLines(tooltipLines).build(this);
    }

    private List<FormattedText> getTooltipLines(ItemStack itemStack, ItemEnchantments itemEnchantments, int value, boolean mayApply) {
        if (!mayApply && value == 0) {
            return Collections.emptyList();
        } else {
            List<FormattedText> lines = new ArrayList<>();
            if (mayApply) {
                lines.add(this.getNameComponent(itemStack, itemEnchantments));
                lines.addAll(this.getCustomLines(itemStack, itemEnchantments));
            }
            Component levelsComponent = this.getLevelsComponent(value, mayApply);
            if (levelsComponent != null) {
                if (!lines.isEmpty()) {
                    lines.add(CommonComponents.EMPTY);
                }
                lines.add(levelsComponent);
            }
            return lines;
        }
    }

    Component getNameComponent(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        MutableComponent component = Component.empty()
                .append(itemStack.getHoverName())
                .withStyle(this.getItemNameRarity(itemStack, itemEnchantments).color());
        if (itemStack.has(DataComponents.CUSTOM_NAME)) {
            component.withStyle(ChatFormatting.ITALIC);
        }
        return component;
    }

    Rarity getItemNameRarity(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        return itemStack.getRarity();
    }

    abstract List<FormattedText> getCustomLines(ItemStack itemStack, ItemEnchantments itemEnchantments);

    @Nullable Component getLevelsComponent(int value, boolean mayApply) {
        if (mayApply) {
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
}
