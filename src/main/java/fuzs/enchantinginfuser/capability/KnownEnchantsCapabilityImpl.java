package fuzs.enchantinginfuser.capability;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Set;

public class KnownEnchantsCapabilityImpl implements KnownEnchantsCapability {
    private static final String TAG_KEY = "KnownEnchantments";

    private final Set<Enchantment> enchantments = Sets.newHashSet();

    @Override
    public void addKnownEnchantment(Enchantment enchantment) {
        this.enchantments.add(enchantment);
    }

    @Override
    public boolean knowsEnchantment(Enchantment enchantment) {
        return this.enchantments.contains(enchantment);
    }

    @Override
    public Collection<Enchantment> getKnownEnchantments() {
        return this.enchantments;
    }

    @Override
    public void write(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Enchantment enchantment : this.enchantments) {
            ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            list.add(StringTag.valueOf(key.toString()));
        }
        tag.put(TAG_KEY, list);
    }

    @Override
    public void read(CompoundTag tag) {
        this.enchantments.clear();
        if (tag.contains(TAG_KEY, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_KEY, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation enchantment = new ResourceLocation(list.getString(i));
                if (ForgeRegistries.ENCHANTMENTS.containsKey(enchantment)) {
                    this.enchantments.add(ForgeRegistries.ENCHANTMENTS.getValue(enchantment));
                }
            }
        }
    }
}
