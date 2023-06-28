package fuzs.enchantinginfuser.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ForgeAbstractions implements CommonAbstractions {

    @Override
    public boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack) {
        return enchantment.canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean isAllowedOnBooks(Enchantment enchantment) {
        return enchantment.isAllowedOnBooks();
    }

}
