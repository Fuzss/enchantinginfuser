package fuzs.enchantinginfuser.config;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public enum ModifiableItems {
    UNENCHANTED(ItemStack::isEnchantable),
    ALL(itemStack -> UNENCHANTED.predicate.test(itemStack) ||
            itemStack.getItem().isEnchantable(itemStack) && itemStack.isEnchanted()),
    FULL_DURABILITY(itemStack -> !itemStack.isDamaged() && ALL.predicate.test(itemStack));

    public final Predicate<ItemStack> predicate;

    ModifiableItems(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }
}
