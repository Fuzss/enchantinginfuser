package fuzs.enchantinginfuser.client.util;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EnchantmentTooltipHelper {
    public static final String KEY_INCOMPATIBLE_ENCHANTMENTS = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("enchantment.tooltip.incompatible"));
    public static final String KEY_CURRENT_ENCHANTING_POWER = Util.makeDescriptionId("gui",
            EnchantingInfuser.id("enchantment.tooltip.current_enchanting_power"));

    public static List<Component> getIncompatibleEnchantmentsTooltip(Collection<Holder<Enchantment>> incompatibleEnchantments) {
        Component component = Component.translatable(KEY_INCOMPATIBLE_ENCHANTMENTS,
                incompatibleEnchantments.stream()
                        .map(EnchantmentTooltipHelper::getDisplayName)
                        .reduce((MutableComponent o1, MutableComponent o2) -> o1.append(", ").append(o2))
                        .orElse(Component.empty())
                        .withStyle(ChatFormatting.GRAY));
        return Collections.singletonList(component);
    }

    public static List<Component> getEnchantmentTooltip(Holder<Enchantment> holder) {
        List<Component> tooltipLines = new ArrayList<>();
        Component levelComponent = getLevelComponent(holder);
        tooltipLines.add(holder.value().description().copy().append(CommonComponents.SPACE).append(levelComponent));
        String translationKey = getEnchantmentDescriptionKey(holder);
        if (translationKey != null) {
            tooltipLines.add(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));
        }

        return tooltipLines;
    }

    private static Component getLevelComponent(Holder<Enchantment> holder) {
        int minLevel = holder.value().getMinLevel();
        int maxLevel = EnchantingBehavior.get().getMaxLevel(holder);
        MutableComponent component = Component.translatable("enchantment.level." + minLevel);
        if (minLevel != maxLevel) {
            component.append("-").append(Component.translatable("enchantment.level." + maxLevel));
        }

        return wrapInRoundBrackets(component).withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent wrapInRoundBrackets(Component component) {
        return Component.literal("(").append(component).append(")");
    }

    @Nullable
    private static String getEnchantmentDescriptionKey(Holder<Enchantment> holder) {
        String translationKey = holder.unwrapKey().map((ResourceKey<Enchantment> resourceKey) -> {
            return Util.makeDescriptionId(resourceKey.registry().getPath(), resourceKey.identifier());
        }).orElse(null);
        if (translationKey == null) {
            return null;
        } else if (Language.getInstance().has(translationKey + ".desc")) {
            return translationKey + ".desc";
        } else if (Language.getInstance().has(translationKey + ".description")) {
            return translationKey + ".description";
        } else {
            return null;
        }
    }

    public static MutableComponent getDisplayName(Holder<Enchantment> holder) {
        return holder.value().description().copy().setStyle(Style.EMPTY);
    }

    public static MutableComponent getDisplayNameWithLevel(Holder<Enchantment> holder, int level) {
        MutableComponent component = getDisplayName(holder);
        if (level != 1 || EnchantingBehavior.get().getMaxLevel(holder) != 1) {
            return component.append(CommonComponents.SPACE)
                    .append(Component.translatable("enchantment.level." + level));
        } else {
            return component;
        }
    }
}
