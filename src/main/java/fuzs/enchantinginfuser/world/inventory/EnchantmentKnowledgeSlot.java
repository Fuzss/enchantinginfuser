package fuzs.enchantinginfuser.world.inventory;

import fuzs.enchantinginfuser.capability.EnchantmentKnowledgeCapability;
import fuzs.enchantinginfuser.registry.ModRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Set;

public class EnchantmentKnowledgeSlot extends Slot {
    private static final Container EMPTY_CONTAINER = new Container() {

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getItem(int p_18941_) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) {

        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player p_18946_) {
            return false;
        }

        @Override
        public void clearContent() {

        }
    };

    private final Player player;

    public EnchantmentKnowledgeSlot(Player player, int p_40224_, int p_40225_, int p_40226_) {
        super(EMPTY_CONTAINER, p_40224_, p_40225_, p_40226_);
        this.player = player;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEnchanted()) {
            Set<Enchantment> enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
            LazyOptional<EnchantmentKnowledgeCapability> optional = this.player.getCapability(ModRegistry.ENCHANTMENT_KNOWLEDGE_CAPABILITY);
            if (optional.isPresent()) {
                // idea is to not allow voiding any items where all enchants are already known, requires syncing capability to client though so not yet implemented
//                EnchantmentKnowledgeCapability capability = optional.orElseThrow(IllegalStateException::new);
//                return enchantments.stream().anyMatch(Predicate.not(capability::knowsEnchantment));
                return true;
            }
        }
        return false;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.isEnchanted()) return;
        Set<Enchantment> enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
        LazyOptional<EnchantmentKnowledgeCapability> optional = this.player.getCapability(ModRegistry.ENCHANTMENT_KNOWLEDGE_CAPABILITY);
        if (optional.isPresent()) {
            EnchantmentKnowledgeCapability capability = optional.orElseThrow(IllegalStateException::new);
            for (Enchantment enchantment : enchantments) {
                if (!capability.knowsEnchantment(enchantment)) {
                    capability.addKnownEnchantment(enchantment);
                }
            }
            this.player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, this.player.getRandom().nextFloat() * 0.1F + 0.9F);
        }
    }
}
