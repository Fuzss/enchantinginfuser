package fuzs.enchantinginfuser.world.item.enchantment;

import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Collections;

public final class VanillaEnchantingBehavior implements EnchantingBehavior {
    public static final EnchantingBehavior INSTANCE = new VanillaEnchantingBehavior();
    static EnchantingBehavior enchantingBehavior = INSTANCE;

    private VanillaEnchantingBehavior() {
        // NO-OP
    }

    @Override
    public Collection<String> getScalingNamespaces() {
        return Collections.singleton("minecraft");
    }

    @Override
    public int getEnchantmentPowerLimit(InfuserType infuserType) {
        return infuserType.getConfig().maximumBookshelves;
    }

    @Override
    public float getEnchantmentPower(BlockState blockState, Level level, BlockPos blockPos) {
        return CommonAbstractions.INSTANCE.getEnchantPowerBonus(blockState, level, blockPos);
    }

    @Override
    public float getEnchantmentPowerLimitScale(BlockState blockState, Level level, BlockPos blockPos) {
        return Math.signum(this.getEnchantmentPower(blockState, level, blockPos));
    }

    @Override
    public float getMaximumCostMultiplier() {
        return 1.0F;
    }

    @Override
    public int getMaxLevel(Holder<Enchantment> enchantment) {
        return enchantment.value().getMaxLevel();
    }

    @Override
    public int getMinCost(Holder<Enchantment> enchantment, int level) {
        return enchantment.value().getMinCost(level);
    }

    @Override
    public int getMaxCost(Holder<Enchantment> enchantment, int level) {
        return enchantment.value().getMaxCost(level);
    }
}
