package fuzs.enchantinginfuser.util;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantmentUtil {

    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, boolean allowTreasure, boolean allowUndiscoverable, boolean allowCurse) {
        List<Enchantment> list = Lists.newArrayList();
        boolean isBook = stack.getItem() instanceof BookItem;
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if (enchantment.canApplyAtEnchantingTable(stack) || (isBook && enchantment.isAllowedOnBooks())) {
                if (!enchantment.isDiscoverable()) {
                    if (!allowUndiscoverable) continue;
                } else if (enchantment.isCurse()) {
                    if (!allowCurse) continue;
                } else if (enchantment.isTreasureOnly()) {
                    if (!allowTreasure) continue;
                }
                list.add(enchantment);
            }
        }
        return list;
    }

    public static Map<Enchantment, Integer> copyEnchantmentsToMap(ItemStack stack, List<Enchantment> enchantments) {
        final Map<Enchantment, Integer> enchantmentsToLevel = enchantments.stream()
                .collect(Collectors.toMap(Function.identity(), enchantment -> 0));
        if (stack.isEnchanted()) {
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
                if (enchantmentsToLevel.containsKey(entry.getKey())) {
                    enchantmentsToLevel.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return enchantmentsToLevel;
    }

    public static ItemStack setNewEnchantments(ItemStack oldStack, Map<Enchantment, Integer> newEnchantments) {
        // copied from grindstone
        ItemStack newStack = oldStack.copy();
        newStack.removeTagKey("Enchantments");
        newStack.removeTagKey("StoredEnchantments");
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(oldStack).entrySet()) {
            if (!newEnchantments.containsKey(entry.getKey())) {
                newEnchantments.put(entry.getKey(), entry.getValue());
            }
        }
        Map<Enchantment, Integer> enchantmentsToLevel = newEnchantments.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        EnchantmentHelper.setEnchantments(enchantmentsToLevel, newStack);
        newStack.setRepairCost(0);
        if (newStack.is(Items.ENCHANTED_BOOK) && enchantmentsToLevel.size() == 0) {
            newStack = new ItemStack(Items.BOOK);
            if (oldStack.hasCustomHoverName()) {
                newStack.setHoverName(oldStack.getHoverName());
            }
        }
        for(int i = 0; i < enchantmentsToLevel.size(); ++i) {
            newStack.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(newStack.getBaseRepairCost()));
        }
        return newStack;
    }

    public static MutableComponent getPlainEnchantmentName(Enchantment enchantment, int level, boolean withLevel) {
        // copied from Enchantment, but without curses being colored red
        MutableComponent mutablecomponent = new TranslatableComponent(enchantment.getDescriptionId());
        if (withLevel && (level != 1 || enchantment.getMaxLevel() != 1)) {
            mutablecomponent.append(" ").append(new TranslatableComponent("enchantment.level." + level));
        }
        return mutablecomponent;
    }
}
