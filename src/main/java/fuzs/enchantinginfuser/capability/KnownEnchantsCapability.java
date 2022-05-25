package fuzs.enchantinginfuser.capability;

import fuzs.puzzleslib.capability.data.CapabilityComponent;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;

public interface KnownEnchantsCapability extends CapabilityComponent {

    void addKnownEnchantment(Enchantment enchantment);

    boolean knowsEnchantment(Enchantment enchantment);

    Collection<Enchantment> getKnownEnchantments();
}
