package fuzs.enchantinginfuser.network;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class S2CInfuserDataMessage implements Message<S2CInfuserDataMessage> {
    private int containerId;
    private int enchantingPower;
    private int repairCost;

    public S2CInfuserDataMessage() {

    }

    public S2CInfuserDataMessage(int containerId, int enchantingPower, int repairCost) {
        this.containerId = containerId;
        this.enchantingPower = enchantingPower;
        this.repairCost = repairCost;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeByte(this.enchantingPower);
        buf.writeByte(this.repairCost);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantingPower = buf.readByte();
        this.repairCost = buf.readByte();
    }

    @Override
    public MessageHandler<S2CInfuserDataMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CInfuserDataMessage packet, Player player, Object gameInstance) {
                if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                    menu.setEnchantingPower(packet.enchantingPower);
                    menu.setRepairCost(packet.repairCost);
                    if (((Minecraft) gameInstance).screen instanceof InfuserScreen screen) screen.refreshSearchResults();
                }
            }
        };
    }
}
