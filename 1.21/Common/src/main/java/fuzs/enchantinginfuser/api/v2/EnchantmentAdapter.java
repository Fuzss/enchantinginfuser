package fuzs.enchantinginfuser.api.v2;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentAdapter {

    /**
     * @param enchantment the enchantment
     * @return rarity of this enchantment, heavily used in Enchanting Infuser for determining cost and availability
     */
    int getWeight(Holder<Enchantment> enchantment);

    /**
     * @param first  the enchantment
     * @param second other enchantment to check compatiblity with
     * @return are <code>enchantment</code> and <code>other</code> compatible with each other
     */
    boolean areCompatible(Holder<Enchantment> first, Holder<Enchantment> second);

    /**
     * @param enchantment the enchantment
     * @return min level <code>enchantment</code> is allowed to have (not really used much)
     */
    int getMinLevel(Holder<Enchantment> enchantment);

    /**
     * @param enchantment the enchantment
     * @return max level <code>enchantment</code> is allowed to have
     */
    int getMaxLevel(Holder<Enchantment> enchantment);

    /**
     * min cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     *
     * @param enchantment      the enchantment
     * @param enchantmentLevel the current enchantment level
     * @return min cost
     */
    int getMinCost(Holder<Enchantment> enchantment, int enchantmentLevel);

    /**
     * max cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     *
     * @param enchantment      the enchantment
     * @param enchantmentLevel the current enchantment level
     * @return max cost
     */
    int getMaxCost(Holder<Enchantment> enchantment, int enchantmentLevel);

    /**
     * @param enchantment the enchantment
     * @return is <code>enchantment</code> a treasure enchantment (not obtainable via the enchanting table)
     */
    boolean isTreasure(Holder<Enchantment> enchantment);

    /**
     * @param enchantment the enchantment
     * @return is <code>enchantment</code> a curse enchantment (aka an enchantment with bad effects)
     */
    boolean isCurse(Holder<Enchantment> enchantment);
}
