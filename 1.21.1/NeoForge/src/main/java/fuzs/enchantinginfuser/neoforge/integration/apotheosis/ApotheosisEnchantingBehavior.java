package fuzs.enchantinginfuser.neoforge.integration.apotheosis;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Set;

public final class ApotheosisEnchantingBehavior implements EnchantingBehavior {
    public static final EnchantingBehavior INSTANCE = new ApotheosisEnchantingBehavior();

    private ApotheosisEnchantingBehavior() {
        // NO-OP
    }

    @Override
    public Collection<String> getScalingNamespaces() {
        return Set.of("minecraft", "apothic_enchanting");
    }

    @Override
    public int getEnchantmentPowerLimit(InfuserType infuserType) {
        return 50;
    }

    @Override
    public float getEnchantmentPower(BlockState state, Level level, BlockPos pos) {
        return EnchantingStatRegistry.getEterna(state, level, pos);
    }

    @Override
    public float getEnchantmentPowerLimitScale(BlockState state, Level level, BlockPos pos) {
        return EnchantingStatRegistry.getMaxEterna(state, level, pos) / 15.0F;
    }

    @Override
    public float getMaximumCostMultiplier() {
        return 2.5F;
    }

    @Override
    public int getMaxLevel(Holder<Enchantment> enchantment) {
        return ApothicEnchanting.getEnchInfo(enchantment).getMaxLevel();
    }

    @Override
    public int getMinCost(Holder<Enchantment> enchantment, int level) {
        return ApothicEnchanting.getEnchInfo(enchantment).getMinPower(level);
    }

    @Override
    public int getMaxCost(Holder<Enchantment> enchantment, int level) {
        return ApothicEnchanting.getEnchInfo(enchantment).getMaxPower(level);
    }
}
