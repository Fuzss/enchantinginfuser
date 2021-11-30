package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BookItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfuserMenu extends Container implements IContainerListener {
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlotType[] SLOT_IDS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
    private final IInventory enchantSlots;
    private final IWorldPosCallable levelAccess;
    private final PlayerEntity player;
    private InfuserBlock.InfuserType infuserType;
    private final IntReferenceHolder enchantingPower = IntReferenceHolder.standalone();
    private final IntReferenceHolder enchantingCost = IntReferenceHolder.standalone();
    private Map<Enchantment, Integer> enchantmentsToLevel;

    public InfuserMenu(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(1), IWorldPosCallable.NULL, InfuserBlock.InfuserType.NORMAL);
    }

    public InfuserMenu(int id, PlayerInventory inventory, IInventory container, IWorldPosCallable levelAccess, InfuserBlock.InfuserType type) {
        super(ModRegistry.INFUSING_MENU_TYPE, id);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
        this.infuserType = type;
        this.addSlot(new Slot(container, 0, 8, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // can't exchange items directly while holding replacement otherwise, this seems to do the trick
                return stack.isEnchantable() || stack.getItem() instanceof BookItem && !this.hasItem();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int k = 0; k < 4; ++k) {
            final EquipmentSlotType equipmentslot = SLOT_IDS[k];
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
                public boolean mayPickup(PlayerEntity p_39744_) {
                    ItemStack itemstack = this.getItem();
                    return (itemstack.isEmpty() || p_39744_.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(p_39744_);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(PlayerContainer.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
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
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
        this.addDataSlot(this.enchantingPower);
        this.addDataSlot(this.enchantingCost);
        this.addSlotListener(this);
    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer) {
        return this.enchantSlots.stillValid(pPlayer);
    }

    @Override
    public void slotsChanged(IInventory pInventory) {
        if (pInventory == this.enchantSlots) {
            this.enchantingCost.set(0);
            ItemStack itemstack = pInventory.getItem(0);
            if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
                this.levelAccess.execute((World level, BlockPos pos) -> {
                    this.setEnchantingPower(level, pos);
                    final List<Enchantment> availableEnchantments = getAvailableEnchantments(itemstack, EnchantingInfuser.CONFIG.server().types.treasure, EnchantingInfuser.CONFIG.server().types.undiscoverable, EnchantingInfuser.CONFIG.server().types.curses);
                    this.setEnchantments(availableEnchantments);
                });
            } else {
                this.setEnchantments(Lists.newArrayList());
            }
        }
    }

    @Override
    public void slotChanged(Container abstractContainerMenu, int i, ItemStack itemStack) {
        if (abstractContainerMenu == this) {
            this.levelAccess.execute((World level, BlockPos pos) -> {
                if (i == 0) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    public int setEnchantingPower(World level, BlockPos pos) {
        int power = this.getAvailablePower(level, pos);
        this.enchantingPower.set(power);
        return power;
    }

    public void setEnchantingPower(int power) {
        this.enchantingPower.set(power);
    }

    @Override
    public void refreshContainer(Container pContainerToSend, NonNullList<ItemStack> pItemsList) {

    }

    @Override
    public void setContainerData(Container pContainer, int pVarToUpdate, int pNewValue) {

    }

    private int getAvailablePower(World world, BlockPos pos) {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && isBlockEmpty(world, pos.offset(l, 0, k)) && isBlockEmpty(world, pos.offset(l, 1, k))) {
                    power += this.getBlockPower(world, pos.offset(l * 2, 0, k * 2));
                    power += this.getBlockPower(world, pos.offset(l * 2, 1, k * 2));
                    if (l != 0 && k != 0) {
                        power += this.getBlockPower(world, pos.offset(l * 2, 0, k));
                        power += this.getBlockPower(world, pos.offset(l * 2, 1, k));
                        power += this.getBlockPower(world, pos.offset(l, 0, k * 2));
                        power += this.getBlockPower(world, pos.offset(l, 1, k * 2));
                    }
                }
            }
        }
        return power;
    }

    private float getBlockPower(World world, BlockPos pos) {
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    public int clickEnchantmentButton(Enchantment enchantment, boolean increase) {
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
        if (level != MathHelper.clamp(level, 0, enchantment.getMaxLevel())) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond bounds");
            return -1;
        }
        if (level > this.getMaxLevel(enchantment).getSecond()) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond max allowed level");
            return -1;
        }
        this.enchantmentsToLevel.put(enchantment, level);
        this.enchantingCost.set(this.getScaledCosts());
        return level;
    }

    @Override
    public boolean clickMenuButton(PlayerEntity player, int id) {
        if (id != 0) return false;
        ItemStack itemstack = this.enchantSlots.getItem(0);
        final int cost = this.getScaledCosts();
        if (itemstack.isEmpty() || cost <= 0 || player.experienceLevel < cost && !player.abilities.instabuild) {
            return false;
        } else {
            this.levelAccess.execute((level, pos) -> {
                ItemStack itemstack2 = itemstack;
                if (!this.enchantmentsToLevel.isEmpty()) {
                    player.onEnchantmentPerformed(itemstack, cost);
                    boolean isBook = itemstack.getItem() instanceof BookItem;
                    if (isBook) {
                        itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundNBT compoundtag = itemstack.getTag();
                        if (compoundtag != null) {
                            itemstack2.setTag(compoundtag.copy());
                        }
                        this.enchantSlots.setItem(0, itemstack2);
                    }
                    for (Map.Entry<Enchantment, Integer> entry : this.enchantmentsToLevel.entrySet()) {
                        if (entry.getValue() > 0) {
                            if (isBook) {
                                EnchantedBookItem.addEnchantment(itemstack2, new EnchantmentData(entry.getKey(), entry.getValue()));
                            } else {
                                itemstack2.enchant(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayerEntity) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, itemstack2, cost);
                    }
                    this.enchantSlots.setChanged();
                    this.slotsChanged(this.enchantSlots);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                }

            });
            return true;
        }
    }

    public Pair<Optional<Integer>, Integer> getMaxLevel(Enchantment enchantment) {
        final int currentPower = this.getCurrentPower();
        final int maxPower = this.infuserType.isAdvanced() ? EnchantingInfuser.CONFIG.server().maximumPowerAdvanced : EnchantingInfuser.CONFIG.server().maximumPowerNormal;
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
        double rarityMultiplier;
        switch (enchantment.getRarity()) {
            case COMMON:
                rarityMultiplier = EnchantingInfuser.CONFIG.server().power.commonMultiplier;
                break;
            case UNCOMMON:
                rarityMultiplier = EnchantingInfuser.CONFIG.server().power.uncommonMultiplier;
                break;
            case RARE:
                rarityMultiplier = EnchantingInfuser.CONFIG.server().power.rareMultiplier;
                break;
            case VERY_RARE:
                rarityMultiplier = EnchantingInfuser.CONFIG.server().power.veryRareMultiplier;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return (int) Math.round(maxPower * rarityMultiplier);
    }

    private int getScaledCosts() {
        final double totalCosts = this.getTotalCosts();
        final int maxCost = this.infuserType.isAdvanced() ? EnchantingInfuser.CONFIG.server().costs.maximumCostAdvanced : EnchantingInfuser.CONFIG.server().costs.maximumCostNormal;
        if (totalCosts > maxCost && !(this.enchantSlots.getItem(0).getItem() instanceof BookItem)) {
            final double ratio = maxCost / totalCosts;
            final int minCosts = this.enchantmentsToLevel.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            return Math.max((int) Math.round(this.getAllCosts() * ratio), minCosts);
        }
        return this.getAllCosts();
    }

    public void setType(InfuserBlock.InfuserType type) {
        this.infuserType = type;
    }

    private int getTotalCosts() {
        // this loops through all enchantments that can be applied to the current item
        // it then checks for compatibility and treats those as duplicates, the 'duplicate' with the higher cost is kept
        Map<Enchantment, Pair<Enchantment.Rarity, Integer>> map = Maps.newHashMap();
        for (Enchantment enchantment : this.enchantmentsToLevel.keySet()) {
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
    
    private Pair<Enchantment.Rarity, Integer> compareEnchantmentData(Pair<Enchantment.Rarity, Integer> pair1, Pair<Enchantment.Rarity, Integer> pair2) {
        int cost1 = this.getCostByRarity(pair1.getFirst()) * pair1.getSecond();
        int cost2 = this.getCostByRarity(pair2.getFirst()) * pair2.getSecond();
        return cost2 > cost1 ? pair2 : pair1;
    }

    private int getAllCosts() {
        return this.enchantmentsToLevel.entrySet().stream()
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
        switch (rarity) {
            case COMMON:
                return EnchantingInfuser.CONFIG.server().costs.commonCost;
            case UNCOMMON:
                return EnchantingInfuser.CONFIG.server().costs.uncommonCost;
            case RARE:
                return EnchantingInfuser.CONFIG.server().costs.rareCost;
            case VERY_RARE:
                return EnchantingInfuser.CONFIG.server().costs.veryRareCost;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlotType equipmentslot = MobEntity.getEquipmentSlotForItem(itemstack);
            if (pIndex == 0) {
                if (equipmentslot.getType() == EquipmentSlotType.Group.ARMOR && !this.slots.get(4 - equipmentslot.getIndex()).hasItem()) {
                    int i = 4 - equipmentslot.getIndex();
                    if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                        slot.onTake(pPlayer, itemstack1);
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentslot == EquipmentSlotType.OFFHAND && !this.slots.get(41).hasItem()) {
                    if (!this.moveItemStackTo(itemstack1, 41, 42, false)) {
                        slot.onTake(pPlayer, itemstack1);
                        return ItemStack.EMPTY;
                    }
                }
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    slot.onTake(pPlayer, itemstack1);
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.isEnchantable() || itemstack1.getItem() instanceof BookItem) {
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
        return this.enchantingPower.get();
    }

    public int getCost() {
        return this.enchantingCost.get();
    }

    public boolean canEnchant(PlayerEntity player) {
        final int cost = this.getCost();
        return cost > 0 && (player.experienceLevel >= cost || player.abilities.instabuild);
    }

    public Stream<Map.Entry<Enchantment, Integer>> getValidEnchantments() {
        return this.enchantmentsToLevel.entrySet().stream().filter(e -> e.getValue() > 0);
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        return this.enchantmentsToLevel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(e -> e.getKey().getRarity().ordinal()).thenComparing(e -> new TranslationTextComponent(e.getKey().getDescriptionId()).getString()))
                .collect(Collectors.toList());
    }

    public void setEnchantments(List<Enchantment> enchantments) {
        this.enchantmentsToLevel = enchantments.stream()
                .collect(Collectors.toMap(Function.identity(), enchantment -> 0));
        this.levelAccess.execute((World level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendTo(new S2CCompatibleEnchantsMessage(this.containerId, enchantments), (ServerPlayerEntity) this.player);
        });
    }

    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, boolean allowTreasure, boolean allowUndiscoverable, boolean allowCurse) {
        List<Enchantment> list = Lists.newArrayList();
        boolean isBook = stack.getItem() instanceof BookItem;
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if (enchantment.canApplyAtEnchantingTable(stack) || (isBook && enchantment.isAllowedOnBooks())) {
                if ((!enchantment.isTreasureOnly() || allowTreasure || (enchantment.isCurse() && allowCurse)) && (enchantment.isDiscoverable() || allowUndiscoverable)) {
                    list.add(enchantment);
                }
            }
        }
        return list;
    }

    public static boolean isBlockEmpty(World world, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }
}
