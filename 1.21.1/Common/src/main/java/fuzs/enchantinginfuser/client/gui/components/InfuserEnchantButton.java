package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InfuserEnchantButton extends InfuserMenuButton {

    public InfuserEnchantButton(InfuserScreen screen, int x, int y, OnPress onPress) {
        super(screen, x, y, 160, 185, onPress);
    }

    @Override
    int getValue() {
        return this.screen.getMenu().getEnchantingCost();
    }

    @Override
    boolean mayApply() {
        return this.screen.getMenu().canEnchant(this.screen.minecraft.player);
    }

    @Override
    ChatFormatting getStringColor() {
        return this.getValue() < 0 ? ChatFormatting.YELLOW :
                (this.mayApply() ? ChatFormatting.GREEN : ChatFormatting.RED);
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
        HolderLookup.Provider registries = this.screen.minecraft.getConnection().registryAccess();
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

    private void getEnchantmentLines(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments, List<FormattedText> newLines, List<FormattedText> changedLines, List<FormattedText> unchangedLines, List<FormattedText> removedLines) {
        HolderLookup.Provider registries = this.screen.minecraft.getConnection().registryAccess();
        HolderSet<Enchantment> holders = getTagOrEmpty(registries, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
        for (Holder<Enchantment> enchantment : holders) {
            int oldLevel = originalEnchantments.getLevel(enchantment);
            int newLevel = itemEnchantments.getLevel(enchantment);
            if (newLevel > 0 && oldLevel == 0) {
                // the enchantment was added
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, newLevel);
                newLines.add(component.withStyle(ChatFormatting.GREEN));
            } else if (newLevel == 0 && oldLevel > 0) {
                // the enchantment was removed
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, oldLevel);
                removedLines.add(component.withStyle(ChatFormatting.RED));
            } else if (newLevel > 0 && newLevel != oldLevel) {
                // the enchantment level changed
                MutableComponent component = EnchantmentTooltipHelper.getDisplayName(enchantment);
                MutableComponent changeComponent = Component.translatable(InfuserMenuButton.KEY_TOOLTIP_CHANGE,
                        Component.translatable("enchantment.level." + oldLevel),
                        Component.translatable("enchantment.level." + newLevel)
                );
                changedLines.add(component.append(CommonComponents.SPACE)
                        .append(changeComponent)
                        .withStyle(ChatFormatting.YELLOW));
            } else if (newLevel > 0) {
                // the enchantment has not changed
                MutableComponent component = EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, newLevel);
                unchangedLines.add(component.withStyle(ChatFormatting.GRAY));
            }
        }
    }

    static <T> HolderSet<T> getTagOrEmpty(HolderLookup.Provider registries, ResourceKey<Registry<T>> registryKey, TagKey<T> key) {
        Optional<HolderSet.Named<T>> optional = registries.lookupOrThrow(registryKey).get(key);
        return optional.map((HolderSet.Named<T> holders) -> (HolderSet<T>) holders).orElseGet(HolderSet::direct);
    }

    @Override
    @Nullable
    Component getLevelsComponent() {
        if (this.mayApply() && this.getValue() < 0) {
            return Component.translatable(InfuserMenuButton.KEY_TOOLTIP_EXPERIENCE).withStyle(ChatFormatting.GRAY);
        } else {
            return super.getLevelsComponent();
        }
    }
}
