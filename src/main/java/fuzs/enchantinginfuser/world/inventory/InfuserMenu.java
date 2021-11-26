package fuzs.enchantinginfuser.world.inventory;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.network.message.S2CEnchantmentLevelMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private Map<Enchantment, Integer> enchantmentsToLevel;

    public InfuserMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public InfuserMenu(int id, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        super(ModRegistry.INFUSING_MENU_TYPE.get(), id);
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;
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
            ItemStack itemstack = pInventory.getItem(0);
            if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
                this.levelAccess.execute((Level level, BlockPos pos) -> {
                    int power = this.setEnchantingPower(level, pos);
                    final List<Enchantment> availableEnchantments = getAvailableEnchantments(itemstack, power >= 25, power >= 30);
                    this.setEnchantments(availableEnchantments);
                });
            } else {
                this.setEnchantments(List.of());
            }
        }
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
        int power = this.getEnchantingPower(level, pos);
        this.enchantingPower.set(power);
        return power;
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {

    }

    private int getEnchantingPower(Level world, BlockPos pos) {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && isBlockEmpty(world, pos.offset(l, 0, k)) && isBlockEmpty(world, pos.offset(l, 1, k))) {
                    power += this.getPower(world, pos.offset(l * 2, 0, k * 2));
                    power += this.getPower(world, pos.offset(l * 2, 1, k * 2));
                    if (l != 0 && k != 0) {
                        power += this.getPower(world, pos.offset(l * 2, 0, k));
                        power += this.getPower(world, pos.offset(l * 2, 1, k));
                        power += this.getPower(world, pos.offset(l, 0, k * 2));
                        power += this.getPower(world, pos.offset(l, 1, k * 2));
                    }
                }
            }
        }
        return power;
    }

    private float getPower(Level world, BlockPos pos) {
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
        int clampedLevel = Mth.clamp(level, 0, enchantment.getMaxLevel());
        if (level != clampedLevel) {
            EnchantingInfuser.LOGGER.warn("trying change enchantment level beyond bounds");
            return -1;
        }
        this.enchantmentsToLevel.put(enchantment, clampedLevel);
        this.enchantingCost.set(this.getAllCosts());
        return level;
    }

    public void setEnchantmentLevel(Enchantment enchantment, int level) {
        this.enchantmentsToLevel.put(enchantment, level);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0) return false;
        ItemStack itemstack = this.enchantSlots.getItem(0);
        final int cost = this.getAllCosts();
        if (itemstack.isEmpty() || cost <= 0 || player.experienceLevel < cost && !player.getAbilities().instabuild) {
            return false;
        } else {
            this.levelAccess.execute((level, pos) -> {
                ItemStack itemstack2 = itemstack;
                if (!this.enchantmentsToLevel.isEmpty()) {
                    player.onEnchantmentPerformed(itemstack, cost);
                    boolean isBook = itemstack.is(Items.BOOK);
                    if (isBook) {
                        itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundTag compoundtag = itemstack.getTag();
                        if (compoundtag != null) {
                            itemstack2.setTag(compoundtag.copy());
                        }
                        this.enchantSlots.setItem(0, itemstack2);
                    }
                    for (Map.Entry<Enchantment, Integer> entry : this.enchantmentsToLevel.entrySet()) {
                        if (entry.getValue() > 0) {
                            if (isBook) {
                                EnchantedBookItem.addEnchantment(itemstack2, new EnchantmentInstance(entry.getKey(), entry.getValue()));
                            } else {
                                itemstack2.enchant(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemstack2, cost);
                    }
                    this.enchantSlots.setChanged();
                    this.slotsChanged(this.enchantSlots);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                }

            });
            return true;
        }
    }

    private int getAllCosts() {
        return this.enchantmentsToLevel.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .mapToInt(entry -> this.getCostByRarity(entry.getKey()) * entry.getValue())
                .sum();
    }

    private int getCostByRarity(Enchantment enchantment) {
        return switch (enchantment.getRarity()) {
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
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(41).hasItem()) {
                    if (!this.moveItemStackTo(itemstack1, 41, 42, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
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

    public int getEnchantingPower() {
        return this.enchantingPower.get();
    }

    public int getCost() {
        return this.enchantingCost.get();
    }

    public boolean canEnchant(Player player) {
        final int cost = this.getCost();
        return cost > 0 && player.experienceLevel >= cost;
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        return this.enchantmentsToLevel.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(e -> e.getKey().getRarity().ordinal()).thenComparing(e -> new TranslatableComponent(e.getKey().getDescriptionId()).getString()))
                .collect(Collectors.toList());
    }

    public void setEnchantments(List<Enchantment> enchantments) {
        this.enchantmentsToLevel = enchantments.stream()
                .collect(Collectors.toMap(Function.identity(), enchantment -> 0));
        this.levelAccess.execute((Level level, BlockPos blockPos) -> {
            EnchantingInfuser.NETWORK.sendTo(new S2CCompatibleEnchantsMessage(this.containerId, enchantments), (ServerPlayer) this.player);
        });
    }

    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, boolean allowTreasure, boolean allowDiscoverable) {
        List<Enchantment> list = Lists.newArrayList();
        boolean isBook = stack.is(Items.BOOK);
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if (enchantment.canApplyAtEnchantingTable(stack) || (isBook && enchantment.isAllowedOnBooks())) {
                if ((!enchantment.isTreasureOnly() || allowTreasure) && (enchantment.isDiscoverable() || allowDiscoverable)) {
                    list.add(enchantment);
                }
            }
        }
        return list;
    }

    public static boolean isBlockEmpty(Level world, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }
}
