package fuzs.enchantinginfuser.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class ForgeInfuserBlockEntity extends InfuserBlockEntity {
    private LazyOptional<IItemHandler> infuserHandler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));

    public ForgeInfuserBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(pWorldPosition, pBlockState);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.infuserHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.infuserHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        // use SidedInvWrapper instead of InvWrapper as otherwise restrictions for extraction (only enchanted items) are ignored
        this.infuserHandler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));
    }
}
