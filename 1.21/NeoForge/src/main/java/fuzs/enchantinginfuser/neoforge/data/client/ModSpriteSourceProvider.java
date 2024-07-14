package fuzs.enchantinginfuser.neoforge.data.client;

import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.puzzleslib.neoforge.api.data.v2.client.AbstractSpriteSourceProvider;
import fuzs.puzzleslib.neoforge.api.data.v2.core.NeoForgeDataProviderContext;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Optional;

public class ModSpriteSourceProvider extends AbstractSpriteSourceProvider {

    public ModSpriteSourceProvider(NeoForgeDataProviderContext context) {
        super(context);
    }

    @Override
    public void addSpriteSources() {
        this.atlas(SpriteSourceProvider.BLOCKS_ATLAS)
                .addSource(new SingleFile(InfuserRenderer.BOOK_LOCATION.texture(), Optional.empty()));
    }
}
