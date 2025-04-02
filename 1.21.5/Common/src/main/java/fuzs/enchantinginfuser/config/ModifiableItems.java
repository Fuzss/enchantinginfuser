package fuzs.enchantinginfuser.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public enum ModifiableItems {
    UNENCHANTED(ItemStack::isEnchantable),
    ALL((ItemStack itemStack) -> UNENCHANTED.predicate.test(itemStack) ||
            itemStack.has(DataComponents.ENCHANTABLE) && itemStack.isEnchanted()),
    FULL_DURABILITY((ItemStack itemStack) -> !itemStack.isDamaged() && ALL.predicate.test(itemStack));

    public final Predicate<ItemStack> predicate;

    ModifiableItems(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }
}
