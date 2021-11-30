package fuzs.enchantinginfuser.network.message;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.InfuserBlock;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class S2CInfuserDataMessage implements Message {
    private int containerId;
    private int enchantingPower;
    private InfuserBlock.InfuserType infuserType;

    public S2CInfuserDataMessage() {

    }

    public S2CInfuserDataMessage(int containerId, int enchantingPower, InfuserBlock.InfuserType infuserType) {
        this.containerId = containerId;
        this.enchantingPower = enchantingPower;
        this.infuserType = infuserType;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeByte(this.enchantingPower);
        buf.writeEnum(this.infuserType);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantingPower = buf.readByte();
        this.infuserType = buf.readEnum(InfuserBlock.InfuserType.class);
    }

    @Override
    public InfuserDataHandler makeHandler() {
        return new InfuserDataHandler();
    }

    private static class InfuserDataHandler extends PacketHandler<S2CInfuserDataMessage> {
        @Override
        public void handle(S2CInfuserDataMessage packet, Player player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                menu.setEnchantingPower(packet.enchantingPower);
                menu.setType(packet.infuserType);
                if (((Minecraft) gameInstance).screen instanceof InfuserScreen screen) screen.refreshSearchResults();
            }
        }
    }
}
