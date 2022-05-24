package fuzs.enchantinginfuser.api.world.item.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

/**
 * provider class for enchantment data
 * created for mods such as Apotheosis that fundamentally change the enchantment system and replace all vanilla calls for enchantment properties
 * set your own provider in {@link fuzs.enchantinginfuser.api.EnchantingInfuserAPI#setEnchantStatsProvider}
 */
public interface EnchantStatsProvider {

    /**
     * @return namespace of source mod for identifying provider
     */
    String getSourceNamespace();

    /**
     * defines a priority for this provider, mainly used to prevent mods registering a provider early from being overridden by the default provider
     * when two providers have the same priority the one that has been registered first will be used
     * @return the priority
     */
    default int getPriority() {
        return 10;
    }

    /**
     * @param enchantment the enchantment
     * @return rarity of this enchantment, heavily used in Enchanting Infuser for determining cost and availability
     */
    Enchantment.Rarity getRarity(Enchantment enchantment);

    /**
     * @param enchantment the enchantment
     * @param other other enchantment to check compatiblity with
     * @return are <code>enchantment</code> and <code>other</code> compatible with each other
     */
    boolean isCompatibleWith(Enchantment enchantment, Enchantment other);

    /**
     * @param enchantment the enchantment
     * @return min level <code>enchantment</code> is allowed to have (not really used much)
     */
    int getMinLevel(Enchantment enchantment);

    /**
     * @param enchantment the enchantment
     * @return max level <code>enchantment</code> is allowed to have
     */
    int getMaxLevel(Enchantment enchantment);

    /**
     * min cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     * @param enchantment the enchantment
     * @param level the current enchantment level
     * @return min cost
     */
    int getMinCost(Enchantment enchantment, int level);

    /**
     * max cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     * @param enchantment the enchantment
     * @param level the current enchantment level
     * @return max cost
     */
    int getMaxCost(Enchantment enchantment, int level);

    /**
     * @param enchantment the enchantment
     * @return is <code>enchantment</code> a treasure enchantment (not obtainable via the enchanting table)
     */
    boolean isTreasureOnly(Enchantment enchantment);

    /**
     * @param enchantment the enchantment
     * @return is <code>enchantment</code> a curse enchantment (aka an enchantment with bad effects)
     */
    boolean isCurse(Enchantment enchantment);

    /**
     * @param enchantment the enchantment
     * @return is <code>enchantment</code> obtainable via villager trading
     */
    boolean isTradeable(Enchantment enchantment);

    /**
     * @param enchantment the enchantment
     * @return can <code>enchantment</code> be found on randomly enchanted gear in any loot chest
     */
    boolean isDiscoverable(Enchantment enchantment);
}
