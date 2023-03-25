package fuzs.enchantinginfuser.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChiseledBookshelfHelper {

    public static int findValidBooks(Level level, BlockPos pos, BlockPos offset) {
        BlockState state = level.getBlockState(pos.offset(offset));
        if (state.is(Blocks.CHISELED_BOOKSHELF)) {
            if (level.getBlockEntity(pos.offset(offset)) instanceof ChiseledBookShelfBlockEntity blockEntity) {
                Direction direction = state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
                int axisOffset = direction.getAxis().choose(offset.getX(), offset.getY(), offset.getZ());
                if (Math.abs(axisOffset) == 2 && Math.signum(axisOffset) == direction.getAxisDirection().getStep()) {
                    return blockEntity.count();
                }
            }
        }
        return 0;
    }
}
