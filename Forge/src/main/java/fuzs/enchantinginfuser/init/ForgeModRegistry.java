package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.world.level.block.entity.ForgeInfuserBlockEntity;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.init.v2.RegistryReference;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    public static final RegistryReference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = ModRegistry.REGISTRY.registerBlockEntityType("enchanting_infuser", () -> BlockEntityType.Builder.of(ForgeInfuserBlockEntity::new, ModRegistry.INFUSER_BLOCK.get(), ModRegistry.ADVANCED_INFUSER_BLOCK.get()));

    public static void touch() {

    }
}
