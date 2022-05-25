package fuzs.enchantinginfuser.util;

import com.google.common.collect.Lists;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.capability.KnownEnchantsCapability;
import fuzs.enchantinginfuser.registry.ModRegistry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantmentUtil {

    public static List<Enchantment> getAvailableEnchantments(Player player, ItemStack stack, boolean allowTreasure, boolean allowUndiscoverable, boolean allowUntradeable, boolean allowCurse) {
        List<Enchantment> list = Lists.newArrayList();
        boolean isBook = stack.getItem() instanceof BookItem;
        for (Enchantment enchantment : getKnownEnchantments(player)) {
            if (enchantment.canApplyAtEnchantingTable(stack) || (isBook && enchantment.isAllowedOnBooks())) {
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

    private static Collection<Enchantment> getKnownEnchantments(Player player) {
        if (!EnchantingInfuser.CONFIG.server().limitedEnchantments) {
            return ForgeRegistries.ENCHANTMENTS.getValues();
        }
        Optional<Collection<Enchantment>> optional = player.getCapability(ModRegistry.KNOWN_ENCHANTS_CAPABILITY).map(KnownEnchantsCapability::getKnownEnchantments);
        return optional.orElseGet(ForgeRegistries.ENCHANTMENTS::getValues);
    }

    public static Map<Enchantment, Integer> copyEnchantmentsToMap(ItemStack stack, List<Enchantment> enchantments) {
        final Map<Enchantment, Integer> enchantmentsToLevel = enchantments.stream()
                .collect(Collectors.toMap(Function.identity(), enchantment -> 0));
        if (stack.isEnchanted()) {
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
                // make sure to not allow editing curses / treasure enchantments
                if (enchantmentsToLevel.containsKey(entry.getKey())) {
                    enchantmentsToLevel.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return enchantmentsToLevel;
    }

    public static ItemStack setNewEnchantments(ItemStack oldStack, Map<Enchantment, Integer> newEnchantments, boolean increaseRepairCost) {
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
        if (increaseRepairCost) {
            newStack.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(oldStack.getBaseRepairCost()));
        }
        if (newStack.is(Items.ENCHANTED_BOOK) && enchantmentsToLevel.size() == 0) {
            newStack = new ItemStack(Items.BOOK);
            if (oldStack.hasCustomHoverName()) {
                newStack.setHoverName(oldStack.getHoverName());
            }
        }
        return newStack;
    }

    public static MutableComponent getPlainEnchantmentName(Enchantment enchantment, int level) {
        // copied from Enchantment, but without curses being colored red
        MutableComponent mutablecomponent = new TranslatableComponent(enchantment.getDescriptionId());
        if (level != -1 && (level != 1 || EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment) != 1)) {
            mutablecomponent.append(" ").append(new TranslatableComponent("enchantment.level." + level));
        }
        return mutablecomponent;
    }
}
