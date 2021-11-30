package fuzs.enchantinginfuser.network.client.message;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.v2.message.Message;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
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
    public void write(PacketBuffer buf) {
        buf.writeByte(this.containerId);
        buf.writeInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(this.enchantment));
        buf.writeBoolean(this.increase);
    }

    @Override
    public void read(PacketBuffer buf) {
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
        public void handle(C2SAddEnchantLevelMessage packet, PlayerEntity player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu) {
                ((InfuserMenu) player.containerMenu).clickEnchantmentButton(packet.enchantment, packet.increase);
            }
        }
    }
}
