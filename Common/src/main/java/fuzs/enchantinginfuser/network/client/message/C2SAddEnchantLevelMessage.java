package fuzs.enchantinginfuser.network.client.message;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

public class C2SAddEnchantLevelMessage implements Message<C2SAddEnchantLevelMessage> {
    private int containerId;
    private Enchantment enchantment;
    private boolean increase;

    public C2SAddEnchantLevelMessage() {

    }

    public C2SAddEnchantLevelMessage(int containerId, Enchantment enchantment, boolean increase) {
        this.containerId = containerId;
        this.enchantment = enchantment;
        this.increase = increase;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeInt(Registry.ENCHANTMENT.getId(this.enchantment));
        buf.writeBoolean(this.increase);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantment = (Registry.ENCHANTMENT.byId(buf.readInt()));
        this.increase = buf.readBoolean();
    }

    @Override
    public PacketHandler<C2SAddEnchantLevelMessage> makeHandler() {
        return new EnchantmentLevelHandler();
    }

    private static class EnchantmentLevelHandler extends PacketHandler<C2SAddEnchantLevelMessage> {

        @Override
        public void handle(C2SAddEnchantLevelMessage packet, Player player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                menu.clickEnchantmentLevelButton(player, packet.enchantment, packet.increase);
            }
        }
    }
}
