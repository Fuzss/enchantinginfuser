package fuzs.enchantinginfuser.network.client.message;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class C2SAddEnchantLevelMessage implements Message {
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
        buf.writeInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(this.enchantment));
        buf.writeBoolean(this.increase);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantment = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buf.readInt());
        this.increase = buf.readBoolean();
    }

    @Override
    public EnchantmentLevelHandler makeHandler() {
        return new EnchantmentLevelHandler();
    }

    private static class EnchantmentLevelHandler extends PacketHandler<C2SAddEnchantLevelMessage> {
        @Override
        public void handle(C2SAddEnchantLevelMessage packet, Player player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                menu.clickEnchantmentButton(packet.enchantment, packet.increase);
            }
        }
    }
}
