package fuzs.enchantinginfuser.util;

import com.google.common.collect.Lists;
import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.core.ModServices;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantmentUtil {

    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, boolean allowAnvil, boolean allowTreasure, boolean allowUndiscoverable, boolean allowUntradeable, boolean allowCurse) {
        List<Enchantment> list = Lists.newArrayList();
        boolean book = stack.getItem() instanceof BookItem || stack.getItem() instanceof EnchantedBookItem;
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if ((allowAnvil ? enchantment.canEnchant(stack) : ModServices.ABSTRACTIONS.canApplyAtEnchantingTable(enchantment, stack)) || (book && ModServices.ABSTRACTIONS.isAllowedOnBooks(enchantment))) {
                if (!EnchantingInfuserAPI.getEnchantStatsProvider().isDiscoverable(enchantment)) {
                    if (!allowUndiscoverable) continue;
                } else if (!EnchantingInfuserAPI.getEnchantStatsProvider().isTradeable(enchantment)) {
                    if (!allowUntradeable) continue;
                } else if (EnchantingInfuserAPI.getEnchantStatsProvider().isCurse(enchantment)) {
                    if (!allowCurse) continue;
                } else if (EnchantingInfuserAPI.getEnchantStatsProvider().isTreasureOnly(enchantment)) {
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
        if (stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem) {
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
                // make sure to not allow editing curses / treasure enchantments
                if (enchantmentsToLevel.containsKey(entry.getKey())) {
                    enchantmentsToLevel.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return enchantmentsToLevel;
    }

    /**
     * mostly copied from grindstone
     */
    public static ItemStack setNewEnchantments(ItemStack stack, Map<Enchantment, Integer> newEnchantments, boolean increaseRepairCost) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (!newEnchantments.containsKey(entry.getKey())) {
                newEnchantments.put(entry.getKey(), entry.getValue());
            }
        }
        Map<Enchantment, Integer> enchantmentsToLevel = newEnchantments.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ItemStack newStack = getNewEnchantmentStack(stack, !enchantmentsToLevel.isEmpty(), true);
        newStack.removeTagKey("Enchantments");
        newStack.removeTagKey("StoredEnchantments");
        EnchantmentHelper.setEnchantments(enchantmentsToLevel, newStack);
        if (increaseRepairCost) {
            newStack.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(stack.getBaseRepairCost()));
        }
        return newStack;
    }

    public static ItemStack getNewEnchantmentStack(ItemStack stack, boolean enchanted, boolean copyResult) {
        ItemStack newStack;
        if (stack.getItem() instanceof EnchantedBookItem && !enchanted) {
            newStack = new ItemStack(Items.BOOK);
        } else if (stack.getItem() instanceof BookItem && enchanted) {
            newStack = new ItemStack(Items.ENCHANTED_BOOK);
        } else {
            return copyResult ? stack.copy() : stack;
        }
        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null) {
            newStack.setTag(compoundtag.copy());
        }
        return newStack;
    }

    /**
     * set <code>level</code> to -1 to skip adding
     */
    public static MutableComponent getPlainEnchantmentName(Enchantment enchantment, int level) {
        // copied from Enchantment, but without curses being colored red
        MutableComponent mutablecomponent = Component.translatable(enchantment.getDescriptionId());
        if (level != -1 && (level != 1 || EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment) != 1)) {
            mutablecomponent.append(" ").append(Component.translatable("enchantment.level." + level));
        }
        return mutablecomponent;
    }
}
