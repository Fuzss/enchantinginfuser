package fuzs.enchantinginfuser.config;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

public enum AllowedRepairItems {
    EVERYTHING,
    TOOLS_AND_ARMOR,
    NOTHING;

    public boolean isAllowedToRepair(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamaged()) return false;
        if (this == TOOLS_AND_ARMOR) {
            return itemStack.getItem() instanceof TieredItem || itemStack.getItem() instanceof ArmorItem;
        }
        return this == EVERYTHING;
    }

    public boolean isActive() {
        return this != NOTHING;
    }
}
