package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.registry.RegistryManager;
import fuzs.puzzleslib.registry.RegistryReference;
import fuzs.puzzleslib.registry.builder.ModBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EnchantingInfuser.MOD_ID);
    public static final RegistryReference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("enchanting_infuser", () -> ModBlockEntityTypeBuilder.of(InfuserBlockEntity::new, ModRegistry.INFUSER_BLOCK.get(), ModRegistry.ADVANCED_INFUSER_BLOCK.get()));

    public static void touch() {

    }
}
