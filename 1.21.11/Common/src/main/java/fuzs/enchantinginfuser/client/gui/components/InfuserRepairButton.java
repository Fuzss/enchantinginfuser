package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Collections;
import java.util.List;

public class InfuserRepairButton extends InfuserMenuButton {

    public InfuserRepairButton(int x, int y, OnPress onPress) {
        super(x, y, InfuserScreen.REPAIR_BUTTON_SPRITES, onPress);
    }

    @Override
    ChatFormatting getStringColor(int value, boolean mayApply) {
        return mayApply ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    @Override
    String getStringValue(int value) {
        return String.valueOf(value);
    }

    @Override
    List<FormattedText> getCustomLines(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        Component changeComponent = Component.translatable(KEY_TOOLTIP_CHANGE,
                itemStack.getMaxDamage() - itemStack.getDamageValue(),
                itemStack.getMaxDamage());
        Component durabilityComponent = Component.translatable(KEY_TOOLTIP_DURABILITY, changeComponent)
                .withStyle(ChatFormatting.YELLOW);
        return Collections.singletonList(durabilityComponent);
    }
}
