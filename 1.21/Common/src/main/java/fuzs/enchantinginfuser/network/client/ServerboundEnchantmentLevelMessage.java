package fuzs.enchantinginfuser.network.client;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v3.ServerMessageListener;
import fuzs.puzzleslib.api.network.v3.ServerboundMessage;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.IntSupplier;

public record ServerboundEnchantmentLevelMessage(int containerId, Holder<Enchantment> enchantment, Operation operation) implements ServerboundMessage<ServerboundEnchantmentLevelMessage> {

    @Override
    public ServerMessageListener<ServerboundEnchantmentLevelMessage> getHandler() {
        return new ServerMessageListener<>() {

            @Override
            public void handle(ServerboundEnchantmentLevelMessage message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
                if (player.containerMenu.containerId == message.containerId && player.containerMenu instanceof InfuserMenu menu) {
                    menu.clickEnchantmentLevelButton(message.enchantment, message.operation);
                }
            }
        };
    }

    public enum Operation implements IntSupplier {
        ADD, REMOVE;

        @Override
        public int getAsInt() {
            return this == REMOVE ? -1 : 1;
        }
    }
}
