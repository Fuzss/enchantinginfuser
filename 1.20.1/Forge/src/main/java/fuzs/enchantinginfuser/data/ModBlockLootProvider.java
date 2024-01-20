package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractLootProvider;
import net.minecraft.data.PackOutput;

public class ModBlockLootProvider extends AbstractLootProvider.Blocks{

    public ModBlockLootProvider(PackOutput packOutput, String modId) {
        super(packOutput, modId);
    }

    @Override
    public void generate() {
        this.add(ModRegistry.INFUSER_BLOCK.get(), this::createNameableBlockEntityTable);
        this.add(ModRegistry.ADVANCED_INFUSER_BLOCK.get(), this::createNameableBlockEntityTable);
    }
}
