package fuzs.enchantinginfuser.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class FabricAbstractions implements CommonAbstractions {

    @Override
    public boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean isAllowedOnBooks(Enchantment enchantment) {
        return true;
    }
}
