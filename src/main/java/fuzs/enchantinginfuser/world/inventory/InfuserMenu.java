package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.enchantinginfuser.util.EnchantmentUtil;
import fuzs.enchantinginfuser.util.ExperienceUtil;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    private InfuserBlock.InfuserType infuserType;
    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();
    private Map<Enchantment, Integer> enchantmentsToLevel;
    private Map<Enchantment, Integer> enchantmentsToLevelBase;
    private int enchantingBaseCost;
    private boolean enchantmentsChanged;

    public InfuserMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL, InfuserBlock.InfuserType.NORMAL);
    }

    public InfuserMenu(int id, Inventory inventory, Container container, ContainerLevelAccess levelAccess, InfuserBlock.InfuserType type) {
        super(ModRegistry.INFUSING_MENU_TYPE.get(), id);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.infuserType = type;
        this.addSlot(new Slot(container, 0, 8, 23) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
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
                    final List<Enchantment> availableEnchantments = EnchantmentUtil.getAvailableEnchantments(itemstack, EnchantingInfuser.CONFIG.server().types.treasure, EnchantingInfuser.CONFIG.server().types.undiscoverable, EnchantingInfuser.CONFIG.server().types.curses);
                    this.setAndSyncEnchantments(EnchantmentUtil.copyEnchantmentsToMap(itemstack, availableEnchantments));
                });
            } else {
                this.setAndSyncEnchantments(Map.of());
            }
        }
    }

    private boolean mayEnchantStack(ItemStack stack) {
        return switch (EnchantingInfuser.CONFIG.server().allowedItems) {
            case UNENCHANTED -> stack.isEnchantable();
            case FULLY_REPAIRED -> stack.isEnchantable() || stack.isEnchanted() && !stack.isDamaged();
            case ALL -> stack.isEnchantable() || stack.isEnchanted();
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
        this.enchantingPower.set(power);
        return power;
    }

    public void setEnchantingPower(int power) {
        this.enchantingPower.set(power);
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {

    }

    private int getAvailablePower(Level world, BlockPos pos) {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && isBlockEmpty(world, pos.offset(l, 0, k)) && isBlockEmpty(world, pos.offset(l, 1, k))) {
                    for (int i = -1; i <= 2; i++) {
                        power += this.getBlockPower(world, pos.offset(l * 2, i, k * 2));
                        if (l != 0 && k != 0) {
                            power += this.getBlockPower(world, pos.offset(l * 2, i, k));
                            power += this.getBlockPower(world, pos.offset(l, i, k * 2));
                        }
                    }
                }
            }
        }
        return power;
    }

    private float getBlockPower(Level world, BlockPos pos) {
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    public int clickEnchantmentButton(Player player, Enchantment enchantment, boolean increase) {
        final boolean incompatible = this.enchantmentsToLevel.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(e -> e != enchantment)
                .anyMatch(e -> !e.isCompatibleWith(enchantment));
        if (incompatible) {
            EnchantingInfuser.LOGGER.warn("trying to add incompatible enchantment");
            return -1;
        }
        int level = this.enchantmentsToLevel.get(enchantment) + (increase ? 1 : -1);
        if (level != Mth.clamp(level, 0, enchantment.getMaxLevel())) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond bounds");
            return -1;
        }
        if (level > this.getMaxLevel(enchantment).getSecond()) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond max allowed level");
            return -1;
        }
        this.enchantmentsToLevel.put(enchantment, level);
        this.enchantingCost.set(this.calculateCosts(player));
        return level;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            ItemStack itemstack = this.enchantSlots.getItem(0);
            final int costs = this.calculateCosts(player);
            if (itemstack.isEmpty() || !this.enchantmentsChanged || player.experienceLevel < costs && !player.getAbilities().instabuild) {
                return false;
            } else {
                this.levelAccess.execute((level, pos) -> {
                    ItemStack itemstack2 = itemstack;
                    player.onEnchantmentPerformed(itemstack, player.getAbilities().instabuild ? 0 : costs);
                    if (itemstack.getItem() instanceof BookItem) {
                        itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundTag compoundtag = itemstack.getTag();
                        if (compoundtag != null) {
                            itemstack2.setTag(compoundtag.copy());
                        }
                    }
                    itemstack2 = EnchantmentUtil.setNewEnchantments(itemstack2, this.enchantmentsToLevel);
                    this.enchantSlots.setItem(0, itemstack2);
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemstack2, costs);
                    }
                    this.enchantSlots.setChanged();
                    this.slotsChanged(this.enchantSlots);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                });
                return true;
            }
        } else if (id == 1) {
            ItemStack itemstack = this.enchantSlots.getItem(0);
            if (itemstack.isEmpty() || !itemstack.isDamaged()) {
                return false;
            } else {
                final double repairStep = itemstack.getMaxDamage() * EnchantingInfuser.CONFIG.server().repair.repairPercentageStep;
                int repairCost = (int) Math.ceil(Math.ceil(itemstack.getDamageValue() / repairStep) * EnchantingInfuser.CONFIG.server().repair.repairStepMultiplier);
                if (player.experienceLevel >= repairCost || player.getAbilities().instabuild) {
                    this.levelAccess.execute((level, pos) -> {
                        if (player.getAbilities().instabuild) {
                            player.giveExperienceLevels(-repairCost);
                        }
                        ItemStack itemstack2 = itemstack.copy();
                        itemstack2.setDamageValue(0);
                        this.enchantSlots.setItem(0, itemstack2);
                        level.levelEvent(1030, pos, 0);
                    });
                }
            }
        }
        return false;
    }

    public Pair<Optional<Integer>, Integer> getMaxLevel(Enchantment enchantment) {
        final int currentPower = this.getCurrentPower();
        final int maxPower = this.getMaxPower();
        Pair<Optional<Integer>, Integer> maxLevelSpecial = this.getSpecialMaxLevel(enchantment, currentPower, maxPower);
        if (maxLevelSpecial != null) return maxLevelSpecial;
        int minPowerByRarity = this.getMinPowerByRarity(enchantment, maxPower);
        if (currentPower < minPowerByRarity) return Pair.of(Optional.of(minPowerByRarity), 0);
        final int levelRange = (int) Math.round(maxPower * EnchantingInfuser.CONFIG.server().power.levelMultiplier);
        final int totalLevels = enchantment.getMaxLevel() - enchantment.getMinLevel();
        final int levelPercentile = totalLevels > 0 ? levelRange / totalLevels : 0;
        for (int i = 0; i <= totalLevels; i++) {
            final int nextPower = Math.min(maxPower, minPowerByRarity + i * levelPercentile);
            if (currentPower < nextPower) {
                return Pair.of(Optional.of(nextPower), enchantment.getMinLevel() + i - 1);
            }
        }
        return Pair.of(Optional.empty(), enchantment.getMaxLevel());
    }

    private Pair<Optional<Integer>, Integer> getSpecialMaxLevel(Enchantment enchantment, int currentPower, int maxPower) {
        double multiplier = -1.0;
        if (enchantment.isCurse()) {
            multiplier = EnchantingInfuser.CONFIG.server().power.curseMultiplier;
        } else if (!enchantment.isDiscoverable()) {
            multiplier = EnchantingInfuser.CONFIG.server().power.undiscoverableMultiplier;
        } else if (enchantment.isTreasureOnly()) {
            multiplier = EnchantingInfuser.CONFIG.server().power.treasureMultiplier;
        }
        if (multiplier != -1.0) {
            final int nextPower = (int) Math.round(maxPower * multiplier);
            return currentPower < nextPower ? Pair.of(Optional.of(nextPower), 0) : Pair.of(Optional.empty(), enchantment.getMaxLevel());
        }
        return null;
    }

    private int getMinPowerByRarity(Enchantment enchantment, int maxPower) {
        // get min amount of bookshelves required for this rarity type
        // round instead of int cast to be more expensive when possible
        return (int) Math.round(maxPower * switch (enchantment.getRarity()) {
                    case COMMON -> EnchantingInfuser.CONFIG.server().power.commonMultiplier;
                    case UNCOMMON -> EnchantingInfuser.CONFIG.server().power.uncommonMultiplier;
                    case RARE -> EnchantingInfuser.CONFIG.server().power.rareMultiplier;
                    case VERY_RARE -> EnchantingInfuser.CONFIG.server().power.veryRareMultiplier;
                });
    }

    private int calculateCosts(Player player) {
        this.markChanged();
        int scaledCosts = ExperienceUtil.convertLevelsToExperience(this.getScaledCosts(this.enchantmentsToLevel));
        int costsAsExperience = scaledCosts - ExperienceUtil.convertLevelsToExperience(this.enchantingBaseCost);
        int costsAsLevels = 0;
        if (costsAsExperience < 0) {
            final int playerExperiencePoints = ExperienceUtil.convertLevelsToExperience(player.experienceLevel) + (int) (player.getXpNeededForNextLevel() * player.experienceProgress);
            final int i = ExperienceUtil.convertExperienceToLevel(-playerExperiencePoints + costsAsExperience);
            final int experienceLevel = player.experienceLevel;
            costsAsLevels = (i + experienceLevel);
        } else if (costsAsExperience > 0) {
            costsAsLevels = ExperienceUtil.convertExperienceToLevel(costsAsExperience);
        }
        return costsAsLevels;
    }

    private void markChanged() {
        this.enchantmentsChanged = !this.enchantmentsToLevel.equals(this.enchantmentsToLevelBase);
    }

    private int getScaledCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        final double totalCosts = this.getTotalCosts(enchantmentsToLevel);
        final int maxCost = this.infuserType.isAdvanced() ? EnchantingInfuser.CONFIG.server().costs.maximumCostAdvanced : EnchantingInfuser.CONFIG.server().costs.maximumCostNormal;
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
            if (!EnchantingInfuser.CONFIG.server().costs.vanillaCostOnly || ForgeRegistries.ENCHANTMENTS.getKey(enchantment).getNamespace().equals("minecraft")) {
                final Pair<Enchantment.Rarity, Integer> pair2 = Pair.of(enchantment.getRarity(), enchantment.getMaxLevel());
                final Optional<Map.Entry<Enchantment, Pair<Enchantment.Rarity, Integer>>> any = map.entrySet().stream()
                        .filter(e -> !e.getKey().isCompatibleWith(enchantment))
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
                .mapToInt(e -> this.getCostByRarity(e.getFirst()) * e.getSecond())
                .sum();
    }

    public void setType(InfuserBlock.InfuserType type) {
        this.infuserType = type;
    }

    private Pair<Enchantment.Rarity, Integer> compareEnchantmentData(Pair<Enchantment.Rarity, Integer> pair1, Pair<Enchantment.Rarity, Integer> pair2) {
        int cost1 = this.getCostByRarity(pair1.getFirst()) * pair1.getSecond();
        int cost2 = this.getCostByRarity(pair2.getFirst()) * pair2.getSecond();
        return cost2 > cost1 ? pair2 : pair1;
    }

    private int getAllCosts(Map<Enchantment, Integer> enchantmentsToLevel) {
        return enchantmentsToLevel.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .mapToInt(entry -> this.getCostByRarity(entry.getKey()) * entry.getValue())
                .sum();
    }

    private int getCostByRarity(Enchantment enchantment) {
        int cost = this.getCostByRarity(enchantment.getRarity());
        if (EnchantingInfuser.CONFIG.server().costs.doubleUniques && (enchantment.isTreasureOnly() || !enchantment.isDiscoverable()) && !enchantment.isCurse()) {
            cost *= 2;
        }
        return cost;
    }

    private int getCostByRarity(Enchantment.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> EnchantingInfuser.CONFIG.server().costs.commonCost;
            case UNCOMMON -> EnchantingInfuser.CONFIG.server().costs.uncommonCost;
            case RARE -> EnchantingInfuser.CONFIG.server().costs.rareCost;
            case VERY_RARE -> EnchantingInfuser.CONFIG.server().costs.veryRareCost;
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
        return this.infuserType.isAdvanced() ? EnchantingInfuser.CONFIG.server().maximumPowerAdvanced : EnchantingInfuser.CONFIG.server().maximumPowerNormal;
    }

    public ItemStack getEnchantableStack() {
        return this.enchantSlots.getItem(0);
    }

    public int getCost() {
        return this.enchantingCost.get();
    }

    public boolean canEnchant(Player player) {
        return !this.enchantSlots.getItem(0).isEmpty() && this.enchantmentsChanged && (player.experienceLevel >= this.getCost() || player.getAbilities().instabuild);
    }

    public boolean canRepair(Player player) {
        // TODO
        return !this.enchantSlots.getItem(0).isEmpty() && (player.experienceLevel >= this.getCost() || player.getAbilities().instabuild);
    }

    public Map<Enchantment, Integer> getValidEnchantments() {
        return this.enchantmentsToLevel.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), ImmutableMap::copyOf));
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        return this.enchantmentsToLevel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(e -> e.getKey().getRarity().ordinal()).thenComparing(e -> new TranslatableComponent(e.getKey().getDescriptionId()).getString()))
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

    public static boolean isBlockEmpty(Level world, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }
}
