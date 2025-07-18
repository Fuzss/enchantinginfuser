package fuzs.enchantinginfuser.util;

import fuzs.puzzleslib.api.item.v2.EnchantingHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Collection;

public class ModEnchantmentHelper {

    public static Collection<Holder<Enchantment>> getEnchantmentsForItem(RegistryAccess registryAccess, ItemStack itemStack, TagKey<Enchantment> availableEnchantments, boolean primaryOnly) {
        Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        boolean isBook = isBook(itemStack);
        return enchantments.get(availableEnchantments)
                .stream()
                .flatMap(HolderSet::stream)
                .filter((Holder<Enchantment> holder) -> {
                    if (isBook) {
                        return true;
                    } else if (primaryOnly) {
                        return EnchantingHelper.canApplyAtEnchantingTable(holder, itemStack);
                    } else {
                        return holder.value().canEnchant(itemStack);
                    }
                })
                .toList();
    }

    public static boolean isBook(ItemStack itemStack) {
        return itemStack.is(Items.BOOK) || itemStack.is(Items.ENCHANTED_BOOK);
    }

    public static ItemEnchantments computeItemEnchantments(ItemStack itemStack, Collection<Holder<Enchantment>> enchantments) {
        ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments) {
            mutableEnchantments.set(holder, itemEnchantments.getLevel(holder));
        }
        return mutableEnchantments.toImmutable();
    }

    public static ItemStack setNewEnchantments(ItemStack itemStack, Object2IntMap<Holder<Enchantment>> newEnchantments, boolean increaseRepairCost) {
        ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(itemEnchantments);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : newEnchantments.object2IntEntrySet()) {
            mutableEnchantments.set(entry.getKey(), entry.getIntValue());
        }
        ItemEnchantments newItemEnchantments = mutableEnchantments.toImmutable();
        ItemStack newItemStack = getEnchantedItemStack(itemStack, !newItemEnchantments.isEmpty());
        EnchantmentHelper.setEnchantments(newItemStack, newItemEnchantments);
        if (increaseRepairCost) {
            newItemStack.set(DataComponents.REPAIR_COST,
                    AnvilMenu.calculateIncreasedRepairCost(itemStack.getOrDefault(DataComponents.REPAIR_COST, 0)));
        }

        return newItemStack;
    }

    public static ItemStack getEnchantedItemStack(ItemStack itemStack, boolean isEnchanted) {
        if (itemStack.is(Items.ENCHANTED_BOOK) && !isEnchanted) {
            return itemStack.transmuteCopy(Items.BOOK);
        } else if (itemStack.is(Items.BOOK) && isEnchanted) {
            return itemStack.transmuteCopy(Items.ENCHANTED_BOOK);
        } else {
            return itemStack.copy();
        }
    }

    public static Rarity getItemNameRarity(HolderLookup.Provider registries, ItemStack itemStack, boolean isEnchanted) {
        if (!isEnchanted) {
            return itemStack.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
        } else {
            itemStack = new ItemStack(itemStack.getItem());
            ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            // just any entry must be present for the item to count as enchanted
            // alternatively create a custom ItemEnchantments implementation
            itemEnchantments.set(null, 1);
            itemStack.set(DataComponents.ENCHANTMENTS, itemEnchantments.toImmutable());
            Rarity rarity = itemStack.getRarity();
            itemStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return rarity;
        }
    }
}
