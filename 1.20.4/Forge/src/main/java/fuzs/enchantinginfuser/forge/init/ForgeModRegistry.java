package fuzs.enchantinginfuser.forge.init;

import fuzs.enchantinginfuser.forge.world.level.block.entity.ForgeInfuserBlockEntity;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    public static final Holder.Reference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = ModRegistry.REGISTRY.registerBlockEntityType("enchanting_infuser", () -> BlockEntityType.Builder.of(ForgeInfuserBlockEntity::new, ModRegistry.INFUSER_BLOCK.value(), ModRegistry.ADVANCED_INFUSER_BLOCK.value()));

    public static void touch() {

    }
}
