package fuzs.enchantinginfuser.registry;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.AdvancedInfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.PuzzlesLib;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModRegistry {
    @ObjectHolder(EnchantingInfuser.MOD_ID + ":" + "enchanting_infuser")
    public static final Block INFUSER_BLOCK = null;
    @ObjectHolder(EnchantingInfuser.MOD_ID + ":" + "advanced_enchanting_infuser")
    public static final Block ADVANCED_INFUSER_BLOCK = null;
    @ObjectHolder(EnchantingInfuser.MOD_ID + ":" + "enchanting_infuser")
    public static final TileEntityType<InfuserBlockEntity> INFUSER_BLOCK_ENTITY_TYPE = null;
    @ObjectHolder(EnchantingInfuser.MOD_ID + ":" + "infusing")
    public static final ContainerType<InfuserMenu> INFUSING_MENU_TYPE = null;

    public static void touch() {
        PuzzlesLib.getRegistryManagerV2().registerBlockWithItem("enchanting_infuser", () -> new InfuserBlock(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().strength(5.0F, 1200.0F)), ItemGroup.TAB_DECORATIONS);
        PuzzlesLib.getRegistryManagerV2().registerBlockWithItem("advanced_enchanting_infuser", () -> new AdvancedInfuserBlock(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().strength(5.0F, 1200.0F)), ItemGroup.TAB_DECORATIONS);
        PuzzlesLib.getRegistryManagerV2().registerRawTileEntityType("enchanting_infuser", () -> TileEntityType.Builder.of(InfuserBlockEntity::new, INFUSER_BLOCK, ADVANCED_INFUSER_BLOCK));
        PuzzlesLib.getRegistryManagerV2().registerRawContainerType("infusing", () -> InfuserMenu::new);
    }
}
