package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.level.block.entity.ForgeInfuserBlockEntity;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import fuzs.puzzleslib.init.builder.ModBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EnchantingInfuser.MOD_ID);
    public static final RegistryReference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("enchanting_infuser", () -> ModBlockEntityTypeBuilder.of(ForgeInfuserBlockEntity::new, ModRegistry.INFUSER_BLOCK.get(), ModRegistry.ADVANCED_INFUSER_BLOCK.get()));

    public static void touch() {

    }
}
