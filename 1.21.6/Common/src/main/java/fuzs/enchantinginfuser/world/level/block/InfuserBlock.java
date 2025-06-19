package fuzs.enchantinginfuser.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ModifiableItems;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.block.v1.entity.TickingEntityBlock;
import fuzs.puzzleslib.api.util.v1.InteractionResultHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InfuserBlock extends BaseEntityBlock implements TickingEntityBlock<InfuserBlockEntity> {
    public static final MapCodec<InfuserBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(InfuserType.CODEC.fieldOf("type").forGetter(InfuserBlock::getType), propertiesCodec())
                .apply(instance, InfuserBlock::new);
    });
    public static final Component COMPONENT_CHOOSE = Component.translatable(
            "block." + EnchantingInfuser.MOD_ID + ".description.choose");
    public static final Component COMPONENT_CHOOSE_AND_MODIFY = Component.translatable(
            "block." + EnchantingInfuser.MOD_ID + ".description.chooseAndModify");
    public static final Component COMPONENT_REPAIR = Component.translatable(
            "block." + EnchantingInfuser.MOD_ID + ".description.repair");
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    private final InfuserType type;

    public InfuserBlock(InfuserType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public static boolean isValidBookShelf(Level level, BlockPos pos, BlockPos offset) {
        if (EnchantingBehavior.get()
                .getEnchantmentPower(level.getBlockState(pos.offset(offset)), level, pos.offset(offset)) != 0.0F) {
            BlockPos inBetweenPos = pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
            return level.getBlockState(inBetweenPos).getCollisionShape(level, inBetweenPos) != Shapes.block();
        } else {
            return false;
        }
    }

    @Override
    public MapCodec<InfuserBlock> codec() {
        return CODEC;
    }

    public InfuserType getType() {
        return this.type;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntityType<? extends InfuserBlockEntity> getBlockEntityType() {
        return ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.value();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!level.isClientSide) {
                player.openMenu(state.getMenuProvider(level, pos));
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                player.containerMenu.slotsChanged(blockEntity);
            }

            return InteractionResultHelper.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof InfuserBlockEntity blockEntity) {
            Component component = blockEntity.getDisplayName();
            return new SimpleMenuProvider((int containerId, Inventory inventory, Player player) -> {
                if (blockEntity.canOpen(player)) {
                    InfuserMenu menu = new InfuserMenu(this.type,
                            containerId,
                            inventory,
                            blockEntity,
                            ContainerLevelAccess.create(level, blockPos));
                    menu.addSlotListener(menu);
                    return menu;
                } else {
                    return null;
                }
            }, component);
        } else {
            return null;
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && isValidBookShelf(level, pos, offset)) {
                level.addParticle(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5,
                        pos.getY() + 2.0,
                        pos.getZ() + 0.5,
                        ((float) offset.getX() + random.nextFloat()) - 0.5,
                        ((float) offset.getY() - random.nextFloat() - 1.0F),
                        ((float) offset.getZ() + random.nextFloat()) - 0.5);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType type) {
        return false;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean isMoving) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    public Component getDescriptionComponent() {
        Component component;
        if (this.getType().getConfig().allowModifyingEnchantments == ModifiableItems.UNENCHANTED) {
            component = InfuserBlock.COMPONENT_CHOOSE;
        } else {
            component = InfuserBlock.COMPONENT_CHOOSE_AND_MODIFY;
        }
        MutableComponent mutableComponent = Component.empty().append(component).withStyle(ChatFormatting.GRAY);
        if (this.getType().getConfig().allowRepairing.isActive()) {
            mutableComponent = mutableComponent.append(CommonComponents.SPACE).append(InfuserBlock.COMPONENT_REPAIR);
        }
        return mutableComponent;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!blockEntity.getItem(0).isEmpty()) {
                return 15;
            }
        }
        return 0;
    }
}
