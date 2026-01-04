package fuzs.enchantinginfuser.world.inventory;

import fuzs.enchantinginfuser.config.ModifiableItems;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.network.ClientboundInfuserEnchantmentsMessage;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.util.EnchantmentCostHelper;
import fuzs.enchantinginfuser.util.EnchantmentPowerHelper;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import fuzs.enchantinginfuser.util.PlayerExperienceHelper;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.container.v1.QuickMoveRuleSet;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    public static final int ENCHANT_BUTTON = 0;
    public static final int REPAIR_BUTTON = 1;
    public static final int ENCHANT_ITEM_SLOT = 0;
    public static final int ENCHANTMENT_POWER_DATA_SLOT = 0;
    public static final int ENCHANTING_COST_DATA_SLOT = 1;
    public static final int REPAIR_COST_DATA_SLOT = 2;

    private final InfuserType type;
    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    private final DataSlot enchantmentPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private Object2IntMap<Holder<Enchantment>> enchantmentLevels = Object2IntMaps.emptyMap();
    private Object2IntMap<Holder<Enchantment>> availableEnchantmentLevels = Object2IntMaps.emptyMap();
    private Object2IntMap<Holder<Enchantment>> requiredEnchantmentPowers = Object2IntMaps.emptyMap();
    private int originalEnchantingCost;
    private boolean markedDirty;

    public InfuserMenu(int containerId, Inventory inventory, InfuserType type) {
        this(type, containerId, inventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public InfuserMenu(InfuserType type, int containerId, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        super(ModRegistry.INFUSING_MENU_TYPE.value(), containerId);
        checkContainerSize(container, 1);
        this.type = type;
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.addSlot(new Slot(container, 0, 8, this.getConfig().allowRepairing.isActive() ? 23 : 34) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = InventoryMenu.SLOT_IDS[k];
            Identifier identifier = InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
            this.addSlot(new ArmorSlot(inventory,
                    inventory.player,
                    equipmentSlot,
                    39 - k,
                    8 + 188 * (k / 2),
                    103 + (k % 2) * 18,
                    identifier));
        }

        this.addStandardInventorySlots(inventory, 30, 103);
        this.addSlot(new Slot(inventory, Inventory.SLOT_OFFHAND, 8, 161) {
            @Override
            public void setByPlayer(ItemStack newItemStack, ItemStack oldItemStack) {
                inventory.player.onEquipItem(EquipmentSlot.OFFHAND, oldItemStack, newItemStack);
                super.setByPlayer(newItemStack, oldItemStack);
            }

            @Override
            public Identifier getNoItemIcon() {
                return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            }
        });
        this.addDataSlot(this.enchantmentPower);
        this.addDataSlot(this.enchantingCost);
        this.addDataSlot(this.repairCost);
    }

    public ServerConfig.InfuserConfig getConfig() {
        return this.type.getConfig();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        super.setItem(slotId, stateId, stack);
        // need this here to update client screen listener when receiving data changes via ClientboundContainerSetDataPacket
        this.broadcastChanges();
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        // need this here to update client screen listener when receiving data changes via ClientboundContainerSetDataPacket
        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        // only called on the server anyway as the slot listener only works there
        if (container == this.enchantSlots) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                this.enchantmentPower.set(this.getAvailablePower(level, pos));
                if (this.mayEnchantStack(this.getEnchantableStack())) {
                    this.setInitialEnchantments(level, Optional.of(this.getOriginalEnchantments()));
                    this.enchantingCost.set(this.calculateEnchantingCost());
                    this.repairCost.set(this.calculateRepairCost());
                } else {
                    this.setInitialEnchantments(level, Optional.empty());
                    this.enchantingCost.set(0);
                    this.repairCost.set(0);
                }
            });
        }
        super.slotsChanged(container);
    }

    private boolean mayEnchantStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else if (this.getConfig().allowBooks) {
            if (itemStack.is(Items.BOOK)) {
                return true;
            } else if (itemStack.is(Items.ENCHANTED_BOOK)) {
                return this.getConfig().allowModifyingEnchantments != ModifiableItems.UNENCHANTED;
            }
        } else if (ModEnchantmentHelper.isBook(itemStack)) {
            return false;
        }

        return this.getConfig().allowModifyingEnchantments.predicate.test(itemStack);
    }

    private int getAvailablePower(Level level, BlockPos pos) {
        float enchantingPower = 0.0F;
        float maxPowerScale = 1.0F;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (InfuserBlock.isValidBookShelf(level, pos, offset)) {
                BlockState blockState = level.getBlockState(pos.offset(offset));
                enchantingPower += EnchantingBehavior.get().getEnchantmentPower(blockState, level, pos.offset(offset));
                maxPowerScale = Math.max(maxPowerScale,
                        EnchantingBehavior.get().getEnchantmentPowerLimitScale(blockState, level, pos.offset(offset)));
            }
        }
        // Apotheosis has bookshelves with negative enchanting power, so make sure this value doesn't go there
        return (int) Math.min(Math.max(0.0F, enchantingPower), this.getConfig().maximumBookshelves * maxPowerScale);
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
            MessageSender.broadcast(new ServerboundEnchantmentLevelMessage(this.containerId, enchantment, operation));
            return true;
        } else {
            return false;
        }
    }

    public int clickEnchantmentLevelButton(Holder<Enchantment> enchantment, IntUnaryOperator operation) {
        // the enchantment is newly added and is not compatible with existing enchantments, so no level is allowed
        int enchantmentLevel = this.enchantmentLevels.getInt(enchantment);
        if (enchantmentLevel == 0 && !EnchantmentHelper.isEnchantmentCompatible(this.getItemEnchantments().keySet(),
                enchantment)) {
            return 0;
        } else {
            int newEnchantmentLevel = operation.applyAsInt(enchantmentLevel);
            // the new enchantment level exceeds min & max enchantment bounds
            newEnchantmentLevel = Mth.clamp(newEnchantmentLevel, 0, EnchantingBehavior.get().getMaxLevel(enchantment));
            // the new enchantment level exceeds max possible level for the available enchantment power,
            // based on the current block configuration
            newEnchantmentLevel = Math.min(newEnchantmentLevel, this.getAvailableEnchantmentLevel(enchantment));
            if (newEnchantmentLevel != enchantmentLevel) {
                this.enchantmentLevels.put(enchantment, newEnchantmentLevel);
                this.markedDirty = !this.getItemEnchantments().equals(this.getOriginalEnchantments());
                this.enchantingCost.set(this.calculateEnchantingCost());
                this.broadcastChanges();
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
                        this.enchantmentLevels,
                        this.getConfig().increaseAnvilRepairCost);
                this.enchantSlots.setItem(ENCHANT_ITEM_SLOT, itemStack);
                if (this.getEnchantingCost() > 0) {
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, itemStack, this.getEnchantingCost());
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

    private void processEnchantingCost(Player player, Level level, BlockPos blockPos, int enchantingCost) {
        if (enchantingCost < 0) {
            int amount = PlayerExperienceHelper.calculateExperienceDelta(this.getItemEnchantments(),
                    this.getOriginalEnchantments(),
                    level.random);
            ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(blockPos.above()), amount);
        } else if (!player.getAbilities().instabuild) {
            // don't use Player::onEnchantmentPerformed as it also resets enchantments seed which we have nothing to do with
            player.giveExperienceLevels(-enchantingCost);
        }
    }

    private int calculateEnchantingCost() {
        int enchantmentCosts = this.getScaledEnchantmentCosts(this.getItemEnchantments()) - this.originalEnchantingCost;
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
                if (this.getConfig().increaseAnvilRepairCost) {
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
        if (this.getConfig().allowRepairing.canRepair(itemStack)) {
            double repairStep = itemStack.getMaxDamage() * this.getConfig().repair.repairPercentageStep;
            return (int) Math.ceil(
                    Math.ceil(itemStack.getDamageValue() / repairStep) * this.getConfig().repair.repairStepMultiplier);
        } else {
            return 0;
        }
    }

    private int getScaledEnchantmentCosts(ItemEnchantments itemEnchantments) {
        float enchantmentCostsScale;
        if (ModEnchantmentHelper.isBook(this.getEnchantableStack())) {
            enchantmentCostsScale = 1.0F;
        } else {
            int scalingEnchantmentCosts = EnchantmentCostHelper.getScalingEnchantmentCosts(this.availableEnchantmentLevels.keySet(),
                    this.getScalingNamespaces());
            int maximumCost = Mth.ceil(
                    this.getConfig().costs.maximumCost * EnchantingBehavior.get().getMaximumCostMultiplier());
            enchantmentCostsScale = Math.min(1.0F, maximumCost / (float) scalingEnchantmentCosts);
        }
        float enchantmentCosts = EnchantmentCostHelper.getEnchantmentCosts(itemEnchantments, enchantmentCostsScale);
        int minimumCosts = itemEnchantments.entrySet().stream().mapToInt(Object2IntMap.Entry::getIntValue).sum();
        return Math.max(Mth.ceil(enchantmentCosts), minimumCosts);
    }

    private Collection<String> getScalingNamespaces() {
        if (this.getConfig().costs.scaleCostsByVanillaOnly) {
            return EnchantingBehavior.get().getScalingNamespaces();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return QuickMoveRuleSet.of(this, this::moveItemStackTo).addContainerSlotRule(0, (Slot slot) -> {
            return this.type.getConfig().allowModifyingEnchantments.predicate.test(slot.getItem());
        }).addInventoryRules().addInventoryCompartmentRules().quickMoveStack(player, index);
    }

    public int getEnchantmentPower() {
        return Math.min(this.enchantmentPower.get(), this.getEnchantmentPowerLimit());
    }

    public int getEnchantmentPowerLimit() {
        return EnchantingBehavior.get().getEnchantmentPowerLimit(this.type);
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
        if (this.getConfig().allowRepairing.canRepair(this.getEnchantableStack())) {
            return player.experienceLevel >= this.getRepairCost() || player.getAbilities().instabuild;
        } else {
            return false;
        }
    }

    public ItemEnchantments getItemEnchantments() {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.enchantmentLevels.object2IntEntrySet()) {
            itemEnchantments.set(entry.getKey(), entry.getIntValue());
        }
        return itemEnchantments.toImmutable();
    }

    public Set<Holder<Enchantment>> getAllEnchantments() {
        return this.availableEnchantmentLevels.keySet();
    }

    public int getAvailableEnchantmentLevel(Holder<Enchantment> enchantment) {
        return this.availableEnchantmentLevels.getInt(enchantment);
    }

    private ItemEnchantments getOriginalEnchantments() {
        return EnchantmentHelper.getEnchantmentsForCrafting(this.getEnchantableStack());
    }

    private int getRequiredEnchantmentPower(Holder<Enchantment> enchantment) {
        return this.requiredEnchantmentPowers.getInt(enchantment);
    }

    public void setInitialEnchantments(Level level, Optional<ItemEnchantments> itemEnchantments) {
        this.setItemEnchantments(itemEnchantments.orElse(ItemEnchantments.EMPTY));
        if (itemEnchantments.isPresent()) {
            this.initializeEnchantmentMaps(level);
            this.originalEnchantingCost = this.getScaledEnchantmentCosts(itemEnchantments.get());
        } else {
            this.availableEnchantmentLevels = Object2IntMaps.emptyMap();
            this.requiredEnchantmentPowers = Object2IntMaps.emptyMap();
            this.originalEnchantingCost = 0;
        }
        this.sendEnchantments(itemEnchantments);
    }

    private void setItemEnchantments(ItemEnchantments itemEnchantments) {
        Object2IntOpenHashMap<Holder<Enchantment>> enchantmentLevels = new Object2IntOpenHashMap<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            enchantmentLevels.put(entry.getKey(), entry.getIntValue());
        }
        this.enchantmentLevels = enchantmentLevels;
        this.markedDirty = false;
    }

    private void initializeEnchantmentMaps(Level level) {
        Collection<Holder<Enchantment>> enchantments = ModEnchantmentHelper.getEnchantmentsForItem(level.registryAccess(),
                this.getEnchantableStack(),
                this.type.getAvailableEnchantments(),
                !this.getConfig().allowAnvilEnchantments);
        int enchantmentValue = this.getEnchantableStack().has(DataComponents.ENCHANTABLE) ?
                this.getEnchantableStack().get(DataComponents.ENCHANTABLE).value() : 0;
        this.availableEnchantmentLevels = EnchantmentPowerHelper.getAvailableEnchantmentLevels(this.getEnchantmentPower(),
                enchantments,
                this.getEnchantmentPowerLimit(),
                enchantmentValue);
        if (level.isClientSide()) {
            this.requiredEnchantmentPowers = EnchantmentPowerHelper.getRequiredEnchantmentPowers(this.getEnchantmentPower(),
                    enchantments,
                    this.getEnchantmentPowerLimit(),
                    enchantmentValue);
        }
    }

    private void sendEnchantments(Optional<ItemEnchantments> itemEnchantments) {
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            MessageSender.broadcast(PlayerSet.ofEntity(this.player),
                    new ClientboundInfuserEnchantmentsMessage(this.containerId, itemEnchantments));
        });
    }

    public EnchantmentValues getEnchantmentValues(Holder<Enchantment> enchantment) {
        int maxLevel = EnchantingBehavior.get().getMaxLevel(enchantment);
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
