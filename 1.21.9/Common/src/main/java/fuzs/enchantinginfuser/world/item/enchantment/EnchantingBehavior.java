package fuzs.enchantinginfuser.world.item.enchantment;

import fuzs.enchantinginfuser.world.level.block.InfuserType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

/**
 * Allows for accessing enchantment properties and behaviors.
 * <p>
 * Serves as an abstraction layer for older Minecraft versions and mods such as Apotheosis that fundamentally change how
 * the enchantment system is implemented.
 */
public interface EnchantingBehavior {

    static EnchantingBehavior get() {
        return VanillaEnchantingBehavior.enchantingBehavior;
    }

    static void set(EnchantingBehavior enchantingBehavior) {
        VanillaEnchantingBehavior.enchantingBehavior = enchantingBehavior;
    }

    /**
     * Enchanting Infuser scales enchantment level costs to account for various kinds of gear being able to receive a
     * varying number of enchantments, so that in the end enchanting all items with all their allowed enchantments will
     * roughly end up at the same cost to prevent enchantment collection mods therefore making individual enchantment
     * costs ludicrously cheap (due to the number of enchantments they add) only certain namespaces are accounted for
     * when scaling costs
     *
     * @return namespaces to account for when scaling costs
     */
    Collection<String> getScalingNamespaces();

    /**
     * this is the same as the config option, when the returned value is not -1 the config option will be overridden
     *
     * @return the maximum enchanting power required for performing high level enchantments
     */
    int getEnchantmentPowerLimit(InfuserType infuserType);

    /**
     * @param blockState the block state to get enchanting power from
     * @param level      leve reader instance
     * @param blockPos   position of the block
     * @return power bonus, 1.0 for bookshelves, otherwise 0.0
     */
    float getEnchantmentPower(BlockState blockState, Level level, BlockPos blockPos);

    /**
     * provides the maximum enchanting power scale for a block, meaning how much beyond the normal enchanting power this
     * block can provide example: normal bookshelves return 1.0, meaning they can provide 15 enchanting power (one
     * each), any more beyond 15 will do nothing, even when enchanting power can go higher most bookshelves from
     * Apotheosis have a higher value here, so it is valid to have more of them
     *
     * @param blockState the block state to get enchanting power from
     * @param level      level reader instance
     * @param blockPos   position of the block
     * @return power bonus, 1.0 for bookshelves, otherwise 0.0
     */
    float getEnchantmentPowerLimitScale(BlockState blockState, Level level, BlockPos blockPos);

    /**
     * maximum cost determines how many levels you'll have to pay for fully enchanting an item with all possible
     * enchantments it can have
     *
     * @return general multiplier for maximum cost
     */
    float getMaximumCostMultiplier();

    /**
     * @param enchantment the enchantment
     * @return max level <code>enchantment</code> is allowed to have
     */
    int getMaxLevel(Holder<Enchantment> enchantment);

    /**
     * min cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     *
     * @param enchantment the enchantment
     * @param level       the current enchantment level
     * @return min cost
     */
    int getMinCost(Holder<Enchantment> enchantment, int level);

    /**
     * max cost variable required for this enchantment combination to be able to apply to an item at an enchanting table
     * does not directly translate to experience levels / points
     *
     * @param enchantment the enchantment
     * @param level       the current enchantment level
     * @return max cost
     */
    int getMaxCost(Holder<Enchantment> enchantment, int level);
}
