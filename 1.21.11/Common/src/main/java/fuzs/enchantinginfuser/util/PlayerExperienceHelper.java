package fuzs.enchantinginfuser.util;

import com.google.common.collect.Sets;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class PlayerExperienceHelper {

    public static int calculateExperienceDelta(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments, RandomSource random) {
        int experiencePoints = collectExperiencePoints(itemEnchantments, originalEnchantments);
        if (experiencePoints > 0) {
            experiencePoints = Mth.ceil(experiencePoints / 2.0);
            return experiencePoints + random.nextInt(experiencePoints);
        } else {
            return 0;
        }
    }

    private static int collectExperiencePoints(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments) {
        int experiencePoints = 0;
        for (Holder<Enchantment> enchantment : Sets.union(itemEnchantments.keySet(), originalEnchantments.keySet())) {
            int originalLevel = originalEnchantments.getLevel(enchantment);
            int currentLevel = itemEnchantments.getLevel(enchantment);
            if (originalLevel > currentLevel) {
                int originalMinCost = EnchantingBehavior.get().getMinCost(enchantment, originalLevel);
                int currentMinCost = EnchantingBehavior.get().getMinCost(enchantment, currentLevel);
                experiencePoints += Math.max(0, originalMinCost) - Math.max(0, currentMinCost);
            }
        }

        return experiencePoints;
    }
}
