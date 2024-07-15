package fuzs.enchantinginfuser.api.v2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * provider class for enchantment data created for mods such as Apotheosis that fundamentally change the enchantment
 * system and replace all vanilla calls for enchantment properties set your own provider in
 * {@link EnchantmentProviders#setEnchantStatsProvider}
 */
public interface EnchantingBehavior {

    /**
     * Enchanting Infuser scales enchantment level costs to account for various kinds of gear being able to receive a
     * varying number of enchantments, so that in the end enchanting all items with all their allowed enchantments will
     * roughly end up at the same cost to prevent enchantment collection mods therefore making individual enchantment
     * costs ludicrously cheap (due to the number of enchantments they add) only certain namespaces are accounted for
     * when scaling costs
     *
     * @return namespaces to account for when scaling costs
     */
    String[] getScalingNamespaces();

    /**
     * this is the same as the config option, when the returned value is not -1 the config option will be overridden
     *
     * @return the maximum enchanting power required for performing high level enchantments
     */
    default int getMaximumEnchantPower() {
        return -1;
    }

    /**
     * @param blockState the block state to get enchanting power from
     * @param level      leve reader instance
     * @param blockPos   position of the block
     * @return power bonus, 1.0 for bookshelves, otherwise 0.0
     */
    float getProvidedPower(BlockState blockState, Level level, BlockPos blockPos);

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
    default float getMaximumEnchantPowerScale(BlockState blockState, Level level, BlockPos blockPos) {
        return Math.signum(this.getProvidedPower(blockState, level, blockPos));
    }

    /**
     * maximum cost determines how many levels you'll have to pay for fully enchanting an item with all possible
     * enchantments it can have
     *
     * @return general multiplier for maximum cost
     */
    default float getMaximumCostMultiplier() {
        return 1.0F;
    }
}
