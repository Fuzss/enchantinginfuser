package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class InfuserRepairButton extends InfuserMenuButton {

    public InfuserRepairButton(InfuserScreen screen, int x, int y, OnPress onPress) {
        super(screen, x, y, 178, 185, onPress);
    }

    @Override
    int getValue() {
        return this.screen.getMenu().getRepairCost();
    }

    @Override
    boolean mayApply() {
        return this.screen.getMenu().canRepair(this.screen.minecraft.player);
    }

    @Override
    ChatFormatting getStringColor() {
        return this.mayApply() ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    @Override
    String getStringValue() {
        return String.valueOf(this.getValue());
    }

    @Override
    List<FormattedText> getCustomLines(ItemStack itemStack) {
        Component changeComponent = Component.translatable(KEY_TOOLTIP_CHANGE,
                itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage()
        );
        Component durabilityComponent = Component.translatable(KEY_TOOLTIP_DURABILITY,
                changeComponent
        ).withStyle(ChatFormatting.YELLOW);
        return Collections.singletonList(durabilityComponent);
    }
}
