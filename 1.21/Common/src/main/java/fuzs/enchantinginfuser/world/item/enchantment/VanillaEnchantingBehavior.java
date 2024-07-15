package fuzs.enchantinginfuser.world.item.enchantment;

import fuzs.enchantinginfuser.api.v2.EnchantingBehavior;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaEnchantingBehavior implements EnchantingBehavior {
    public static final EnchantingBehavior INSTANCE = new VanillaEnchantingBehavior();

    @Override
    public String[] getScalingNamespaces() {
        return new String[]{"minecraft"};
    }

    @Override
    public float getProvidedPower(BlockState blockState, Level level, BlockPos blockPos) {
        return CommonAbstractions.INSTANCE.getEnchantPowerBonus(blockState, level, blockPos);
    }
}
