package fuzs.enchantinginfuser.config;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public enum ModifiableItems {
    UNENCHANTED(ItemStack::isEnchantable),
    ALL(ModifiableItems::isItemEnchantableOrEnchanted),
    FULL_DURABILITY((ItemStack itemStack) -> !itemStack.isDamaged() && isItemEnchantableOrEnchanted(itemStack));

    public final Predicate<ItemStack> predicate;

    ModifiableItems(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    private static boolean isItemEnchantableOrEnchanted(ItemStack itemStack) {
        return itemStack.isEnchantable() || itemStack.isEnchanted();
    }
}
