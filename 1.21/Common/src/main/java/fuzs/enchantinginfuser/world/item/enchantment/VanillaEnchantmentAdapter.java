package fuzs.enchantinginfuser.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

public class VanillaEnchantmentAdapter implements EnchantmentAdapter {
    public static final EnchantmentAdapter INSTANCE = new VanillaEnchantmentAdapter();

    @Override
    public int getWeight(Holder<Enchantment> enchantment) {
        return enchantment.value().getWeight();
    }

    @Override
    public boolean areCompatible(Holder<Enchantment> first, Holder<Enchantment> second) {
        return Enchantment.areCompatible(first, second);
    }

    @Override
    public int getMinLevel(Holder<Enchantment> enchantment) {
        return enchantment.value().getMinLevel();
    }

    @Override
    public int getMaxLevel(Holder<Enchantment> enchantment) {
        return enchantment.value().getMaxLevel();
    }

    @Override
    public int getMinCost(Holder<Enchantment> enchantment, int enchantmentLevel) {
        return enchantment.value().getMinCost(enchantmentLevel);
    }

    @Override
    public int getMaxCost(Holder<Enchantment> enchantment, int enchantmentLevel) {
        return enchantment.value().getMaxCost(enchantmentLevel);
    }

    @Override
    public boolean isTreasure(Holder<Enchantment> enchantment) {
        return enchantment.is(EnchantmentTags.TRADEABLE);
    }

    @Override
    public boolean isCurse(Holder<Enchantment> enchantment) {
        return enchantment.is(EnchantmentTags.CURSE);
    }
}
