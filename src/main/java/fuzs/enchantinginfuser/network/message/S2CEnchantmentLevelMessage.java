package fuzs.enchantinginfuser.network.message;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class S2CEnchantmentLevelMessage implements Message {
    private int containerId;
    private Enchantment enchantment;
    private int level;

    public S2CEnchantmentLevelMessage() {

    }

    public S2CEnchantmentLevelMessage(int containerId, Enchantment enchantment, int level) {
        this.containerId = containerId;
        this.enchantment = enchantment;
        this.level = level;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(this.enchantment));
        buf.writeByte(this.level);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantment = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buf.readInt());
        this.level = buf.readByte();
    }

    @Override
    public EnchantmentLevelHandler makeHandler() {
        return new EnchantmentLevelHandler();
    }

    private static class EnchantmentLevelHandler extends PacketHandler<S2CEnchantmentLevelMessage> {
        @Override
        public void handle(S2CEnchantmentLevelMessage packet, Player player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                menu.setEnchantmentLevel(packet.enchantment, packet.level);
            }
        }
    }
}
