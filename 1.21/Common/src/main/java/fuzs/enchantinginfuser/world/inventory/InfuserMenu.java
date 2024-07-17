package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ModifiableItems;
import fuzs.enchantinginfuser.network.ClientboundInfuserEnchantmentsMessage;
import fuzs.enchantinginfuser.util.EnchantmentPowerHelper;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import fuzs.enchantinginfuser.util.PlayerExperienceHelper;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantmentAdapter;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    public static final int ENCHANT_BUTTON = 0;
    public static final int REPAIR_BUTTON = 1;
    public static final int ENCHANT_ITEM_SLOT = 0;

    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    public final EnchantingBehavior behavior;
    private final TagKey<Enchantment> tagKey;
    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private ItemEnchantments.Mutable itemEnchantments;
    private Object2IntMap<Holder<Enchantment>> maximumEnchantmentLevels;
    private Object2IntMap<Holder<Enchantment>> requiredEnchantmentPowers;
    private int enchantingBaseCost;
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
            this.addSlot(new ArmorSlot(inventory, inventory.player, equipmentSlot, 39 - k, 8 + 188 * (k / 2),
                    103 + (k % 2) * 18, resourceLocation
            ));
        }
        ContainerMenuHelper.addInventorySlots(this, inventory, 30, 103);
        this.addSlot(new Slot(inventory, 40, 8, 161) {

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
        this.addDataSlot(this.enchantingPower);
        this.addDataSlot(this.enchantingCost);
        this.addDataSlot(this.repairCost);
        this.addSlotListener(this);
        this.levelAccess.execute((Level level, BlockPos pos) -> {
            this.enchantingPower.set(this.getAvailablePower(level, pos));
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            this.setInitialEnchantments(null, ItemEnchantments.EMPTY);
            this.enchantingCost.set(0);
            this.repairCost.set(0);
            if (this.mayEnchantStack(this.getEnchantableStack())) {
                this.levelAccess.execute((Level level, BlockPos pos) -> {
                    this.setInitialEnchantments(level, this.getOriginalEnchantments());
                    this.enchantingPower.set(this.getAvailablePower(level, pos));
                    this.repairCost.set(this.calculateRepairCost());
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
            if (InfuserBlock.isValidBookShelf(level, pos, offset)) {
                BlockState blockState = level.getBlockState(pos.offset(offset));
                enchantingPower += this.behavior.getEnchantmentPower(blockState, level, pos.offset(offset));
                maxPowerScale = Math.max(maxPowerScale,
                        this.behavior.getEnchantmentPowerLimitScale(blockState, level, pos.offset(offset))
                );
            }
        }
        // Apotheosis has bookshelves with negative enchanting power, so make sure this value doesn't go there
        return (int) Math.min(Math.max(0.0F, enchantingPower),
                this.behavior.getConfig().maximumBookshelves * maxPowerScale
        );
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (containerMenu == this) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                if (dataSlotIndex == ENCHANT_ITEM_SLOT) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // NO-OP
    }

    public int clickEnchantmentLevelButton(Holder<Enchantment> enchantment, IntSupplier operation) {
        int enchantmentLevel = this.itemEnchantments.getLevel(enchantment) + operation.getAsInt();
        int newEnchantmentLevel = enchantmentLevel + operation.getAsInt();
        if (EnchantmentHelper.isEnchantmentCompatible(this.itemEnchantments.keySet(), enchantment)) {
            return enchantmentLevel;
        } else if (newEnchantmentLevel != Mth.clamp(newEnchantmentLevel, 0,
                EnchantmentAdapter.get().getMaxLevel(enchantment)
        )) {
            return enchantmentLevel;
        } else if (newEnchantmentLevel > this.getMaximumEnchantmentLevel(enchantment)) {
            return enchantmentLevel;
        } else {
            this.itemEnchantments.set(enchantment, newEnchantmentLevel);
            this.setChanged();
            this.sendEnchantments(this.itemEnchantments.toImmutable(), false);
            this.enchantingCost.set(this.calculateEnchantCost());
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
                this.processEnchantingCost(player, level, pos, this.getEnchantCost());
                ItemStack itemStack = ModEnchantmentHelper.setNewEnchantments(this.getEnchantableStack(),
                        this.itemEnchantments.toImmutable(),
                        this.behavior.getConfig().increaseAnvilRepairCost && this.enchantingBaseCost != 0
                );
                this.enchantSlots.setItem(ENCHANT_ITEM_SLOT, itemStack);
                if (this.getEnchantCost() > 0) {
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) player, itemStack,
                                this.getEnchantCost()
                        );
                    }
                }
                this.enchantSlots.setChanged();
                this.slotsChanged(this.enchantSlots);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F,
                        level.random.nextFloat() * 0.1F + 0.9F
                );
            });

            return true;
        } else {

            return false;
        }
    }

    private void processEnchantingCost(Player player, Level level, BlockPos pos, int enchantingCost) {
        if (enchantingCost < 0) {
            ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos),
                    PlayerExperienceHelper.calculateExperienceDelta(this.itemEnchantments.toImmutable(),
                            this.getOriginalEnchantments(), level.random
                    )
            );
        } else if (!player.getAbilities().instabuild) {
            // don't use Player::onEnchantmentPerformed as it also reseeds enchantments seed which we have nothing to do with
            player.giveExperienceLevels(-enchantingCost);
        }
    }

    private int calculateEnchantCost() {
        // TODO here begins the overhaul
        int cost = this.getScaledCosts(this.itemEnchantments.toImmutable()) - this.enchantingBaseCost;
        if (cost == 0 && this.markedDirty) cost++;
        return cost;
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
            return (int) Math.ceil(Math.ceil(
                    itemStack.getDamageValue() / repairStep) * this.behavior.getConfig().repair.repairStepMultiplier);
        } else {
            return 0;
        }
    }

    private int getScaledCosts(ItemEnchantments itemEnchantments) {
        final double totalCosts = this.getTotalCosts(itemEnchantments);
        final int maxCost = (int) (this.behavior.getConfig().costs.maximumCost * this.behavior.getMaximumCostMultiplier());
        ItemStack itemStack = this.getEnchantableStack();
        if (totalCosts > maxCost && !ModEnchantmentHelper.isBook(itemStack)) {
            final double ratio = maxCost / totalCosts;
            final int minCosts = itemEnchantments.values().stream().mapToInt(Integer::intValue).sum();
            return Math.max((int) Math.round(this.getAllCosts(itemEnchantments) * ratio), minCosts);
        } else {
            return this.getAllCosts(itemEnchantments);
        }
    }

    private int getTotalCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        Map<Enchantment, Pair<Enchantment.Rarity, Integer>> map = Maps.newHashMap();
        for (Enchantment enchantment : enchantmentsToLevel.keySet()) {
            boolean scaleAllCosts = !this.behavior.getConfig().costs.scaleCostsByVanillaOnly;
            if (!scaleAllCosts) {
                String namespace = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).getNamespace();
                for (String scalingNamespace : this.behavior.getScalingNamespaces()) {
                    if (namespace.equals(scalingNamespace)) {
                        scaleAllCosts = true;
                        break;
                    }
                }
            }
            if (scaleAllCosts) {
                final Pair<Enchantment.Rarity, Integer> pair2 = Pair.of(EnchantmentAdapter.get().getWeight(enchantment),
                        EnchantmentAdapter.get().getMaxLevel(enchantment)
                );
                final Optional<Map.Entry<Enchantment, Pair<Enchantment.Rarity, Integer>>> any = map.entrySet()
                        .stream()
                        .filter(e -> !EnchantmentAdapter.get().areCompatible(e.getKey(), enchantment))
                        .findAny();
                if (any.isPresent()) {
                    final Map.Entry<Enchantment, Pair<Enchantment.Rarity, Integer>> enchantmentData = any.get();
                    final Pair<Enchantment.Rarity, Integer> pair1 = enchantmentData.getValue();
                    map.put(enchantmentData.getKey(), this.compareEnchantmentData(pair1, pair2));
                } else {
                    map.put(enchantment, pair2);
                }
            }
        }
        return map.values().stream().mapToInt(e -> this.getRarityCost(e.getFirst()) * e.getSecond()).sum();
    }

    private Pair<Enchantment.Rarity, Integer> compareEnchantmentData(Pair<Enchantment.Rarity, Integer> pair1, Pair<Enchantment.Rarity, Integer> pair2) {
        int cost1 = this.getRarityCost(pair1.getFirst()) * pair1.getSecond();
        int cost2 = this.getRarityCost(pair2.getFirst()) * pair2.getSecond();
        return cost2 > cost1 ? pair2 : pair1;
    }

    private int getAllCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        return enchantmentsToLevel.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .mapToInt(entry -> this.getAdjustedRarityCost(entry.getKey()) * entry.getValue())
                .sum();
    }

    private int getAdjustedRarityCost(Holder<Enchantment> enchantment) {
        int cost = this.getRarityCost(EnchantmentAdapter.get().getWeight(enchantment));
        if (this.behavior.getConfig().costs.doubleUniques && enchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
            cost *= 2;
        }
        return cost;
    }

    private int getRarityCost(Enchantment.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> this.behavior.getConfig().costs.commonCostMultiplier;
            case UNCOMMON -> this.behavior.getConfig().costs.uncommonCostMultiplier;
            case RARE -> this.behavior.getConfig().costs.rareCostMultiplier;
            case VERY_RARE -> this.behavior.getConfig().costs.veryRareCostMultiplier;
        };
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
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(
                        4 - equipmentSlot.getIndex()).hasItem()) {
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
        return Math.min(this.enchantingPower.get(), this.getEnchantmentPowerLimit());
    }

    public int getEnchantmentPowerLimit() {
        return this.behavior.getEnchantmentPowerLimit();
    }

    public ItemStack getEnchantableStack() {
        return this.enchantSlots.getItem(ENCHANT_ITEM_SLOT);
    }

    public int getEnchantCost() {
        return this.enchantingCost.get();
    }

    public int getRepairCost() {
        return this.repairCost.get();
    }

    public boolean canEnchant(Player player) {
        if (!this.getEnchantableStack().isEmpty() && this.markedDirty) {
            return player.experienceLevel >= this.getEnchantCost() || player.getAbilities().instabuild;
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
        return this.maximumEnchantmentLevels.keySet();
    }

    public int getMaximumEnchantmentLevel(Holder<Enchantment> enchantment) {
        return this.maximumEnchantmentLevels.getInt(enchantment);
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
        this.enchantingBaseCost = this.getScaledCosts(itemEnchantments);
        this.setChanged();
        this.initializeEnchantmentMaps(level);
        if (!itemEnchantments.isEmpty()) {
            this.sendEnchantments(itemEnchantments, true);
        }
    }

    private void initializeEnchantmentMaps(@Nullable Level level) {
        if (level != null) {
            Collection<Holder<Enchantment>> enchantments = ModEnchantmentHelper.getEnchantmentsForItem(
                    level.registryAccess(), this.getEnchantableStack(), this.tagKey,
                    !this.behavior.getConfig().allowAnvilEnchantments
            );
            this.maximumEnchantmentLevels = EnchantmentPowerHelper.getMaximumEnchantmentLevels(
                    this.getEnchantmentPower(), enchantments, this.getEnchantmentPowerLimit(),
                    this.getEnchantableStack().getItem().getEnchantmentValue()
            );
            if (level.isClientSide) {
                this.requiredEnchantmentPowers = EnchantmentPowerHelper.getRequiredEnchantmentPowers(
                        this.getEnchantmentPower(), enchantments, this.getEnchantmentPowerLimit(),
                        this.getEnchantableStack().getItem().getEnchantmentValue()
                );
            }
        } else {
            this.maximumEnchantmentLevels = Object2IntMaps.emptyMap();
            this.requiredEnchantmentPowers = Object2IntMaps.emptyMap();
        }
    }

    private void sendEnchantments(ItemEnchantments itemEnchantments, boolean initialize) {
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendTo((ServerPlayer) this.player,
                    new ClientboundInfuserEnchantmentsMessage(this.containerId, itemEnchantments, initialize)
            );
        });
    }
}
