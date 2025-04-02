package fuzs.enchantinginfuser.neoforge.data.client;

import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.neoforge.api.client.data.v2.AbstractAtlasProvider;

public class ModAtlasProvider extends AbstractAtlasProvider {

    public ModAtlasProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addAtlases() {
        this.addMaterial(InfuserRenderer.BOOK_LOCATION);
    }
}
