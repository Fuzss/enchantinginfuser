package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.api.item.v2.ToolTypeHelper;
import net.minecraft.world.item.ItemStack;

public enum AllowedRepairItems {
    EVERYTHING,
    TOOLS_AND_ARMOR,
    NOTHING;

    public boolean canRepair(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamaged()) {
            return false;
        } else if (this == TOOLS_AND_ARMOR) {
            return ToolTypeHelper.INSTANCE.isTool(itemStack) || ToolTypeHelper.INSTANCE.isArmor(itemStack);
        } else {
            return this == EVERYTHING;
        }
    }

    public boolean isActive() {
        return this != NOTHING;
    }
}
