package fuzs.enchantinginfuser.core;

import fuzs.puzzleslib.util.PuzzlesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = PuzzlesUtil.loadServiceProvider(CommonAbstractions.class);

    float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos);

    boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack);

    boolean isAllowedOnBooks(Enchantment enchantment);

    boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity);
}
