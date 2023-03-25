package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v2.RegistryManager;
import fuzs.puzzleslib.api.init.v2.RegistryReference;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.instant(EnchantingInfuser.MOD_ID);
    public static final RegistryReference<Block> INFUSER_BLOCK = REGISTRY.registerBlock("enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.NORMAL, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)));
    public static final RegistryReference<Block> ADVANCED_INFUSER_BLOCK = REGISTRY.registerBlock("advanced_enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.ADVANCED, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)));
    public static final RegistryReference<Item> INFUSER_ITEM = REGISTRY.registerBlockItem(INFUSER_BLOCK);
    public static final RegistryReference<Item> ADVANCED_INFUSER_ITEM = REGISTRY.registerBlockItem(ADVANCED_INFUSER_BLOCK);
    public static final RegistryReference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.whenNotOn(ModLoader.FORGE).registerBlockEntityType("enchanting_infuser", () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, INFUSER_BLOCK.get(), ADVANCED_INFUSER_BLOCK.get()));
    public static final RegistryReference<MenuType<InfuserMenu>> INFUSING_MENU_TYPE = REGISTRY.registerMenuType("infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.NORMAL, id, inventory));
    public static final RegistryReference<MenuType<InfuserMenu>> ADVANCED_INFUSING_MENU_TYPE = REGISTRY.registerMenuType("advanced_infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.ADVANCED, id, inventory));

    public static void touch() {

    }
}
