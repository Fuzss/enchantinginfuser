package fuzs.enchantinginfuser.world.inventory;

import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ModifiableItems;
import fuzs.enchantinginfuser.network.ClientboundInfuserEnchantmentsMessage;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.util.EnchantmentCostHelper;
import fuzs.enchantinginfuser.util.EnchantmentPowerHelper;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import fuzs.enchantinginfuser.util.PlayerExperienceHelper;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import fuzs.puzzleslib.api.network.v3.PlayerSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.IntSupplier;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    public static final int ENCHANT_BUTTON = 0;
    public static final int REPAIR_BUTTON = 1;
    public static final int ENCHANT_ITEM_SLOT = 0;
    public static final int ENCHANTMENT_POWER_DATA_SLOT = 0;
    public static final int ENCHANTING_COST_DATA_SLOT = 1;
    public static final int REPAIR_COST_DATA_SLOT = 2;

    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    public final EnchantingBehavior behavior;
    private final TagKey<Enchantment> tagKey;
    private final DataSlot enchantmentPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private ItemEnchantments.Mutable itemEnchantments;
    private Object2IntMap<Holder<Enchantment>> availableEnchantmentLevels;
    private Object2IntMap<Holder<Enchantment>> requiredEnchantmentPowers;
    private int originalEnchantingCost;
    private boolean markedDirty;

    public InfuserMenu(InfuserType infuserType, int containerId, Inventory inventory) {
        this(infuserType, containerId, inventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public InfuserMenu(InfuserType infuserType, int containerId, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        super(infuserType.getMenuType(), containerId);
        checkContainerSize(container, 1);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.behavior = infuserType.createBehavior();
        this.tagKey = infuserType.getAvailableEnchantments();
        this.addSlot(new Slot(container, 0, 8, this.behavior.getConfig().allowRepairing.isActive() ? 23 : 34) {

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = InventoryMenu.SLOT_IDS[k];
            ResourceLocation resourceLocation = InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
            this.addSlot(new ArmorSlot(inventory,
                    inventory.player,
                    equipmentSlot,
                    39 - k,
                    8 + 188 * (k / 2),
                    103 + (k % 2) * 18,
                    resourceLocation));
        }
        ContainerMenuHelper.addInventorySlots(this, inventory, 30, 103);
        this.addSlot(new Slot(inventory, 40, 8, 161) {

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
        this.addDataSlot(this.enchantmentPower);
        this.addDataSlot(this.enchantingCost);
        this.addDataSlot(this.repairCost);
        this.addSlotListener(this);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            this.setInitialEnchantments(null, ItemEnchantments.EMPTY);
            // using setData will update the client screen listener
            this.setData(ENCHANTING_COST_DATA_SLOT, 0);
            this.setData(REPAIR_COST_DATA_SLOT, 0);
            if (this.mayEnchantStack(this.getEnchantableStack())) {
                this.levelAccess.execute((Level level, BlockPos pos) -> {
                    this.setInitialEnchantments(level, this.getOriginalEnchantments());
                    this.setData(ENCHANTMENT_POWER_DATA_SLOT, this.getAvailablePower(level, pos));
                    this.setData(REPAIR_COST_DATA_SLOT, this.calculateRepairCost());
                });
            }
        }
    }

    private boolean mayEnchantStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else if (this.behavior.getConfig().allowBooks) {
            if (itemStack.is(Items.BOOK)) {
                return true;
            } else if (itemStack.is(Items.ENCHANTED_BOOK)) {
                return this.behavior.getConfig().allowModifyingEnchantments != ModifiableItems.UNENCHANTED;
            }
        } else if (ModEnchantmentHelper.isBook(itemStack)) {
            return false;
        }

        return this.behavior.getConfig().allowModifyingEnchantments.predicate.test(itemStack);
    }

    private int getAvailablePower(Level level, BlockPos pos) {
        float enchantingPower = 0.0F;
        float maxPowerScale = 1.0F;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (InfuserBlock.isValidBookShelf(this.behavior, level, pos, offset)) {
                BlockState blockState = level.getBlockState(pos.offset(offset));
                enchantingPower += this.behavior.getEnchantmentPower(blockState, level, pos.offset(offset));
                maxPowerScale = Math.max(maxPowerScale,
                        this.behavior.getEnchantmentPowerLimitScale(blockState, level, pos.offset(offset)));
            }
        }
        // Apotheosis has bookshelves with negative enchanting power, so make sure this value doesn't go there
        return (int) Math.min(Math.max(0.0F, enchantingPower),
                this.behavior.getConfig().maximumBookshelves * maxPowerScale);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (containerMenu == this && dataSlotIndex == ENCHANT_ITEM_SLOT) {
            this.slotsChanged(this.enchantSlots);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // NO-OP
    }

    public boolean clickClientEnchantmentLevelButton(Holder<Enchantment> enchantment, int enchantmentLevel, ServerboundEnchantmentLevelMessage.Operation operation) {
        int newLevel = this.clickEnchantmentLevelButton(enchantment, operation);
        if (newLevel != enchantmentLevel) {
            EnchantingInfuser.NETWORK.sendMessage(new ServerboundEnchantmentLevelMessage(this.containerId,
                    enchantment,
                    operation));
            return true;
        } else {
            return false;
        }
    }

    public int clickEnchantmentLevelButton(Holder<Enchantment> enchantment, IntSupplier operation) {
        // the enchantment is newly added and is not compatible with existing enchantments, so no level is allowed
        if (this.itemEnchantments.getLevel(enchantment) == 0 &&
                !EnchantmentAdapter.get().isEnchantmentCompatible(this.itemEnchantments.keySet(), enchantment)) {
            return 0;
        } else {
            int enchantmentLevel = this.itemEnchantments.getLevel(enchantment);
            int newEnchantmentLevel = enchantmentLevel + operation.getAsInt();
            // the new enchantment level exceeds min & max enchantment bounds
            newEnchantmentLevel = Mth.clamp(newEnchantmentLevel, 0, EnchantmentAdapter.get().getMaxLevel(enchantment));
            // the new enchantment level exceeds max possible level for the available enchantment power,
            // based on the current block configuration
            newEnchantmentLevel = Math.min(newEnchantmentLevel, this.getAvailableEnchantmentLevel(enchantment));
            if (newEnchantmentLevel != enchantmentLevel) {
                this.itemEnchantments.set(enchantment, newEnchantmentLevel);
                this.setChanged();
                this.sendEnchantments(this.itemEnchantments.toImmutable(), false);
                this.setData(ENCHANTING_COST_DATA_SLOT, this.calculateEnchantingCost());
            }
            return newEnchantmentLevel;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case ENCHANT_BUTTON -> this.clickEnchantButton(player);
            case REPAIR_BUTTON -> this.clickRepairButton(player);
            default -> false;
        };
    }

    private boolean clickEnchantButton(Player player) {
        if (this.canEnchant(player)) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                this.processEnchantingCost(player, level, pos, this.getEnchantingCost());
                ItemStack itemStack = ModEnchantmentHelper.setNewEnchantments(this.getEnchantableStack(),
                        this.itemEnchantments.toImmutable(),
                        this.behavior.getConfig().increaseAnvilRepairCost);
                this.enchantSlots.setItem(ENCHANT_ITEM_SLOT, itemStack);
                if (this.getEnchantingCost() > 0) {
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) player,
                                itemStack,
                                this.getEnchantingCost());
                    }
                }
                this.enchantSlots.setChanged();
                this.slotsChanged(this.enchantSlots);
                level.playSound(null,
                        pos,
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS,
                        1.0F,
                        level.random.nextFloat() * 0.1F + 0.9F);
            });

            return true;
        } else {

            return false;
        }
    }

    private void processEnchantingCost(Player player, Level level, BlockPos pos, int enchantingCost) {
        if (enchantingCost < 0) {
            ExperienceOrb.award((ServerLevel) level,
                    Vec3.atCenterOf(pos),
                    PlayerExperienceHelper.calculateExperienceDelta(this.itemEnchantments.toImmutable(),
                            this.getOriginalEnchantments(),
                            level.random));
        } else if (!player.getAbilities().instabuild) {
            // don't use Player::onEnchantmentPerformed as it also reseeds enchantments seed which we have nothing to do with
            player.giveExperienceLevels(-enchantingCost);
        }
    }

    private int calculateEnchantingCost() {
        int enchantmentCosts =
                this.getScaledEnchantmentCosts(this.itemEnchantments.toImmutable()) - this.originalEnchantingCost;
        if (enchantmentCosts == 0 && this.markedDirty) {
            return 1;
        } else {
            return enchantmentCosts;
        }
    }

    private boolean clickRepairButton(Player player) {
        if (this.canRepair(player)) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                if (!player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-this.getRepairCost());
                }
                ItemStack itemStack = this.getEnchantableStack();
                int itemRepairCost = itemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
                itemStack = itemStack.copy();
                itemStack.setDamageValue(0);
                if (this.behavior.getConfig().increaseAnvilRepairCost) {
                    itemStack.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(itemRepairCost));
                }
                this.enchantSlots.setItem(ENCHANT_ITEM_SLOT, itemStack);
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
            });

            return true;
        } else {

            return false;
        }
    }

    private int calculateRepairCost() {
        ItemStack itemStack = this.getEnchantableStack();
        if (this.behavior.getConfig().allowRepairing.canRepair(itemStack)) {
            double repairStep = itemStack.getMaxDamage() * this.behavior.getConfig().repair.repairPercentageStep;
            return (int) Math.ceil(Math.ceil(itemStack.getDamageValue() / repairStep) *
                    this.behavior.getConfig().repair.repairStepMultiplier);
        } else {
            return 0;
        }
    }

    private int getScaledEnchantmentCosts(ItemEnchantments itemEnchantments) {
        int scalingEnchantmentCosts = EnchantmentCostHelper.getScalingEnchantmentCosts(this.availableEnchantmentLevels.keySet(),
                this.getScalingNamespaces());
        int maximumCost = this.behavior.getMaximumCost();
        if (scalingEnchantmentCosts > maximumCost && !ModEnchantmentHelper.isBook(this.getEnchantableStack())) {
            float enchantmentCostScale = maximumCost / (float) scalingEnchantmentCosts;
            int scaledEnchantmentCosts = Math.round(
                    EnchantmentCostHelper.getEnchantmentCosts(itemEnchantments) * enchantmentCostScale);
            int minimumCosts = itemEnchantments.entrySet().stream().mapToInt(Object2IntMap.Entry::getIntValue).sum();
            return Math.max(scaledEnchantmentCosts, minimumCosts);
        } else {
            return EnchantmentCostHelper.getEnchantmentCosts(itemEnchantments);
        }
    }

    private Collection<String> getScalingNamespaces() {
        if (this.behavior.getConfig().costs.scaleCostsByVanillaOnly) {
            return this.behavior.getScalingNamespaces();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemInSlot = slot.getItem();
            itemStack = itemInSlot.copy();
            EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
            if (index == 0) {
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR &&
                        !this.slots.get(4 - equipmentSlot.getIndex()).hasItem()) {
                    int i = 4 - equipmentSlot.getIndex();
                    if (!this.moveItemStackTo(itemInSlot, i, i + 1, false)) {
                        slot.onTake(player, itemInSlot);
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentSlot == EquipmentSlot.OFFHAND && !this.slots.get(41).hasItem()) {
                    if (!this.moveItemStackTo(itemInSlot, 41, 42, false)) {
                        slot.onTake(player, itemInSlot);
                        return ItemStack.EMPTY;
                    }
                }
                if (!this.moveItemStackTo(itemInSlot, 5, 41, true)) {
                    slot.onTake(player, itemInSlot);
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.getFirst().hasItem()) {
                    return ItemStack.EMPTY;
                }
                ItemStack itemInSlotCopy = itemInSlot.copy();
                itemInSlotCopy.setCount(1);
                itemInSlot.shrink(1);
                this.slots.getFirst().set(itemInSlotCopy);
            }
            if (itemInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemInSlot.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemInSlot);
        }

        return itemStack;
    }

    public int getEnchantmentPower() {
        return Math.min(this.enchantmentPower.get(), this.getEnchantmentPowerLimit());
    }

    public int getEnchantmentPowerLimit() {
        return this.behavior.getEnchantmentPowerLimit();
    }

    public ItemStack getEnchantableStack() {
        return this.enchantSlots.getItem(ENCHANT_ITEM_SLOT);
    }

    public int getEnchantingCost() {
        return this.enchantingCost.get();
    }

    public int getRepairCost() {
        return this.repairCost.get();
    }

    public boolean canEnchant(Player player) {
        if (!this.getEnchantableStack().isEmpty() && this.markedDirty) {
            return player.experienceLevel >= this.getEnchantingCost() || player.getAbilities().instabuild;
        } else {
            return false;
        }
    }

    public boolean canRepair(Player player) {
        if (this.behavior.getConfig().allowRepairing.canRepair(this.getEnchantableStack())) {
            return player.experienceLevel >= this.getRepairCost() || player.getAbilities().instabuild;
        } else {
            return false;
        }
    }

    public ItemEnchantments getItemEnchantments() {
        return this.itemEnchantments.toImmutable();
    }

    public Set<Holder<Enchantment>> getAllEnchantments() {
        return this.availableEnchantmentLevels.keySet();
    }

    public int getAvailableEnchantmentLevel(Holder<Enchantment> enchantment) {
        return this.availableEnchantmentLevels.getInt(enchantment);
    }

    private void setChanged() {
        this.markedDirty = !this.itemEnchantments.toImmutable().equals(this.getOriginalEnchantments());
    }

    private ItemEnchantments getOriginalEnchantments() {
        return EnchantmentHelper.getEnchantmentsForCrafting(this.getEnchantableStack());
    }

    public int getRequiredEnchantmentPower(Holder<Enchantment> enchantment) {
        return this.requiredEnchantmentPowers.getInt(enchantment);
    }

    public void setEnchantmentsFromServer(ItemEnchantments itemEnchantments) {
        this.itemEnchantments = new ItemEnchantments.Mutable(itemEnchantments);
        this.setChanged();
    }

    public void setInitialEnchantments(@Nullable Level level, ItemEnchantments itemEnchantments) {
        this.itemEnchantments = new ItemEnchantments.Mutable(itemEnchantments);
        this.setChanged();
        this.initializeEnchantmentMaps(level);
        this.originalEnchantingCost = this.getScaledEnchantmentCosts(itemEnchantments);
        this.sendEnchantments(itemEnchantments, true);
    }

    private void initializeEnchantmentMaps(@Nullable Level level) {
        if (level != null) {
            Collection<Holder<Enchantment>> enchantments = ModEnchantmentHelper.getEnchantmentsForItem(level.registryAccess(),
                    this.getEnchantableStack(),
                    this.tagKey,
                    !this.behavior.getConfig().allowAnvilEnchantments);
            int enchantmentValue = this.getEnchantableStack().getItem().getEnchantmentValue();
            this.availableEnchantmentLevels = EnchantmentPowerHelper.getAvailableEnchantmentLevels(this.getEnchantmentPower(),
                    enchantments,
                    this.getEnchantmentPowerLimit(),
                    enchantmentValue);
            if (level.isClientSide) {
                this.requiredEnchantmentPowers = EnchantmentPowerHelper.getRequiredEnchantmentPowers(this.getEnchantmentPower(),
                        enchantments,
                        this.getEnchantmentPowerLimit(),
                        enchantmentValue);
            }
        } else {
            this.availableEnchantmentLevels = Object2IntMaps.emptyMap();
            this.requiredEnchantmentPowers = Object2IntMaps.emptyMap();
        }
    }

    private void sendEnchantments(ItemEnchantments itemEnchantments, boolean initialize) {
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendMessage(PlayerSet.ofEntity(this.player),
                    new ClientboundInfuserEnchantmentsMessage(this.containerId, itemEnchantments, initialize));
        });
    }

    public EnchantmentValues getEnchantmentValues(Holder<Enchantment> enchantment) {
        int maxLevel = EnchantmentAdapter.get().getMaxLevel(enchantment);
        int availableLevel = this.getAvailableEnchantmentLevel(enchantment);
        int requiredEnchantmentPower = this.getRequiredEnchantmentPower(enchantment);
        return new EnchantmentValues(maxLevel, availableLevel, this.getEnchantmentPower(), requiredEnchantmentPower);
    }

    public record EnchantmentValues(int maxLevel,
                                    int availableLevel,
                                    int enchantmentPower,
                                    int requiredEnchantmentPower) {

    }
}
