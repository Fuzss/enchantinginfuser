package fuzs.enchantinginfuser.util;

import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class EnchantmentCostHelper {

    public static int getEnchantmentCosts(ItemEnchantments itemEnchantments) {
        int enchantmentCosts = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            int enchantmentLevel = entry.getIntValue();
            if (entry.getKey().is(EnchantmentTags.TREASURE)) {
                enchantmentLevel *= 2;
            }
            enchantmentCosts += getEnchantmentCost(entry.getKey(), enchantmentLevel);
        }

        return enchantmentCosts;
    }

    public static int getScalingEnchantmentCosts(Collection<Holder<Enchantment>> itemEnchantments, Collection<String> scalingNamespaces) {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        int scalingEnchantmentCosts = 0;
        for (Holder<Enchantment> enchantment : filterEnchantmentsForItem(itemEnchantments)) {
            if (scalingNamespaces.isEmpty() || scalingNamespaces.contains(
                    enchantment.unwrapKey().orElseThrow().location().getNamespace())) {
                scalingEnchantmentCosts += getEnchantmentCost(enchantment);
            }
        }

        return scalingEnchantmentCosts;
    }

    public static Collection<Holder<Enchantment>> filterEnchantmentsForItem(Collection<Holder<Enchantment>> itemEnchantments) {
        Object2IntMap<Holder<Enchantment>> itemEnchantmentCosts = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            int newEnchantmentCost = getEnchantmentCost(enchantment);
            Holder<Enchantment> incompatibleEnchantment = findIncompatibleEnchantment(enchantment,
                    itemEnchantmentCosts.keySet()
            );
            if (incompatibleEnchantment != null) {
                int oldEnchantmentCost = itemEnchantmentCosts.removeInt(incompatibleEnchantment);
                if (newEnchantmentCost > oldEnchantmentCost) {
                    itemEnchantmentCosts.put(enchantment, newEnchantmentCost);
                } else {
                    itemEnchantmentCosts.put(incompatibleEnchantment, oldEnchantmentCost);
                }
            } else {
                itemEnchantmentCosts.put(enchantment, newEnchantmentCost);
            }
        }

        return Collections.unmodifiableSet(itemEnchantmentCosts.keySet());
    }

    @Nullable
    private static Holder<Enchantment> findIncompatibleEnchantment(Holder<Enchantment> enchantment, Collection<Holder<Enchantment>> enchantments) {
        for (Holder<Enchantment> holder : enchantments) {
            if (!EnchantmentAdapter.get().areCompatible(enchantment, holder)) {
                return holder;
            }
        }

        return null;
    }

    private static int getEnchantmentCost(Holder<Enchantment> enchantment) {
        return getEnchantmentCost(enchantment, enchantment.value().getMaxLevel());
    }

    private static int getEnchantmentCost(Holder<Enchantment> enchantment, int enchantmentLevel) {
        return enchantment.value().getAnvilCost() * enchantmentLevel;
    }
}