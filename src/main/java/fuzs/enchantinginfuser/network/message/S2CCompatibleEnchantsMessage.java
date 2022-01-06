package fuzs.enchantinginfuser.network.message;

import com.google.common.collect.Lists;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.List;

public class S2CCompatibleEnchantsMessage implements Message {
    private int containerId;
    private List<Enchantment> enchantments;

    public S2CCompatibleEnchantsMessage() {

    }

    public S2CCompatibleEnchantsMessage(int containerId, List<Enchantment> enchantments) {
        this.containerId = containerId;
        this.enchantments = enchantments;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeInt(this.enchantments.size());
        for (Enchantment enchantment : this.enchantments) {
            buf.writeInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(enchantment));
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        final int size = buf.readInt();
        List<Enchantment> enchantments = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            enchantments.add(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buf.readInt()));
        }
        this.enchantments = enchantments;
    }

    @Override
    public CompatibleEnchantsHandler makeHandler() {
        return new CompatibleEnchantsHandler();
    }

    private static class CompatibleEnchantsHandler extends PacketHandler<S2CCompatibleEnchantsMessage> {
        @Override
        public void handle(S2CCompatibleEnchantsMessage packet, Player player, Object gameInstance) {
            if (player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof InfuserMenu menu) {
                menu.setEnchantments(packet.enchantments);
                if (((Minecraft) gameInstance).screen instanceof InfuserScreen screen) screen.refreshSearchResults();
            }
        }
    }
}
