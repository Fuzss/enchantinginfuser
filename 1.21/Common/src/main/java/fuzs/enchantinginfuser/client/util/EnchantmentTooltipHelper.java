package fuzs.enchantinginfuser.client.util;

import com.google.common.collect.Lists;
import fuzs.enchantinginfuser.api.v2.EnchantingInfuserApi;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.ClientComponentSplitter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class EnchantmentTooltipHelper {
    public static final String KEY_INCOMPATIBLE_ENCHANTMENTS = "gui.enchantinginfuser.tooltip.incompatible";
    public static final Component UNKNOWN_ENCHANT_COMPONENT = Component.translatable(
            "gui.enchantinginfuser.tooltip.unknown_enchantment").withStyle(ChatFormatting.GRAY);
    public static final Component INCREASE_LEVEL_COMPONENT = Component.translatable(
            "gui.enchantinginfuser.tooltip.lowPower1").withStyle(ChatFormatting.GRAY);
    public static final Component MODIFY_LEVEL_COMPONENT = Component.translatable(
            "gui.enchantinginfuser.tooltip.lowPower2").withStyle(ChatFormatting.GRAY);
    public static final String KEY_CURRENT_ENCHANTING_POWER = "gui.enchantinginfuser.tooltip.current_enchanting_power";

    public static List<FormattedCharSequence> getWeakPowerTooltip(int currentPower, int requiredPower, Component component) {
        List<FormattedCharSequence> lines = Lists.newArrayList();
        Component currentPowerComponent = Component.literal(String.valueOf(currentPower))
                .withStyle(ChatFormatting.RED);
        Component requiredPowerComponent = Component.literal(String.valueOf(requiredPower));
        lines.add(Component.translatable(KEY_CURRENT_ENCHANTING_POWER, currentPowerComponent, requiredPowerComponent)
                .getVisualOrderText());
        ClientComponentSplitter.splitTooltipLines(component).forEach(lines::add);
        return lines;
    }

    public static List<FormattedCharSequence> getIncompatibleEnchantmentsTooltip(Collection<Holder<Enchantment>> incompatibleEnchantments) {
        Component component = Component.translatable(KEY_INCOMPATIBLE_ENCHANTMENTS,
                incompatibleEnchantments.stream()
                        .map(EnchantmentTooltipHelper::getDisplayName)
                        .reduce((MutableComponent o1, MutableComponent o2) -> o1.append(", ").append(o2))
                        .orElse(Component.empty())
                        .withStyle(ChatFormatting.GRAY)
        );
        return ClientComponentSplitter.splitTooltipLines(component).toList();
    }

    public static List<FormattedCharSequence> getEnchantmentTooltip(Holder<Enchantment> enchantment) {
        List<FormattedCharSequence> lines = Lists.newArrayList();
        lines.add(Component.empty()
                .append(enchantment.value().description())
                .append(CommonComponents.SPACE)
                .append(getLevelComponent(enchantment))
                .getVisualOrderText());
        String translationKey = getEnchantmentDescriptionKey(enchantment);
        if (translationKey != null) {
            Component component = Component.translatable(translationKey).withStyle(ChatFormatting.GRAY);
            ClientComponentSplitter.splitTooltipLines(component).forEach(lines::add);
        }
        return lines;
    }

    private static Component getLevelComponent(Holder<Enchantment> enchantment) {
        int minLevel = EnchantingInfuserApi.getEnchantStatsProvider().getMinLevel(enchantment);
        int maxLevel = EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment);
        MutableComponent component = Component.translatable("enchantment.level." + minLevel);
        if (minLevel != maxLevel) {
            component.append("-").append(Component.translatable("enchantment.level." + maxLevel));
        }
        return wrapInRoundBrackets(component).withStyle(ChatFormatting.GRAY);
    }

    static MutableComponent wrapInRoundBrackets(Component component) {
        return Component.literal("(").append(component).append(")");
    }

    @Nullable
    private static String getEnchantmentDescriptionKey(Holder<Enchantment> enchantment) {
        String translationKey = enchantment.unwrapKey().map(resourceKey -> {
            return Util.makeDescriptionId(resourceKey.registry().getPath(), resourceKey.location());
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

    public static MutableComponent getDisplayName(Holder<Enchantment> enchantment) {
        return Component.empty().append(enchantment.value().description());
    }

    public static MutableComponent getDisplayNameWithLevel(Holder<Enchantment> enchantment, int level) {
        MutableComponent component = getDisplayName(enchantment);
        if (level != 1 || EnchantingInfuserApi.getEnchantStatsProvider().getMaxLevel(enchantment) != 1) {
            return component.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
        } else {
            return component;
        }
    }
}
