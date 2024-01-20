package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ModRegistry {
    public static final RegistryManager REGISTRY = RegistryManager.from(EnchantingInfuser.MOD_ID);
    public static final Holder.Reference<Block> INFUSER_BLOCK = REGISTRY.registerBlock("enchanting_infuser", () -> new InfuserBlock(InfuserType.NORMAL, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)));
    public static final Holder.Reference<Block> ADVANCED_INFUSER_BLOCK = REGISTRY.registerBlock("advanced_enchanting_infuser", () -> new InfuserBlock(InfuserType.ADVANCED, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)));
    public static final Holder.Reference<Item> INFUSER_ITEM = REGISTRY.registerBlockItem(INFUSER_BLOCK);
    public static final Holder.Reference<Item> ADVANCED_INFUSER_ITEM = REGISTRY.registerBlockItem(ADVANCED_INFUSER_BLOCK);
    public static final Holder.Reference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.whenNotOn(ModLoader.FORGE).registerBlockEntityType("enchanting_infuser", () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, INFUSER_BLOCK.value(), ADVANCED_INFUSER_BLOCK.value()));
    public static final Holder.Reference<MenuType<InfuserMenu>> INFUSING_MENU_TYPE = REGISTRY.registerMenuType("infusing", () -> (id, inventory) -> new InfuserMenu(InfuserType.NORMAL, id, inventory));
    public static final Holder.Reference<MenuType<InfuserMenu>> ADVANCED_INFUSING_MENU_TYPE = REGISTRY.registerMenuType("advanced_infusing", () -> (id, inventory) -> new InfuserMenu(InfuserType.ADVANCED, id, inventory));

    public static void touch() {

    }
}
