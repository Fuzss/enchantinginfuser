package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.api.v2.EnchantingInfuserApi;
import fuzs.enchantinginfuser.config.ModifiableItems;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.network.ClientboundInfuserEnchantmentsMessage;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.util.ModEnchantmentHelper;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.enchantinginfuser.world.level.block.InfuserType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    public final ServerConfig.InfuserConfig config;
    private final TagKey<Enchantment> availableEnchantments;
    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private ItemEnchantments.Mutable mutableEnchantments;
    private ItemEnchantments originalEnchantments;
    private int enchantingBaseCost;
    private boolean enchantmentsChanged;

    public InfuserMenu(InfuserType infuserType, int containerId, Inventory inventory) {
        this(infuserType, containerId, inventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public InfuserMenu(InfuserType infuserType, int containerId, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        super(infuserType.getMenuType(), containerId);
        checkContainerSize(container, 1);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.config = infuserType.getConfig();
        this.availableEnchantments = infuserType.getAvailableEnchantments();
        this.addSlot(new Slot(container, 0, 8, this.config.allowRepairing.isActive() ? 23 : 34) {

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = InventoryMenu.SLOT_IDS[k];
            ResourceLocation resourceLocation = InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
            this.addSlot(new ArmorSlot(inventory, inventory.player, equipmentSlot, 39 - k, 8 + 188 * (k / 2), 103 + (k % 2) * 18,
                    resourceLocation
            ));
        }
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(inventory, j1 + (l + 1) * 9, 30 + j1 * 18, 103 + l * 18));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1, 30 + i1 * 18, 161));
        }
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
            this.enchantingCost.set(0);
            this.repairCost.set(0);
            ItemStack itemStack = container.getItem(0);
            if (!itemStack.isEmpty() && this.mayEnchantStack(itemStack)) {
                this.levelAccess.execute((Level level, BlockPos pos) -> {
                    Collection<Holder<Enchantment>> enchantments = ModEnchantmentHelper.getEnchantmentsForItem(level.registryAccess(), itemStack, this.availableEnchantments, !this.config.allowAnvilEnchantments);
                    this.setAndSyncEnchantments(ModEnchantmentHelper.computeItemEnchantments(itemStack, enchantments));
                    this.enchantingPower.set(this.getAvailablePower(level, pos));
                    this.repairCost.set(this.calculateRepairCost());
                });
            } else {
                this.setAndSyncEnchantments(ItemEnchantments.EMPTY);
            }
        }
    }

    private boolean mayEnchantStack(ItemStack itemStack) {
        if (this.config.allowBooks) {
            if (itemStack.getItem() instanceof BookItem) {
                return true;
            } else if (itemStack.getItem() instanceof EnchantedBookItem) {
                return this.config.allowModifyingEnchantments != ModifiableItems.UNENCHANTED;
            }
        } else if (itemStack.getItem() instanceof BookItem || itemStack.getItem() instanceof EnchantedBookItem) {
            return false;
        }
        return this.config.allowModifyingEnchantments.predicate.test(itemStack);
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (abstractContainerMenu == this) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                if (dataSlotIndex == 0) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // NO-OP
    }

    private int getAvailablePower(Level level, BlockPos pos) {
        float enchantingPower = 0.0F;
        float maxPowerScale = 1.0F;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (InfuserBlock.isValidBookShelf(level, pos, offset)) {
                BlockState blockState = level.getBlockState(pos.offset(offset));
                enchantingPower += EnchantingInfuserApi.getEnchantStatsProvider().getEnchantPowerBonus(blockState, level, pos.offset(offset));
                maxPowerScale = Math.max(maxPowerScale, EnchantingInfuserApi.getEnchantStatsProvider().getMaximumEnchantPowerScale(blockState, level, pos.offset(offset)));
            }
        }
        // Apotheosis has bookshelves with negative enchanting power, so make sure this value doesn't go there
        return (int) Math.min(Math.max(0.0F, enchantingPower), this.config.maximumBookshelves * maxPowerScale);
    }

    public int clickEnchantmentLevelButton(Holder<Enchantment> enchantment, ServerboundEnchantmentLevelMessage.Operation operation) {
        final boolean incompatible = this.mutableEnchantments.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(e -> e != enchantment)
                .anyMatch(e -> !EnchantingInfuserApi.getEnchantStatsProvider().isCompatibleWith(e, enchantment));
        if (incompatible) {
            EnchantingInfuser.LOGGER.warn("trying to add incompatible enchantment");
            return -1;
        }
        int enchantmentLevel = this.mutableEnchantments.getLevel(enchantment) + operation.value();
        if (enchantmentLevel != Mth.clamp(enchantmentLevel, 0, EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment))) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond bounds");
            return -1;
        }
        if (enchantmentLevel > this.getMaxLevel(enchantment).getSecond()) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond max allowed level");
            return -1;
        }
        this.mutableEnchantments.set(enchantment, enchantmentLevel);
        this.enchantingCost.set(this.calculateEnchantCost());
        return enchantmentLevel;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case 0 -> this.clickEnchantButton(player);
            case 1 -> this.clickRepairButton(player);
            default -> false;
        };
    }

    private boolean clickEnchantButton(Player player) {
        ItemStack itemstack = this.enchantSlots.getItem(0);
        int cost = this.calculateEnchantCost();
        if (itemstack.isEmpty() || !this.enchantmentsChanged || player.experienceLevel < cost && !player.getAbilities().instabuild) {
            return false;
        } else {
            this.levelAccess.execute((level, pos) -> {
                ItemStack itemstack2 = itemstack;
                if (cost < 0) {
                    ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos), this.calculateExperienceDelta(this.mutableEnchantments, this.originalEnchantments, level.random));
                } else if (!player.getAbilities().instabuild) {
                    // don't use Player::onEnchantmentPerformed as it also reseeds enchantments seed which we have nothing to do with
                    player.giveExperienceLevels(-cost);
                }
                itemstack2 = ModEnchantmentHelper.setNewEnchantments(itemstack2, this.mutableEnchantments.toImmutable(), this.config.increaseAnvilRepairCost && this.enchantingBaseCost != 0);
                this.enchantSlots.setItem(0, itemstack2);
                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) player, itemstack2, cost);
                }
                this.enchantSlots.setChanged();
                this.slotsChanged(this.enchantSlots);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
    }

    private boolean clickRepairButton(Player player) {
        if (!this.config.allowRepairing.isActive()) return false;
        ItemStack itemStack = this.enchantSlots.getItem(0);
        if (!this.config.allowRepairing.isAllowedToRepair(itemStack)) return false;
        final double repairStep = itemStack.getMaxDamage() * this.config.repair.repairPercentageStep;
        int repairCostLevels = (int) Math.ceil(Math.ceil(itemStack.getDamageValue() / repairStep) * this.config.repair.repairStepMultiplier);
        if (player.experienceLevel >= repairCostLevels || player.getAbilities().instabuild) {
            this.levelAccess.execute((level, pos) -> {
                if (!player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-repairCostLevels);
                }
                int itemRepairCost = itemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
                ItemStack newItemStack = itemStack.copy();
                newItemStack.setDamageValue(0);
                if (this.config.increaseAnvilRepairCost) {
                    newItemStack.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(itemRepairCost));
                }
                this.enchantSlots.setItem(0, newItemStack);
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
            });
        }
        return true;
    }

    public int calculateRepairCost() {
        ItemStack itemstack = this.enchantSlots.getItem(0);
        if (!this.config.allowRepairing.isAllowedToRepair(itemstack)) return 0;
        final double repairStep = itemstack.getMaxDamage() * this.config.repair.repairPercentageStep;
        return (int) Math.ceil(Math.ceil(itemstack.getDamageValue() / repairStep) * this.config.repair.repairStepMultiplier);
    }

    public Pair<OptionalInt, Integer> getMaxLevel(Holder<Enchantment> enchantment) {
        final int currentPower = this.getCurrentPower();
        final int maxPower = this.getMaxPower();
        Pair<OptionalInt, Integer> maxLevelSpecial = this.getSpecialMaxLevel(enchantment, currentPower, maxPower);
        if (maxLevelSpecial != null) return maxLevelSpecial;
        int minPowerByRarity = this.getMinPowerByRarity(enchantment, maxPower);
        if (currentPower < minPowerByRarity) return Pair.of(OptionalInt.of(minPowerByRarity), 0);
        final int totalLevels = EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment) - EnchantingInfuserApi.getEnchantStatsProvider().getMinLevel(enchantment);
        double levelRange = maxPower * this.config.power.rarityRange;
        double levelPercentile = totalLevels > 0 ? levelRange / totalLevels : 0;
        for (int i = 0; i <= totalLevels; i++) {
            int nextPower = Math.min(maxPower, (int) Math.ceil(minPowerByRarity + i * levelPercentile));
            if (currentPower < nextPower) {
                return Pair.of(OptionalInt.of(nextPower), EnchantingInfuserApi.getEnchantStatsProvider().getMinLevel(enchantment) + i - 1);
            }
        }
        return Pair.of(OptionalInt.of(maxPower), EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment));
    }

    private Pair<OptionalInt, Integer> getSpecialMaxLevel(Enchantment enchantment, int currentPower, int maxPower) {
        double multiplier = -1.0;
        // only allow one multiplier at most as enchantments may have multiple of these properties enabled
        if (EnchantingInfuserApi.getEnchantStatsProvider().isCurse(enchantment)) {
            multiplier = this.config.power.curseMultiplier;
        } else if (!EnchantingInfuserApi.getEnchantStatsProvider().isDiscoverable(enchantment)) {
            multiplier = this.config.power.undiscoverableMultiplier;
        } else if (!EnchantingInfuserApi.getEnchantStatsProvider().isTradeable(enchantment)) {
            multiplier = this.config.power.untradeableMultiplier;
        } else if (EnchantingInfuserApi.getEnchantStatsProvider().isTreasureOnly(enchantment)) {
            multiplier = this.config.power.treasureMultiplier;
        }
        if (multiplier != -1.0) {
            final int nextPower = (int) Math.round(maxPower * multiplier);
            return currentPower < nextPower ? Pair.of(OptionalInt.of(nextPower), 0) : Pair.of(OptionalInt.empty(), EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment));
        }
        return null;
    }

    private int getMinPowerByRarity(Enchantment enchantment, int maxPower) {
        // get min amount of bookshelves required for this rarity type
        // round instead of int cast to be more expensive when possible
        return (int) Math.round(maxPower * switch (EnchantingInfuserApi.getEnchantStatsProvider().getRarity(enchantment)) {
                    case COMMON -> this.config.power.commonMultiplier;
                    case UNCOMMON -> this.config.power.uncommonMultiplier;
                    case RARE -> this.config.power.rareMultiplier;
                    case VERY_RARE -> this.config.power.veryRareMultiplier;
                });
    }

    private int calculateEnchantCost() {
        this.markChanged();
        int cost = this.getScaledCosts(this.mutableEnchantments) - this.enchantingBaseCost;
        if (cost == 0 && this.enchantmentsChanged) cost++;
        return cost;
    }

    private int calculateExperienceDelta(Map<Enchantment, Integer> enchantments, Map<Enchantment, Integer> originalEnchantments, RandomSource random) {
        // both must have same enchantments as only level value is ever changed, all enchantments are present from start
        if (enchantments.size() != originalEnchantments.size()) throw new IllegalStateException("Enchantment map size mismatch!");
        int experience = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (originalEnchantments.containsKey(enchantment)) {
                int originalLevel = originalEnchantments.get(enchantment);
                int currentLevel = entry.getValue();
                if (originalLevel > currentLevel) {
                    int originalMinCost = EnchantingInfuserApi.getEnchantStatsProvider().getMinCost(enchantment, originalLevel);
                    int currentMinCost = EnchantingInfuserApi.getEnchantStatsProvider().getMinCost(enchantment, currentLevel);
                    experience += Math.max(0, originalMinCost) - Math.max(0, currentMinCost);
                }
            }
        }
        if (experience > 0) {
            experience = (int) Math.ceil(experience / 2.0);
            return experience + random.nextInt(experience);
        } else {
            return 0;
        }
    }

    private void markChanged() {
        this.enchantmentsChanged = !this.mutableEnchantments.equals(this.originalEnchantments);
    }

    private int getScaledCosts(ItemEnchantments itemEnchantments) {
        final double totalCosts = this.getTotalCosts(itemEnchantments);
        final int maxCost = (int) (this.config.costs.maximumCost * EnchantingInfuserApi.getEnchantStatsProvider().getMaximumCostMultiplier());
        ItemStack itemStack = this.enchantSlots.getItem(0);
        if (totalCosts > maxCost && !ModEnchantmentHelper.isBook(itemStack)) {
            final double ratio = maxCost / totalCosts;
            final int minCosts = itemEnchantments.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
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
            boolean scaleAllCosts = !this.config.costs.scaleCostsByVanillaOnly;
            if (!scaleAllCosts) {
                String namespace = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).getNamespace();
                for (String scalingNamespace : EnchantingInfuserApi.getEnchantStatsProvider().getScalingNamespaces()) {
                    if (namespace.equals(scalingNamespace)) {
                        scaleAllCosts = true;
                        break;
                    }
                }
            }
            if (scaleAllCosts) {
                final Pair<Enchantment.Rarity, Integer> pair2 = Pair.of(EnchantingInfuserApi.getEnchantStatsProvider().getRarity(enchantment), EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment));
                final Optional<Map.Entry<Enchantment, Pair<Enchantment.Rarity, Integer>>> any = map.entrySet().stream()
                        .filter(e -> !EnchantingInfuserApi.getEnchantStatsProvider().isCompatibleWith(e.getKey(), enchantment))
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
        return map.values().stream()
                .mapToInt(e -> this.getRarityCost(e.getFirst()) * e.getSecond())
                .sum();
    }

    private Pair<Enchantment.Rarity, Integer> compareEnchantmentData(Pair<Enchantment.Rarity, Integer> pair1, Pair<Enchantment.Rarity, Integer> pair2) {
        int cost1 = this.getRarityCost(pair1.getFirst()) * pair1.getSecond();
        int cost2 = this.getRarityCost(pair2.getFirst()) * pair2.getSecond();
        return cost2 > cost1 ? pair2 : pair1;
    }

    private int getAllCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        return enchantmentsToLevel.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .mapToInt(entry -> this.getAdjustedRarityCost(entry.getKey()) * entry.getValue())
                .sum();
    }

    private int getAdjustedRarityCost(Holder<Enchantment> enchantment) {
        int cost = this.getRarityCost(EnchantingInfuserApi.getEnchantStatsProvider().getRarity(enchantment));
        if (this.config.costs.doubleUniques && enchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
            cost *= 2;
        }
        return cost;
    }

    private int getRarityCost(Enchantment.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> this.config.costs.commonCostMultiplier;
            case UNCOMMON -> this.config.costs.uncommonCostMultiplier;
            case RARE -> this.config.costs.rareCostMultiplier;
            case VERY_RARE -> this.config.costs.veryRareCostMultiplier;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemStack = itemstack1.copy();
            EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemStack);
            if (index == 0) {
                if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(4 - equipmentslot.getIndex()).hasItem()) {
                    int i = 4 - equipmentslot.getIndex();
                    if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                        slot.onTake(player, itemstack1);
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(41).hasItem()) {
                    if (!this.moveItemStackTo(itemstack1, 41, 42, false)) {
                        slot.onTake(player, itemstack1);
                        return ItemStack.EMPTY;
                    }
                }
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    slot.onTake(player, itemstack1);
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.getFirst().hasItem()) {
                    return ItemStack.EMPTY;
                }
                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                this.slots.getFirst().set(itemstack2);
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
        }

        return itemStack;
    }

    public int getCurrentPower() {
        return Math.min(this.enchantingPower.get(), this.getMaxPower());
    }

    public int getMaxPower() {
        int maximumEnchantPower = EnchantingInfuserApi.getEnchantStatsProvider().getMaximumEnchantPower();
        if (maximumEnchantPower != -1) return maximumEnchantPower;
        return this.config.maximumBookshelves;
    }

    public ItemStack getEnchantableStack() {
        return this.enchantSlots.getItem(0);
    }

    public int getEnchantCost() {
        return this.enchantingCost.get();
    }

    public int getRepairCost() {
        return this.repairCost.get();
    }

    public boolean canEnchant(Player player) {
        if (!this.enchantSlots.getItem(0).isEmpty() && this.enchantmentsChanged) {
            return player.experienceLevel >= this.getEnchantCost() || player.getAbilities().instabuild;
        } else {
            return false;
        }
    }

    public boolean canRepair(Player player) {
        if (this.config.allowRepairing.isAllowedToRepair(this.enchantSlots.getItem(0))) {
            return player.experienceLevel >= this.getRepairCost() || player.getAbilities().instabuild;
        } else {
            return false;
        }
    }

    public ItemEnchantments getSelectedEnchantments() {
        return this.mutableEnchantments.toImmutable();
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        return this.mutableEnchantments.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(e -> EnchantingInfuserApi.getEnchantStatsProvider().getRarity(e.getKey()).ordinal()).thenComparing(e -> Component.translatable(e.getKey().getDescriptionId()).getString()))
                .collect(Collectors.toList());
    }

    public void setAndSyncEnchantments(ItemEnchantments itemEnchantments) {
        this.mutableEnchantments = new ItemEnchantments.Mutable(itemEnchantments);
        this.originalEnchantments = itemEnchantments;
        this.enchantingBaseCost = this.getScaledCosts(itemEnchantments);
        this.markChanged();
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendTo((ServerPlayer) this.player, new ClientboundInfuserEnchantmentsMessage(this.containerId, itemEnchantments));
        });
    }
}
