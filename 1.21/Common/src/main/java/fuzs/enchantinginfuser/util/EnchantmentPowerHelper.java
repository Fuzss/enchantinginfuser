package fuzs.enchantinginfuser.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;

public class EnchantmentPowerHelper {

    public static Object2IntMap<Holder<Enchantment>> getMaximumEnchantmentLevels(int enchantingPower, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        Object2IntMap<Holder<Enchantment>> maximumEnchantmentLevels = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> holder : itemEnchantments) {
            for (int enchantmentLevel = holder.value().getMaxLevel(); enchantmentLevel >= 0; enchantmentLevel--) {
                if (enchantmentLevel == 0) {
                    maximumEnchantmentLevels.put(holder, enchantmentLevel);
                } else {
                    int powerForLevel = getScaledPowerForLevel(holder, enchantmentLevel, itemEnchantments, powerLimit,
                            enchantmentValue
                    );
                    if (powerForLevel <= enchantingPower) {
                        maximumEnchantmentLevels.put(holder, enchantmentLevel);
                        break;
                    }
                }
            }
        }

        return Object2IntMaps.unmodifiable(maximumEnchantmentLevels);
    }

    /**
     * Gets the absolute amount of required enchanting power providers (=bookshelves) to be able to support the
     * enchantment at that enchantment level.
     *
     * @param enchantment      the enchantment
     * @param enchantmentLevel the enchantment levels
     * @param itemEnchantments the enchantment pool for a specific item
     * @param powerLimit       maximum amount of enchanting power providers (=bookshelves) for the current
     *                         configuration
     * @param enchantmentValue item enchantment value from {@link Item#getEnchantmentValue()}
     * @return the absolute amount of required enchanting power providers (=bookshelves)
     */
    public static int getScaledPowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        float relativePowerForLevel = getRelativePowerForLevel(enchantment, enchantmentLevel, itemEnchantments);
        float reducedPowerLimit = Math.max(0.0F, powerLimit - enchantmentValue / 60.0F * powerLimit);
        int scaledPowerForLevel = Math.round(relativePowerForLevel * reducedPowerLimit);
        if (enchantment.is(EnchantmentTags.CURSE)) {
            scaledPowerForLevel *= 3;
        } else if (enchantment.is(EnchantmentTags.TREASURE)) {
            scaledPowerForLevel *= 2;
        }
        return Mth.clamp(scaledPowerForLevel, 0, powerLimit);
    }

    /**
     * Returns a value from <code>0.0</code> to <code>1.0</code> determining the amount of enchanting power providers
     * (=bookshelves) required to be able to support the enchantment at that enchantment level.
     *
     * @param enchantment      the enchantment
     * @param enchantmentLevel the enchantment levels
     * @param itemEnchantments the enchantment pool for a specific item
     * @return the relative enchanting power
     */
    private static float getRelativePowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel, Collection<Holder<Enchantment>> itemEnchantments) {
        int minPower = getMinPower(itemEnchantments);
        int maxPower = getMaxPower(itemEnchantments);
        int powerForLevel = getPowerForLevel(enchantment, enchantmentLevel);
        return (powerForLevel - minPower) / (float) (maxPower - minPower);
    }

    /**
     * Returns the average result of {@link #getPowerForLevel(Holder, int)} for the minimum enchantment level from
     * multiple enchantments, weighted towards the smalled value.
     *
     * @param itemEnchantments the enchantment pool for a specific item
     * @return the weighted minimum enchanting power
     */
    private static int getMinPower(Collection<Holder<Enchantment>> itemEnchantments) {
        if (itemEnchantments.isEmpty()) {
            return 0;
        } else {
            int averageMinStrength = itemEnchantments.stream().mapToInt((Holder<Enchantment> holder) -> {
                return getPowerForLevel(holder, holder.value().getMinLevel());
            }).sum() / itemEnchantments.size();
            int absoluteMinStrength = itemEnchantments.stream().mapToInt((Holder<Enchantment> holder) -> {
                return getPowerForLevel(holder, holder.value().getMinLevel());
            }).min().orElse(0);
            return (averageMinStrength + absoluteMinStrength) / 2;
        }
    }

    /**
     * Returns the average result of {@link #getPowerForLevel(Holder, int)} for the maximum enchantment level from
     * multiple enchantments, weighted towards the largest value.
     *
     * @param itemEnchantments the enchantment pool for a specific item
     * @return the weighted maximum enchanting power
     */
    private static int getMaxPower(Collection<Holder<Enchantment>> itemEnchantments) {
        if (itemEnchantments.isEmpty()) {
            return 0;
        } else {
            int averageMaxStrength = itemEnchantments.stream().mapToInt((Holder<Enchantment> holder) -> {
                return getPowerForLevel(holder, holder.value().getMaxLevel());
            }).sum() / itemEnchantments.size();
            int absoluteMaxStrength = itemEnchantments.stream().mapToInt((Holder<Enchantment> holder) -> {
                return getPowerForLevel(holder, holder.value().getMaxLevel());
            }).max().orElse(0);
            return (averageMaxStrength + absoluteMaxStrength) / 2;
        }
    }

    /**
     * Get the cost for an enchantment at a given enchantment level. Vanilla values usually range from <code>1</code> to
     * about <code>100</code>.
     * <p>
     * We use this value for determining the amount of enchanting power providers (=bookshelves) required to be able to
     * support the enchantment at that level.
     * <p>
     * The implementation determines a value based on vanilla's minimum &amp; maximum costs, apply a weighted average
     * depending on the level.
     */
    private static int getPowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel) {
        int minCost = enchantment.value().getMinCost(enchantmentLevel);
        int maxCost = enchantment.value().getMaxCost(enchantmentLevel);
        return minCost + (maxCost - minCost) * enchantmentLevel / (enchantment.value().getMaxLevel() + 1);
    }
}
