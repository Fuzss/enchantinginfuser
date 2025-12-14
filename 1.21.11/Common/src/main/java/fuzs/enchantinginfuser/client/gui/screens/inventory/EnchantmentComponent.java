package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.google.common.collect.ImmutableSet;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.util.v1.ComponentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record EnchantmentComponent(InfuserMenu.EnchantmentValues enchantmentValues,
                                   int enchantmentLevel,
                                   Collection<Holder<Enchantment>> incompatibleEnchantments) {

    public static EnchantmentComponent create(Holder<Enchantment> enchantment, InfuserMenu.EnchantmentValues enchantmentValues, ItemEnchantments itemEnchantments) {
        int enchantmentLevel = itemEnchantments.getLevel(enchantment);
        Set<Holder<Enchantment>> incompatibleEnchantments = new HashSet<>();
        for (Holder<Enchantment> holder : itemEnchantments.keySet()) {
            if (!enchantment.is(holder) && !Enchantment.areCompatible(enchantment, holder)) {
                incompatibleEnchantments.add(holder);
            }
        }

        return new EnchantmentComponent(enchantmentValues,
                enchantmentLevel,
                ImmutableSet.copyOf(incompatibleEnchantments));
    }

    public boolean isPresent() {
        return this.enchantmentLevel > 0;
    }

    public boolean isIncompatible() {
        return !this.incompatibleEnchantments.isEmpty();
    }

    public boolean isInactive() {
        return this.isIncompatible() || this.isNotAvailable();
    }

    public boolean isNotAvailable() {
        return this.enchantmentValues.availableLevel() == 0;
    }

    public Component getDisplayName(Holder<Enchantment> enchantment, int maxWidth, Font font, int enchantmentSeed) {
        if (this.isNotAvailable()) {
            int enchantmentId = Minecraft.getInstance()
                    .getConnection()
                    .registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getIdOrThrow(enchantment.value());
            EnchantmentNames.getInstance().initSeed(enchantmentSeed + enchantmentId);
            maxWidth = (int) (maxWidth * 0.72F);
            FormattedText randomName = EnchantmentNames.getInstance().getRandomName(font, maxWidth);
            List<FormattedCharSequence> lines = font.split(randomName, maxWidth);
            if (!lines.isEmpty()) {
                return ComponentHelper.getAsComponent(lines.getFirst());
            } else {
                return Component.literal("???????");
            }
        }

        if (this.isPresent()) {
            return EnchantmentTooltipHelper.getDisplayNameWithLevel(enchantment, this.enchantmentLevel);
        } else {
            return EnchantmentTooltipHelper.getDisplayName(enchantment);
        }
    }

    public List<Component> getTooltip(Holder<Enchantment> enchantment) {
        if (this.isNotAvailable()) {
            return this.getWeakPowerTooltip(EnchantmentTooltipHelper.UNKNOWN_ENCHANT_COMPONENT);
        } else if (this.isIncompatible()) {
            return EnchantmentTooltipHelper.getIncompatibleEnchantmentsTooltip(this.incompatibleEnchantments);
        } else {
            return EnchantmentTooltipHelper.getEnchantmentTooltip(enchantment);
        }
    }

    public List<Component> getWeakPowerTooltip(Component component) {
        return EnchantmentTooltipHelper.getWeakPowerTooltip(this.enchantmentValues.enchantmentPower(),
                this.enchantmentValues.requiredEnchantmentPower(),
                component);
    }
}
