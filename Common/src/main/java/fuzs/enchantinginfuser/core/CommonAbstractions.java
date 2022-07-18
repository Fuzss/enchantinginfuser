package fuzs.enchantinginfuser.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CommonAbstractions {

    float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos);

    boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack);

    boolean isAllowedOnBooks(Enchantment enchantment);

    boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity);
}
