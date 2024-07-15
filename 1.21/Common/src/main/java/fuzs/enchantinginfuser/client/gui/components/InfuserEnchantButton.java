package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfuserEnchantButton extends InfuserMenuButton {

    public InfuserEnchantButton(InfuserScreen screen, int x, int y, OnPress onPress) {
        super(screen, x, y, 126, 185, onPress);
    }

    @Override
    int getValue() {
        return this.screen.getMenu().getEnchantCost();
    }

    @Override
    boolean canApply() {
        return this.screen.getMenu().canEnchant(this.screen.minecraft.player);
    }

    @Override
    ChatFormatting getStringColor() {
        return this.getValue() < 0 ? ChatFormatting.YELLOW :
                (this.canApply() ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @Override
    String getStringValue() {
        return this.getValue() < 0 ? "+" : String.valueOf(this.getValue());
    }

    @Override
    Component getNameComponent(ItemStack itemStack) {
        boolean isEnchanted = !this.screen.getMenu().getItemEnchantments().isEmpty();
        itemStack = ModEnchantmentHelper.getEnchantedItemStack(itemStack, isEnchanted);
        return super.getNameComponent(itemStack);
    }

    @Override
    Rarity getItemNameRarity(ItemStack itemStack) {
        boolean isEnchanted = !this.screen.getMenu().getItemEnchantments().isEmpty();
        itemStack = ModEnchantmentHelper.getEnchantedItemStack(itemStack, isEnchanted);
        RegistryAccess.Frozen registries = Minecraft.getInstance().getConnection().registryAccess();
        return ModEnchantmentHelper.getItemNameRarity(registries, itemStack, isEnchanted);
    }

    @Override
    List<FormattedText> getCustomLines(ItemStack itemStack) {
        ItemEnchantments itemEnchantments = this.screen.getMenu().getItemEnchantments();
        ItemEnchantments originalEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        List<FormattedText> newLines = new ArrayList<>();
        List<FormattedText> changedLines = new ArrayList<>();
        List<FormattedText> unchangedLines = new ArrayList<>();
        List<FormattedText> removedLines = new ArrayList<>();
        this.getEnchantmentLines(itemEnchantments, originalEnchantments, newLines, changedLines, unchangedLines,
                removedLines
        );
        List<FormattedText> lines = new ArrayList<>();
        lines.addAll(newLines);
        lines.addAll(changedLines);
        lines.addAll(unchangedLines);
        lines.addAll(removedLines);
        return lines;
    }

    private void getEnchantmentLines(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments,
                                     List<FormattedText> newLines, List<FormattedText> changedLines,
                                     List<FormattedText> unchangedLines, List<FormattedText> removedLines) {
        Set<Holder<Enchantment>> allEnchantments = new HashSet<>(itemEnchantments.keySet());
        allEnchantments.addAll(originalEnchantments.keySet());
        for (Holder<Enchantment> enchantment : allEnchantments) {
            int oldLevel =
                    originalEnchantments.keySet().contains(enchantment) ? originalEnchantments.getLevel(enchantment) :
                            -1;
            int newLevel =
                    itemEnchantments.keySet().contains(enchantment) ? itemEnchantments.getLevel(enchantment) : -1;
            if (newLevel > 0 && oldLevel <= 0) {
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, newLevel);
                newLines.add(component.withStyle(ChatFormatting.GREEN));
            } else if (newLevel == 0 && oldLevel > 0) {
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, oldLevel);
                removedLines.add(component.withStyle(ChatFormatting.RED));
            } else if (newLevel > 0 && oldLevel > 0 && newLevel != oldLevel) {
                // -1 prevents level from being added, so we can do it ourselves
                MutableComponent component = EnchantmentTooltipHelper.getDisplayName(enchantment);
                MutableComponent changeComponent = Component.translatable(InfuserMenuButton.KEY_TOOLTIP_CHANGE,
                        Component.translatable("enchantment.level." + oldLevel),
                        Component.translatable("enchantment.level." + newLevel)
                );
                changedLines.add(component.append(CommonComponents.SPACE)
                        .append(changeComponent)
                        .withStyle(ChatFormatting.YELLOW));
            } else if (newLevel > 0 || oldLevel > 0) {
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment,
                        newLevel > 0 ? newLevel : oldLevel
                );
                unchangedLines.add(component.withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    @Nullable
    Component getLevelsComponent() {
        if (this.canApply() && this.getValue() < 0) {
            return Component.translatable(InfuserMenuButton.KEY_TOOLTIP_EXPERIENCE).withStyle(ChatFormatting.GRAY);
        } else {
            return super.getLevelsComponent();
        }
    }
}
