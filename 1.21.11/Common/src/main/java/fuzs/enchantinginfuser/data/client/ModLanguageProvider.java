package fuzs.enchantinginfuser.data.client;

import fuzs.enchantinginfuser.client.gui.components.InfuserMenuButton;
import fuzs.enchantinginfuser.client.gui.components.LevelBasedOperationButton;
import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentLevelEntry;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ModRegistry.INFUSER_BLOCK.value(), "Enchanting Infuser");
        builder.add(ModRegistry.ADVANCED_INFUSER_BLOCK.value(), "Advanced Enchanting Infuser");
        builder.add(InfuserBlock.COMPONENT_CHOOSE, "Choose enchantments for your gear.");
        builder.add(InfuserBlock.COMPONENT_CHOOSE_AND_MODIFY, "Choose, modify and remove enchantments for your gear.");
        builder.add(InfuserBlock.COMPONENT_REPAIR, "Repair your gear with levels.");
        builder.add(EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER, "Enchanting Power: %s / %s");
        builder.add(InfuserScreen.KEY_TOOLTIP_HINT,
                "Place more bookshelves in a square around the infuser on up to two layers.");
        builder.add(InfuserMenuButton.KEY_TOOLTIP_DURABILITY, "Durability: %s");
        builder.add(InfuserMenuButton.KEY_TOOLTIP_CHANGE, "%s -> %s");
        builder.add(InfuserMenuButton.KEY_TOOLTIP_EXPERIENCE, "Gain Experience Points");
        builder.add(EnchantmentLevelEntry.UNKNOWN_ENCHANT_COMPONENT,
                "This enchantment is too powerful for an infuser with such little enchanting power.");
        builder.add(LevelBasedOperationButton.INCREASE_LEVEL_COMPONENT,
                "Further increasing the level for this enchantment requires an infuser with more enchanting power.");
        builder.add(LevelBasedOperationButton.MODIFY_LEVEL_COMPONENT,
                "Modifying the level for this enchantment requires an infuser with more enchanting power.");
        builder.add(EnchantmentTooltipHelper.KEY_INCOMPATIBLE_ENCHANTMENTS,
                "This enchantment is incompatible with: %s");
    }
}
