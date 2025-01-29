package fuzs.enchantinginfuser.init;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(EnchantingInfuser.MOD_ID);
    public static final Holder.Reference<Block> INFUSER_BLOCK = REGISTRIES.registerBlock("enchanting_infuser",
            (BlockBehaviour.Properties properties) -> new InfuserBlock(InfuserType.NORMAL, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE));
    public static final Holder.Reference<Block> ADVANCED_INFUSER_BLOCK = REGISTRIES.registerBlock(
            "advanced_enchanting_infuser",
            (BlockBehaviour.Properties properties) -> new InfuserBlock(InfuserType.ADVANCED, properties),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE));
    public static final Holder.Reference<Item> INFUSER_ITEM = REGISTRIES.registerBlockItem(INFUSER_BLOCK);
    public static final Holder.Reference<Item> ADVANCED_INFUSER_ITEM = REGISTRIES.registerBlockItem(
            ADVANCED_INFUSER_BLOCK);
    public static final Holder.Reference<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY_TYPE = REGISTRIES.registerBlockEntityType(
            "enchanting_infuser",
            InfuserBlockEntity::new,
            () -> Set.of(INFUSER_BLOCK.value(), ADVANCED_INFUSER_BLOCK.value()));
    public static final Holder.Reference<MenuType<InfuserMenu>> INFUSING_MENU_TYPE = REGISTRIES.registerMenuType(
            "infusing",
            () -> (id, inventory) -> new InfuserMenu(InfuserType.NORMAL, id, inventory));
    public static final Holder.Reference<MenuType<InfuserMenu>> ADVANCED_INFUSING_MENU_TYPE = REGISTRIES.registerMenuType(
            "advanced_infusing",
            () -> (id, inventory) -> new InfuserMenu(InfuserType.ADVANCED, id, inventory));

    static final TagFactory TAGS = TagFactory.make(EnchantingInfuser.MOD_ID);
    public static final TagKey<Enchantment> IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG = TAGS.registerEnchantmentTag(
            "in_enchanting_infuser");
    public static final TagKey<Enchantment> IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG = TAGS.registerEnchantmentTag(
            "in_advanced_enchanting_infuser");

    public static void bootstrap() {
        // NO-OP
    }
}
