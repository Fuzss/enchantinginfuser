package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantmentTagsProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagsProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG).addTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        this.tag(ModRegistry.IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG)
                .addTag(EnchantmentTags.IN_ENCHANTING_TABLE);
    }
}
