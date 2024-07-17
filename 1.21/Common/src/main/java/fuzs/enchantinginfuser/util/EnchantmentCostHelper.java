package fuzs.enchantinginfuser.util;

import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentCostHelper {

    public static Fraction getEnchantmentCosts(ItemEnchantments itemEnchantments) {
        Fraction enchantmentCosts = Fraction.ZERO;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            int enchantmentLevel = entry.getIntValue();
            if (entry.getKey().is(EnchantmentTags.TREASURE)) {
                enchantmentLevel *= 2;
            }
            Fraction enchantmentCost = getEnchantmentCost(entry.getKey(), enchantmentLevel);
            enchantmentCosts = enchantmentCosts.add(enchantmentCost);
        }

        return enchantmentCosts;
    }

    public static Fraction getScalingEnchantmentCosts(Collection<Holder<Enchantment>> itemEnchantments, Collection<String> scalingNamespaces) {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        Fraction scalingEnchantmentCosts = Fraction.ZERO;
        for (Holder<Enchantment> enchantment : filterEnchantmentsForItem(itemEnchantments)) {
            if (scalingNamespaces.isEmpty() || scalingNamespaces.contains(
                    enchantment.unwrapKey().orElseThrow().location().getNamespace())) {
                Fraction enchantmentCost = getEnchantmentCost(enchantment);
                scalingEnchantmentCosts = scalingEnchantmentCosts.add(enchantmentCost);
            }
        }

        return scalingEnchantmentCosts;
    }

    public static Collection<Holder<Enchantment>> filterEnchantmentsForItem(Collection<Holder<Enchantment>> itemEnchantments) {
        Map<Holder<Enchantment>, Fraction> itemEnchantmentCosts = new HashMap<>();
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            Fraction newEnchantmentCost = getEnchantmentCost(enchantment);
            Holder<Enchantment> incompatibleEnchantment = findIncompatibleEnchantment(enchantment,
                    itemEnchantmentCosts.keySet()
            );
            if (incompatibleEnchantment != null) {
                Fraction oldEnchantmentCost = itemEnchantmentCosts.remove(incompatibleEnchantment);
                if (newEnchantmentCost.compareTo(oldEnchantmentCost) > 0) {
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

    private static Fraction getEnchantmentCost(Holder<Enchantment> enchantment) {
        return getEnchantmentCost(enchantment, enchantment.value().getMaxLevel());
    }

    private static Fraction getEnchantmentCost(Holder<Enchantment> enchantment, int enchantmentLevel) {
        return Fraction.getFraction(enchantmentLevel, enchantment.value().getWeight());
    }
}
