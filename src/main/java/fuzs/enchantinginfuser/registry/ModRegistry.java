package fuzs.enchantinginfuser.registry;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.capability.EnchantmentKnowledgeCapability;
import fuzs.enchantinginfuser.capability.EnchantmentKnowledgeCapabilityImpl;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.PlayerRespawnStrategy;
import fuzs.puzzleslib.registry.RegistryManager;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    private static final RegistryManager REGISTRY = RegistryManager.of(EnchantingInfuser.MOD_ID);
    public static final RegistryObject<Block> INFUSER_BLOCK = REGISTRY.registerBlockWithItem("enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.NORMAL, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)), CreativeModeTab.TAB_DECORATIONS);
    public static final RegistryObject<Block> ADVANCED_INFUSER_BLOCK = REGISTRY.registerBlockWithItem("advanced_enchanting_infuser", () -> new InfuserBlock(InfuserBlock.InfuserType.ADVANCED, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(blockState -> 7).strength(5.0F, 1200.0F)), CreativeModeTab.TAB_DECORATIONS);
    public static final RegistryObject<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRY.registerRawBlockEntityType("enchanting_infuser", () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, INFUSER_BLOCK.get(), ADVANCED_INFUSER_BLOCK.get()));
    public static final RegistryObject<MenuType<InfuserMenu>> INFUSING_MENU_TYPE = REGISTRY.registerRawMenuType("infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.NORMAL, id, inventory));
    public static final RegistryObject<MenuType<InfuserMenu>> ADVANCED_INFUSING_MENU_TYPE = REGISTRY.registerRawMenuType("advanced_infusing", () -> (id, inventory) -> InfuserMenu.create(InfuserBlock.InfuserType.ADVANCED, id, inventory));

    private static final CapabilityController CAPABILITIES = CapabilityController.of(EnchantingInfuser.MOD_ID);
    public static final Capability<EnchantmentKnowledgeCapability> ENCHANTMENT_KNOWLEDGE_CAPABILITY = CAPABILITIES.registerPlayerCapability("enchantment_knowledge", EnchantmentKnowledgeCapability.class, player -> new EnchantmentKnowledgeCapabilityImpl(), PlayerRespawnStrategy.ALWAYS_COPY, new CapabilityToken<EnchantmentKnowledgeCapability>() {});

    public static void touch() {

    }
}
