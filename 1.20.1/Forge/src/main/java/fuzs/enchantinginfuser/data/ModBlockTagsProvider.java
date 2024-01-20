package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends AbstractTagProvider.Blocks {

    public ModBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper fileHelper) {
        super(packOutput, lookupProvider, modId, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModRegistry.INFUSER_BLOCK.get(), ModRegistry.ADVANCED_INFUSER_BLOCK.get());
    }
}
