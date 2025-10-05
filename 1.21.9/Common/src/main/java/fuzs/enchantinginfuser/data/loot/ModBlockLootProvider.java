package fuzs.enchantinginfuser.data.loot;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractLootProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModBlockLootProvider extends AbstractLootProvider.Blocks {

    public ModBlockLootProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addLootTables() {
        this.add(ModRegistry.INFUSER_BLOCK.value(), this::createNameableBlockEntityTable);
        this.add(ModRegistry.ADVANCED_INFUSER_BLOCK.value(), this::createNameableBlockEntityTable);
    }
}
