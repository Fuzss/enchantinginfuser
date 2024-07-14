package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantmentTagsProvider extends AbstractTagProvider.Intrinsic<Enchantment> {

    public ModEnchantmentTagsProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        for (InfuserType infuserType : InfuserType.values()) {
            this.tag(infuserType.notAllowedEnchantments);
        }
    }
}
