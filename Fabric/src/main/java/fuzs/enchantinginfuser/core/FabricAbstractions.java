package fuzs.enchantinginfuser.core;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FabricAbstractions implements CommonAbstractions {

    @Override
    public float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.is(ConventionalBlockTags.BOOKSHELVES) ? 1.0F : 0.0F;
    }

    @Override
    public boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean isAllowedOnBooks(Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
        return slot == Mob.getEquipmentSlotForItem(stack);
    }
}
