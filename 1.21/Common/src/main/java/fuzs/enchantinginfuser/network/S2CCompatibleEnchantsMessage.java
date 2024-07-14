package fuzs.enchantinginfuser.network;

import com.google.common.collect.Maps;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class S2CCompatibleEnchantsMessage implements WritableMessage<S2CCompatibleEnchantsMessage> {
    private final int containerId;
    private final Map<Enchantment, Integer> enchantmentsToLevel;

    public S2CCompatibleEnchantsMessage(int containerId, Map<Enchantment, Integer> enchantmentsToLevel) {
        this.containerId = containerId;
        this.enchantmentsToLevel = enchantmentsToLevel;
    }

    public S2CCompatibleEnchantsMessage(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        final int size = buf.readInt();
        Map<Enchantment, Integer> enchantmentsToLevel = Maps.newHashMap();
        for (int i = 0; i < size; i++) {
            enchantmentsToLevel.put(BuiltInRegistries.ENCHANTMENT.byId(buf.readInt()), buf.readInt());
        }
        this.enchantmentsToLevel = enchantmentsToLevel;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        buf.writeInt(this.enchantmentsToLevel.size());
        for (Map.Entry<Enchantment, Integer> entry : this.enchantmentsToLevel.entrySet()) {
            buf.writeInt(BuiltInRegistries.ENCHANTMENT.getId(entry.getKey()));
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public MessageHandler<S2CCompatibleEnchantsMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CCompatibleEnchantsMessage message, Player player, Object gameInstance) {
                if (player.containerMenu.containerId == message.containerId && player.containerMenu instanceof InfuserMenu menu) {
                    menu.setAndSyncEnchantments(message.enchantmentsToLevel);
                    if (((Minecraft) gameInstance).screen instanceof InfuserScreen screen)
                        screen.refreshSearchResults();
                }
            }
        };
    }
}
