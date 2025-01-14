package fuzs.enchantinginfuser.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;

/**
 * Allows for accessing enchantment properties.
 * <p>
 * Serves as an abstraction layer for older Minecraft versions and mods such as Apotheosis that fundamentally change how
 * the enchantment system is implemented.
 */
public interface EnchantmentAdapter {

    /**
     * @param enchantment the enchantment
     * @return rarity of this enchantment, heavily used in Enchanting Infuser for determining cost and availability
     */
    int getWeight(Holder<Enchantment> enchantment);

    /**
     * @param first  the enchantment
     * @param second other enchantment to check compatiblity with
     * @return are both enchantments compatible with each other
     */
    boolean areCompatible(Holder<Enchantment> first, Holder<Enchantment> second);

    /**
     * @param enchantments   the enchantments
     * @param newEnchantment the new enchantment to test for compatibility
     * @return are all enchantments compatible with the new enchantment
     */
    default boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> enchantments, Holder<Enchantment> newEnchantment) {
        return enchantments.stream().allMatch(enchantment -> this.areCompatible(enchantment, newEnchantment));
    }

    /**
     * @param enchantment the enchantment
     * @return min level the enchantment is allowed to have
     */
    int getMinLevel(Holder<Enchantment> enchantment);

    /**
     * @param enchantment the enchantment
     * @return max level the enchantment is allowed to have
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

    /**
     * @return the current {@link EnchantmentAdapter}
     */
    static EnchantmentAdapter get() {
        return VanillaEnchantmentAdapter.INSTANCE;
    }
}
