package fuzs.enchantinginfuser.util;

import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class EnchantmentCostHelper {

    public static float getEnchantmentCosts(ItemEnchantments itemEnchantments, float scale) {
        float enchantmentCosts = 0.0F;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            int enchantmentLevel = entry.getIntValue();
            if (entry.getKey().is(EnchantmentTags.TREASURE)) {
                enchantmentLevel *= 2;
            }
            enchantmentCosts += Math.max(enchantmentLevel,
                    getEnchantmentCost(entry.getKey(), enchantmentLevel) * scale);
        }

        return enchantmentCosts;
    }

    public static int getScalingEnchantmentCosts(Collection<Holder<Enchantment>> itemEnchantments, Collection<String> scalingNamespaces) {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        int scalingEnchantmentCosts = 0;
        for (Holder<Enchantment> enchantment : getMostExpensiveEnchantments(itemEnchantments)) {
            if (scalingNamespaces.isEmpty() || scalingNamespaces.contains(enchantment.unwrapKey()
                    .orElseThrow()
                    .identifier()
                    .getNamespace())) {
                scalingEnchantmentCosts += getMaxEnchantmentCost(enchantment);
            }
        }

        return scalingEnchantmentCosts;
    }

    private static Collection<Holder<Enchantment>> getMostExpensiveEnchantments(Collection<Holder<Enchantment>> itemEnchantments) {
        Object2IntMap<Holder<Enchantment>> itemEnchantmentCosts = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            int newEnchantmentCost = getMaxEnchantmentCost(enchantment);
            Holder<Enchantment> incompatibleEnchantment = findIncompatibleEnchantment(enchantment,
                    itemEnchantmentCosts.keySet());
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

        return itemEnchantmentCosts.keySet();
    }

    /**
     * Same as {@link EnchantmentHelper#isEnchantmentCompatible(Collection, Holder)}, but returns the incompatible
     * enchantment if found.
     */
    @Nullable
    private static Holder<Enchantment> findIncompatibleEnchantment(Holder<Enchantment> enchantment, Collection<Holder<Enchantment>> enchantments) {
        for (Holder<Enchantment> holder : enchantments) {
            if (!Enchantment.areCompatible(enchantment, holder)) {
                return holder;
            }
        }

        return null;
    }

    private static int getMaxEnchantmentCost(Holder<Enchantment> enchantment) {
        return getEnchantmentCost(enchantment, EnchantingBehavior.get().getMaxLevel(enchantment));
    }

    private static int getEnchantmentCost(Holder<Enchantment> enchantment, int enchantmentLevel) {
        return enchantment.value().getAnvilCost() * enchantmentLevel;
    }
}
