package fuzs.enchantinginfuser.world.level.block;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CInfuserDataMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class InfuserBlock extends EnchantingTableBlock {
    public InfuserBlock(Properties p_52953_) {
        super(p_52953_);
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
        return new InfuserBlockEntity();
    }

    protected InfuserType getInfuserType() {
        return InfuserType.NORMAL;
    }

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        final TileEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof InfuserBlockEntity) {
            if (pLevel.isClientSide) {
                return ActionResultType.SUCCESS;
            }
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            if (pPlayer.containerMenu instanceof InfuserMenu) {
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                pPlayer.containerMenu.slotsChanged(((InfuserBlockEntity) blockEntity));
                final int power = ((InfuserMenu) pPlayer.containerMenu).setEnchantingPower(pLevel, pPos);
                EnchantingInfuser.NETWORK.sendTo(new S2CInfuserDataMessage(pPlayer.containerMenu.containerId, power, this.getInfuserType()), (ServerPlayerEntity) pPlayer);
            }
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    @Override
    @Nullable
    public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
        final TileEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof InfuserBlockEntity) {
            ITextComponent component = ((InfuserBlockEntity) blockEntity).getDisplayName();
            return new SimpleNamedContainerProvider((p_52959_, p_52960_, p_52961_) -> {
                if (((InfuserBlockEntity) blockEntity).canOpen(p_52961_)) {
                    return new InfuserMenu(p_52959_, p_52960_, ((InfuserBlockEntity) blockEntity), IWorldPosCallable.create(pLevel, pPos), this.getInfuserType());
                }
                return null;
            }, component);
        } else {
            return null;
        }
    }

    @Override
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
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
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            final TileEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof InfuserBlockEntity) {
                InventoryHelper.dropContents(worldIn, pos, ((InfuserBlockEntity) blockEntity));
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        final TileEntity blockEntity = worldIn.getBlockEntity(pos);
        if (blockEntity instanceof InfuserBlockEntity) {
            if (!((InfuserBlockEntity) blockEntity).getItem(0).isEmpty()) {
                return 15;
            }
        }
        return 0;
    }

    public enum InfuserType {
        NORMAL, ADVANCED;

        public boolean isAdvanced() {
            return this == ADVANCED;
        }
    }
}
