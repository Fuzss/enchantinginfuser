package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

public class ModBlockTagsProvider extends AbstractTagProvider.Blocks {

    public ModBlockTagsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModRegistry.INFUSER_BLOCK.value(), ModRegistry.ADVANCED_INFUSER_BLOCK.value());
    }
}
