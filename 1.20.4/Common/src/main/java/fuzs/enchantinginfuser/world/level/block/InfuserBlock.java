package fuzs.enchantinginfuser.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.api.v2.EnchantingInfuserApi;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.util.ChiseledBookshelfHelper;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.block.v1.entity.TickingEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfuserBlock extends BaseEntityBlock implements TickingEntityBlock<InfuserBlockEntity> {
    public static final MapCodec<InfuserBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(InfuserType.CODEC.fieldOf("type").forGetter(infuserBlock -> infuserBlock.type), propertiesCodec()).apply(instance, InfuserBlock::new);
    });
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    private static final Component CHOOSE_TOOLTIP = Component.translatable("block.enchantinginfuser.description.choose");
    private static final Component CHOOSE_AND_MODIFY_TOOLTIP = Component.translatable("block.enchantinginfuser.description.chooseAndModify");
    private static final Component REPAIR_TOOLTIP = Component.translatable("block.enchantinginfuser.description.repair");

    private final InfuserType type;

    public InfuserBlock(InfuserType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public static boolean isValidBookShelf(Level level, BlockPos pos, BlockPos offset) {
        if (EnchantingInfuserApi.getEnchantStatsProvider().getEnchantPowerBonus(level.getBlockState(pos.offset(offset)), level, pos.offset(offset)) == 0.0F) {
            if (ChiseledBookshelfHelper.findValidBooks(level, pos, offset) == 0) {
                return false;
            }
        }
        BlockPos inBetweenPos = pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
        return level.getBlockState(inBetweenPos).getCollisionShape(level, inBetweenPos) != Shapes.block();
    }

    @Override
    public MapCodec<InfuserBlock> codec() {
        return CODEC;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!level.isClientSide) {
                player.openMenu(state.getMenuProvider(level, pos));
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                player.containerMenu.slotsChanged(blockEntity);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockentity) {
            Component component = blockentity.getDisplayName();
            return new SimpleMenuProvider((p_52959_, p_52960_, p_52961_) -> {
                if (blockentity.canOpen(p_52961_)) {
                    return new InfuserMenu(this.type, p_52959_, p_52960_, blockentity, ContainerLevelAccess.create(pLevel, pPos));
                }
                return null;
            }, component);
        } else {
            return null;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for(BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && isValidBookShelf(level, pos, blockpos)) {
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, ((float)blockpos.getX() + random.nextFloat()) - 0.5D, ((float)blockpos.getY() - random.nextFloat() - 1.0F), ((float)blockpos.getZ() + random.nextFloat()) - 0.5D);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof EnchantmentTableBlockEntity blockEntity) {
                blockEntity.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
                Containers.dropContents(worldIn, pos, blockEntity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
        // don't let this go through when initially gathering tooltip data during start-up, configs do not exist then and it's ok if this is not searchable
        if (!EnchantingInfuser.CONFIG.getHolder(ServerConfig.class).isAvailable()) return;
        Component component;
        if (this.type.getConfig().allowModifyingEnchantments == ServerConfig.ModifiableItems.UNENCHANTED) {
            component = CHOOSE_TOOLTIP;
        } else {
            component = CHOOSE_AND_MODIFY_TOOLTIP;
        }
        MutableComponent mutableComponent = Component.empty().append(component).withStyle(ChatFormatting.GRAY);
        if (this.type.getConfig().allowRepairing.isActive()) {
            mutableComponent = mutableComponent.append(" ").append(REPAIR_TOOLTIP);
        }
        list.add(mutableComponent);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!blockEntity.getItem(0).isEmpty()) {
                return 15;
            }
        }
        return 0;
    }
}
