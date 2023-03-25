package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.puzzleslib.api.data.v1.AbstractSpriteSourceProvider;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import java.util.Optional;

public class ModSpriteSourceProvider extends AbstractSpriteSourceProvider {

    public ModSpriteSourceProvider(PackOutput packOutput, String modId, ExistingFileHelper fileHelper) {
        super(packOutput, modId, fileHelper);
    }

    @Override
    protected void addSources() {
        this.atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(InfuserRenderer.BOOK_LOCATION.texture(), Optional.empty()));
    }
}
