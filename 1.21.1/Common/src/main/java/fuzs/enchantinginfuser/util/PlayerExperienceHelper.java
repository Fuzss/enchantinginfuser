package fuzs.enchantinginfuser.util;

import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class PlayerExperienceHelper {

    public static int calculateExperienceDelta(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments, RandomSource random) {
        int experiencePoints = collectExperiencePoints(itemEnchantments, originalEnchantments);
        if (experiencePoints > 0) {
            experiencePoints = (int) Math.ceil(experiencePoints / 2.0);
            return experiencePoints + random.nextInt(experiencePoints);
        } else {
            return 0;
        }
    }

    private static int collectExperiencePoints(ItemEnchantments itemEnchantments, ItemEnchantments originalEnchantments) {
        int experiencePoints = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int originalLevel = originalEnchantments.getLevel(enchantment);
            int currentLevel = entry.getIntValue();
            if (originalLevel > currentLevel) {
                int originalMinCost = EnchantmentAdapter.get().getMinCost(enchantment, originalLevel);
                int currentMinCost = EnchantmentAdapter.get().getMinCost(enchantment, currentLevel);
                experiencePoints += Math.max(0, originalMinCost) - Math.max(0, currentMinCost);
            }
        }

        return experiencePoints;
    }
}
