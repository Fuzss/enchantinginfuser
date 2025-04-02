package fuzs.enchantinginfuser.data.client;

import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.puzzleslib.api.client.data.v2.AbstractAtlasProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModAtlasProvider extends AbstractAtlasProvider {

    public ModAtlasProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addAtlases() {
        this.addMaterial(InfuserRenderer.BOOK_LOCATION);
    }
}
