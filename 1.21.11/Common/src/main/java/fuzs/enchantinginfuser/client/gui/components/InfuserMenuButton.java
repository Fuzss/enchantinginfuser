package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InfuserMenuButton extends ImageButton {
    public static final String KEY_TOOLTIP_EXPERIENCE = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("infusing.tooltip.points"));
    public static final String KEY_TOOLTIP_CHANGE = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("infusing.tooltip.change"));
    public static final String KEY_TOOLTIP_DURABILITY = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("infusing.tooltip.durability"));

    private Component backdropMessage = CommonComponents.EMPTY;

    public InfuserMenuButton(int x, int y, WidgetSprites widgetSprites, OnPress onPress) {
        super(x, y, 18, 18, widgetSprites, onPress);
    }

    @Override
    public void setMessage(Component message) {
        this.message = this.inactiveMessage = message;
        this.backdropMessage = message.copy().withStyle(ChatFormatting.BLACK);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabel(guiGraphics);
    }

    protected void renderLabel(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int posX = this.getX() + this.getWidth() - font.width(this.getMessage());
        int posY = this.getY() + this.getHeight() - font.lineHeight + 1;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    guiGraphics.drawString(font, this.backdropMessage, posX + i, posY + j, -1, false);
                }
            }
        }

        guiGraphics.drawString(font, this.getMessage(), posX, posY, -1, false);
    }

    public void refreshMessage(int value, boolean mayApply) {
        if (value != 0) {
            ChatFormatting chatFormatting = this.getStringColor(value, mayApply);
            Component component = Component.literal(this.getStringValue(value)).withStyle(chatFormatting);
            this.setMessage(component);
        } else {
            this.setMessage(CommonComponents.EMPTY);
        }
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
