package fuzs.enchantinginfuser.world.level.block.entity;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.puzzleslib.api.container.v1.ListBackedContainer;
import fuzs.puzzleslib.api.container.v1.MenuProviderWithData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class InfuserBlockEntity extends EnchantingTableBlockEntity implements WorldlyContainer, TickingBlockEntity, ListBackedContainer, MenuProviderWithData<InfuserType> {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private LockCode code = LockCode.NO_LOCK;

    public InfuserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
        this.type = this.getType();
    }

    @Override
    public BlockEntityType<?> getType() {
        // set in super constructor, so just override the whole method
        return ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.value();
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.code = LockCode.fromTag(valueInput);
        this.items.clear();
        ContainerHelper.loadAllItems(valueInput, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        this.code.addToTag(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items, true);
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public NonNullList<ItemStack> getContainerItems() {
        return this.items;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level != null && this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr(this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5) > 64.0);
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        if (index == 0) {
            return this.getItem(0).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        // only allow extracting of enchantable item
        return index == 0 && (itemStack.isEnchanted() || itemStack.is(Items.ENCHANTED_BOOK));
    }

    public boolean canOpen(Player player) {
        return BaseContainerBlockEntity.canUnlock(player, this.code, this.getDisplayName());
    }

    @Override
    public void clientTick() {
        bookAnimationTick(this.level, this.worldPosition, this.getBlockState(), this);
    }

    @Override
    public InfuserType getMenuData(ServerPlayer serverPlayer) {
        return ((InfuserBlock) this.getBlockState().getBlock()).getType();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (this.canOpen(player)) {
            InfuserType type = ((InfuserBlock) this.getBlockState().getBlock()).getType();
            InfuserMenu menu = new InfuserMenu(type,
                    containerId,
                    inventory,
                    this,
                    ContainerLevelAccess.create(this.getLevel(), this.getBlockPos()));
            menu.addSlotListener(menu);
            return menu;
        } else {
            return null;
        }
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }
}
