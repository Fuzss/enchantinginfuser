package fuzs.enchantinginfuser.network;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v3.ClientMessageListener;
import fuzs.puzzleslib.api.network.v3.ClientboundMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record ClientboundInfuserEnchantmentsMessage(int containerId,
                                                    ItemEnchantments enchantments,
                                                    boolean initialize) implements ClientboundMessage<ClientboundInfuserEnchantmentsMessage> {
    @Override
    public ClientMessageListener<ClientboundInfuserEnchantmentsMessage> getHandler() {
        return new ClientMessageListener<>() {

            @Override
            public void handle(ClientboundInfuserEnchantmentsMessage message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {
                if (player.containerMenu.containerId == message.containerId && player.containerMenu instanceof InfuserMenu menu) {
                    if (message.initialize) {
                        menu.setInitialEnchantments(level, message.enchantments);
                    } else {
                        menu.setEnchantmentsFromServer(message.enchantments);
                    }
                    if (client.screen instanceof InfuserScreen screen) {
                        screen.refreshSearchResults();
                    }
                }
            }
        };
    }
}
