package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.util.EnchantmentUtil;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    public final ServerConfig.InfuserConfig config;
    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private Map<Enchantment, Integer> enchantmentsToLevel;
    private Map<Enchantment, Integer> enchantmentsToLevelBase;
    private int enchantingBaseCost;
    private boolean enchantmentsChanged;

    public static InfuserMenu create(InfuserBlock.InfuserType type, int id, Inventory inventory) {
        return new InfuserMenu(type.menuType(), id, inventory, type.config());
    }

    private InfuserMenu(MenuType<?> menuType, int id, Inventory playerInventory, ServerConfig.InfuserConfig config) {
        this(menuType, id, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL, config);
    }

    public static InfuserMenu create(InfuserBlock.InfuserType type, int id, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        return new InfuserMenu(type.menuType(), id, inventory, container, levelAccess, type.config());
    }

    private InfuserMenu(MenuType<?> menuType, int id, Inventory inventory, Container container, ContainerLevelAccess levelAccess, ServerConfig.InfuserConfig config) {
        super(menuType, id);
        checkContainerSize(container, 1);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.config = config;
        this.addSlot(new Slot(container, 0, 8, config.allowRepairing ? 23 : 34) {

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        if (EnchantingInfuser.CONFIG.server().limitedEnchantments) {
            this.addSlot(new EnchantmentKnowledgeSlot(this.player, 1, 196, 161));
        }
        for (int k = 0; k < 4; ++k) {
            final EquipmentSlot equipmentslot = SLOT_IDS[k];
            this.addSlot(new Slot(inventory, 39 - k, 8 + 188 * (k / 2), 103 + (k % 2) * 18) {

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack p_39746_) {
                    return p_39746_.canEquip(equipmentslot, inventory.player);
                }

                @Override
                public boolean mayPickup(Player p_39744_) {
                    ItemStack itemstack = this.getItem();
                    return (itemstack.isEmpty() || p_39744_.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(p_39744_);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
                }
            });
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
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.enchantSlots.stillValid(pPlayer);
    }

    @Override
    public void slotsChanged(Container pInventory) {
        if (pInventory == this.enchantSlots) {
            this.enchantingCost.set(0);
            this.repairCost.set(0);
            ItemStack itemstack = pInventory.getItem(0);
            if (!itemstack.isEmpty() && this.mayEnchantStack(itemstack)) {
                this.levelAccess.execute((Level level, BlockPos pos) -> {
                    this.setEnchantingPower(level, pos);
                    this.setRepairCost();
                    final List<Enchantment> availableEnchantments = EnchantmentUtil.getAvailableEnchantments(this.player, itemstack, this.config.types.allowTreasure, this.config.types.allowUndiscoverable, this.config.types.allowUntradeable, this.config.types.allowCurses);
                    this.setAndSyncEnchantments(EnchantmentUtil.copyEnchantmentsToMap(itemstack, availableEnchantments));
                });
            } else {
                this.setAndSyncEnchantments(Map.of());
            }
        }
    }

    private boolean mayEnchantStack(ItemStack stack) {
        if ((stack.getItem() instanceof BookItem || stack.getItem() instanceof EnchantedBookItem) && !this.config.allowBooks) {
            return false;
        }
        return switch (this.config.allowModifyingEnchantments) {
            case ALL -> stack.isEnchantable() || stack.isEnchanted();
            case FULL_DURABILITY -> (stack.isEnchantable() || stack.isEnchanted()) && !stack.isDamaged();
            case UNENCHANTED -> stack.isEnchantable();
        };
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
        if (abstractContainerMenu == this) {
            this.levelAccess.execute((Level level, BlockPos pos) -> {
                if (i == 0) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    public int setEnchantingPower(Level level, BlockPos pos) {
        int power = this.getAvailablePower(level, pos);
        this.setEnchantingPower(power);
        return power;
    }

    public int setRepairCost() {
        int repairCost = this.calculateRepairCost();
        this.setRepairCost(repairCost);
        return repairCost;
    }

    public void setEnchantingPower(int power) {
        this.enchantingPower.set(power);
    }

    public void setRepairCost(int repairCost) {
        this.repairCost.set(repairCost);
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {

    }

    private int getAvailablePower(Level level, BlockPos pos) {
        int power = 0;
        for (BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (isValidBookShelf(level, pos, blockpos)) {
                power += level.getBlockState(pos.offset(blockpos)).getEnchantPowerBonus(level, pos.offset(blockpos));
            }
        }
        return power;
    }

    public static boolean isValidBookShelf(Level level, BlockPos center, BlockPos offset) {
        return level.getBlockState(center.offset(offset)).getEnchantPowerBonus(level, center.offset(offset)) != 0 && blockWithoutCollision(level, center.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2));
    }

    public static boolean blockWithoutCollision(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public int clickEnchantmentLevelButton(Player player, Enchantment enchantment, boolean increase) {
        final boolean incompatible = this.enchantmentsToLevel.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(e -> e != enchantment)
                .anyMatch(e -> !EnchantingInfuserAPI.getEnchantStatsProvider().isCompatibleWith(e, enchantment));
        if (incompatible) {
            EnchantingInfuser.LOGGER.warn("trying to add incompatible enchantment");
            return -1;
        }
        int enchantmentLevel = this.enchantmentsToLevel.get(enchantment) + (increase ? 1 : -1);
        if (enchantmentLevel != Mth.clamp(enchantmentLevel, 0, EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment))) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond bounds");
            return -1;
        }
        if (enchantmentLevel > this.getMaxLevel(enchantment).getSecond()) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond max allowed level");
            return -1;
        }
        this.enchantmentsToLevel.put(enchantment, enchantmentLevel);
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
                    ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos), this.calculateExperienceDelta(this.enchantmentsToLevel, this.enchantmentsToLevelBase, level.random));
                } else {
                    // don't use Player::onEnchantmentPerformed as it also reseeds enchantments seed which we have nothing to do with
                    player.giveExperienceLevels(player.getAbilities().instabuild ? 0 : -cost);
                }
                if (itemstack.getItem() instanceof BookItem) {
                    itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                    CompoundTag compoundtag = itemstack.getTag();
                    if (compoundtag != null) {
                        itemstack2.setTag(compoundtag.copy());
                    }
                }
                itemstack2 = EnchantmentUtil.setNewEnchantments(itemstack2, this.enchantmentsToLevel, this.enchantingBaseCost != 0);
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
        if (!this.config.allowRepairing) return false;
        ItemStack itemstack = this.enchantSlots.getItem(0);
        if (itemstack.isEmpty() || !itemstack.isDamaged()) {
            return false;
        }
        final double repairStep = itemstack.getMaxDamage() * this.config.repair.repairPercentageStep;
        int repairCost = (int) Math.ceil(Math.ceil(itemstack.getDamageValue() / repairStep) * this.config.repair.repairStepMultiplier);
        if (player.experienceLevel >= repairCost || player.getAbilities().instabuild) {
            this.levelAccess.execute((level, pos) -> {
                if (player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-repairCost);
                }
                ItemStack itemstack2 = itemstack.copy();
                itemstack2.setDamageValue(0);
                itemstack2.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(itemstack.getBaseRepairCost()));
                this.enchantSlots.setItem(0, itemstack2);
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
            });
        }
        return true;
    }

    public int calculateRepairCost() {
        ItemStack itemstack = this.enchantSlots.getItem(0);
        if (itemstack.isEmpty() || !itemstack.isDamaged()) {
            return 0;
        }
        final double repairStep = itemstack.getMaxDamage() * this.config.repair.repairPercentageStep;
        return (int) Math.ceil(Math.ceil(itemstack.getDamageValue() / repairStep) * this.config.repair.repairStepMultiplier);
    }

    public Pair<Optional<Integer>, Integer> getMaxLevel(Enchantment enchantment) {
        final int currentPower = this.getCurrentPower();
        final int maxPower = this.getMaxPower();
        Pair<Optional<Integer>, Integer> maxLevelSpecial = this.getSpecialMaxLevel(enchantment, currentPower, maxPower);
        if (maxLevelSpecial != null) return maxLevelSpecial;
        int minPowerByRarity = this.getMinPowerByRarity(enchantment, maxPower);
        if (currentPower < minPowerByRarity) return Pair.of(Optional.of(minPowerByRarity), 0);
        final int levelRange = (int) Math.round(maxPower * this.config.power.rarityRangeMultiplier);
        final int totalLevels = EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment) - EnchantingInfuserAPI.getEnchantStatsProvider().getMinLevel(enchantment);
        final int levelPercentile = totalLevels > 0 ? levelRange / totalLevels : 0;
        for (int i = 0; i <= totalLevels; i++) {
            final int nextPower = Math.min(maxPower, minPowerByRarity + i * levelPercentile);
            if (currentPower < nextPower) {
                return Pair.of(Optional.of(nextPower), EnchantingInfuserAPI.getEnchantStatsProvider().getMinLevel(enchantment) + i - 1);
            }
        }
        return Pair.of(Optional.empty(), EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment));
    }

    private Pair<Optional<Integer>, Integer> getSpecialMaxLevel(Enchantment enchantment, int currentPower, int maxPower) {
        double multiplier = -1.0;
        // only allow one multiplier at most as enchantments may have multiple of these properties enabled
        if (EnchantingInfuserAPI.getEnchantStatsProvider().isCurse(enchantment)) {
            multiplier = this.config.power.curseMultiplier;
        } else if (!EnchantingInfuserAPI.getEnchantStatsProvider().isDiscoverable(enchantment)) {
            multiplier = this.config.power.undiscoverableMultiplier;
        } else if (!EnchantingInfuserAPI.getEnchantStatsProvider().isTradeable(enchantment)) {
            multiplier = this.config.power.untradeableMultiplier;
        } else if (EnchantingInfuserAPI.getEnchantStatsProvider().isTreasureOnly(enchantment)) {
            multiplier = this.config.power.treasureMultiplier;
        }
        if (multiplier != -1.0) {
            final int nextPower = (int) Math.round(maxPower * multiplier);
            return currentPower < nextPower ? Pair.of(Optional.of(nextPower), 0) : Pair.of(Optional.empty(), EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment));
        }
        return null;
    }

    private int getMinPowerByRarity(Enchantment enchantment, int maxPower) {
        // get min amount of bookshelves required for this rarity type
        // round instead of int cast to be more expensive when possible
        return (int) Math.round(maxPower * switch (EnchantingInfuserAPI.getEnchantStatsProvider().getRarity(enchantment)) {
                    case COMMON -> this.config.power.commonMultiplier;
                    case UNCOMMON -> this.config.power.uncommonMultiplier;
                    case RARE -> this.config.power.rareMultiplier;
                    case VERY_RARE -> this.config.power.veryRareMultiplier;
                });
    }

    private int calculateEnchantCost() {
        this.markChanged();
        int cost = this.getScaledCosts(this.enchantmentsToLevel) - this.enchantingBaseCost;
        if (cost == 0 && this.enchantmentsChanged) cost++;
        return cost;
    }

    private int calculateExperienceDelta(Map<Enchantment, Integer> current, Map<Enchantment, Integer> base, Random random) {
        // both must have same enchantments as only level value is ever changed, all enchantments are present from start
        if (current.size() != base.size()) throw new IllegalStateException("Enchantment map size mismatch!");
        int experience = 0;
        for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (base.containsKey(enchantment)) {
                int baseLevel = base.get(enchantment);
                int currentLevel = entry.getValue();
                if (baseLevel > currentLevel) {
                    experience += Math.max(0, EnchantingInfuserAPI.getEnchantStatsProvider().getMinCost(enchantment, baseLevel) - EnchantingInfuserAPI.getEnchantStatsProvider().getMinCost(enchantment, currentLevel));
                }
            }
        }
        if (experience > 0) {
            int half = (int) Math.ceil(experience / 2.0);
            return half + random.nextInt(half);
        } else {
            return 0;
        }
    }

    private void markChanged() {
        this.enchantmentsChanged = !this.enchantmentsToLevel.equals(this.enchantmentsToLevelBase);
    }

    private int getScaledCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        final double totalCosts = this.getTotalCosts(enchantmentsToLevel);
        final int maxCost = this.config.costs.maximumCost;
        if (totalCosts > maxCost && !(this.enchantSlots.getItem(0).getItem() instanceof BookItem)) {
            final double ratio = maxCost / totalCosts;
            final int minCosts = enchantmentsToLevel.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            return Math.max((int) Math.round(this.getAllCosts(enchantmentsToLevel) * ratio), minCosts);
        }
        return this.getAllCosts(enchantmentsToLevel);
    }

    private int getTotalCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        Map<Enchantment, Pair<Enchantment.Rarity, Integer>> map = Maps.newHashMap();
        for (Enchantment enchantment : enchantmentsToLevel.keySet()) {
            if (!this.config.costs.scaleCostsByVanillaOnly || ForgeRegistries.ENCHANTMENTS.getKey(enchantment).getNamespace().equals("minecraft")) {
                final Pair<Enchantment.Rarity, Integer> pair2 = Pair.of(EnchantingInfuserAPI.getEnchantStatsProvider().getRarity(enchantment), EnchantingInfuserAPI.getEnchantStatsProvider().getMaxLevel(enchantment));
                final Optional<Map.Entry<Enchantment, Pair<Enchantment.Rarity, Integer>>> any = map.entrySet().stream()
                        .filter(e -> !EnchantingInfuserAPI.getEnchantStatsProvider().isCompatibleWith(e.getKey(), enchantment))
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

    private int getAdjustedRarityCost(Enchantment enchantment) {
        int cost = this.getRarityCost(EnchantingInfuserAPI.getEnchantStatsProvider().getRarity(enchantment));
        if (this.config.costs.doubleUniques && (EnchantingInfuserAPI.getEnchantStatsProvider().isTreasureOnly(enchantment) || !EnchantingInfuserAPI.getEnchantStatsProvider().isDiscoverable(enchantment) || !EnchantingInfuserAPI.getEnchantStatsProvider().isTradeable(enchantment)) && !EnchantingInfuserAPI.getEnchantStatsProvider().isCurse(enchantment)) {
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
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
            if (pIndex == 0) {
                if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(4 - equipmentslot.getIndex()).hasItem()) {
                    int i = 4 - equipmentslot.getIndex();
                    if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                        slot.onTake(pPlayer, itemstack1);
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(41).hasItem()) {
                    if (!this.moveItemStackTo(itemstack1, 41, 42, false)) {
                        slot.onTake(pPlayer, itemstack1);
                        return ItemStack.EMPTY;
                    }
                }
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    slot.onTake(pPlayer, itemstack1);
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem()) {
                    return ItemStack.EMPTY;
                }
                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                this.slots.get(0).set(itemstack2);
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(pPlayer, itemstack1);
        }
        return itemstack;
    }

    public int getCurrentPower() {
        return Math.min(this.enchantingPower.get(), this.getMaxPower());
    }

    public int getMaxPower() {
        return this.config.maximumPower;
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
        }
        return false;
    }

    public boolean canRepair(Player player) {
        if (!this.enchantSlots.getItem(0).isEmpty() && this.enchantSlots.getItem(0).isDamaged()) {
            return player.experienceLevel >= this.getRepairCost() || player.getAbilities().instabuild;
        }
        return false;
    }

    public Map<Enchantment, Integer> getValidEnchantments() {
        return ImmutableMap.copyOf(this.enchantmentsToLevel);
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        return this.enchantmentsToLevel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(e -> EnchantingInfuserAPI.getEnchantStatsProvider().getRarity(e.getKey()).ordinal()).thenComparing(e -> new TranslatableComponent(e.getKey().getDescriptionId()).getString()))
                .collect(Collectors.toList());
    }

    public void setAndSyncEnchantments(Map<Enchantment, Integer> enchantmentsToLevel) {
        this.enchantmentsToLevel = enchantmentsToLevel;
        this.enchantmentsToLevelBase = ImmutableMap.copyOf(enchantmentsToLevel);
        this.enchantingBaseCost = this.getScaledCosts(enchantmentsToLevel);
        this.markChanged();
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendTo(new S2CCompatibleEnchantsMessage(this.containerId, enchantmentsToLevel), (ServerPlayer) this.player);
        });
    }
}
