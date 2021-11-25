package fuzs.enchantinginfuser.world.level.block;

import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class InfuserBlock extends EnchantmentTableBlock {
    public InfuserBlock(Properties p_52953_) {
        super(p_52953_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new InfuserBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.get(), EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockEntity) {
            if (pLevel.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            pPlayer.openMenu(blockEntity);
            if (pPlayer.containerMenu instanceof InfuserMenu menu) {
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                menu.slotsChanged(blockEntity);
                menu.setEnchantingPower(pLevel, pPos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand) {
        super.animateTick(pState, pLevel, pPos, pRand);
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }
                if (pRand.nextInt(16) == 0) {
                    for(int k = 0; k <= 1; ++k) {
                        BlockPos blockpos = pPos.offset(i, k, j);
                        if (pLevel.getBlockState(blockpos).getEnchantPowerBonus(pLevel, blockpos) > 0) {
                            if (!InfuserMenu.isBlockEmpty(pLevel, pPos.offset(i / 2, 0, j / 2))) {
                                break;
                            }
                            pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 2.0D, (double)pPos.getZ() + 0.5D, (double)((float)i + pRand.nextFloat()) - 0.5D, (double)((float)k - pRand.nextFloat() - 1.0F), (double)((float)j + pRand.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }
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
