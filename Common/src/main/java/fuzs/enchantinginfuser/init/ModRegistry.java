package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.registry.RegistryManager;
import fuzs.puzzleslib.registry.RegistryReference;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EnchantingInfuser.MOD_ID);
    public static final RegistryReference<Block> INFUSER_BLOCK = REGISTRY.registerBlockWithItem("enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.NORMAL, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)), CreativeModeTab.TAB_DECORATIONS);
    public static final RegistryReference<Block> ADVANCED_INFUSER_BLOCK = REGISTRY.registerBlockWithItem("advanced_enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.ADVANCED, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)), CreativeModeTab.TAB_DECORATIONS);
    public static final RegistryReference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.placeholder(Registry.BLOCK_ENTITY_TYPE_REGISTRY, "enchanting_infuser");
    public static final RegistryReference<MenuType<InfuserMenu>> INFUSING_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.NORMAL, id, inventory));
    public static final RegistryReference<MenuType<InfuserMenu>> ADVANCED_INFUSING_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("advanced_infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.ADVANCED, id, inventory));

    public static void touch() {

    }
}
