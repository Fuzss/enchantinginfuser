package fuzs.enchantinginfuser.world.item.enchantment;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public record VanillaEnchantingBehavior(ServerConfig.InfuserConfig config) implements EnchantingBehavior {

    @Override
    public List<String> getScalingNamespaces() {
        return Collections.singletonList("minecraft");
    }

    @Override
    public int getEnchantmentPowerLimit() {
        return this.getConfig().maximumBookshelves;
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
    public ServerConfig.InfuserConfig getConfig() {
        return this.config;
    }
}
