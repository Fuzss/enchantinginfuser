package fuzs.enchantinginfuser.network.client;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v3.ServerMessageListener;
import fuzs.puzzleslib.api.network.v3.ServerboundMessage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.IntUnaryOperator;

public record ServerboundEnchantmentLevelMessage(int containerId,
                                                 Holder<Enchantment> enchantment,
                                                 Operation operation) implements ServerboundMessage<ServerboundEnchantmentLevelMessage> {

    @Override
    public ServerMessageListener<ServerboundEnchantmentLevelMessage> getHandler() {
        return new ServerMessageListener<>() {

            @Override
            public void handle(ServerboundEnchantmentLevelMessage message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
                if (player.containerMenu.containerId == message.containerId &&
                        player.containerMenu instanceof InfuserMenu menu) {
                    menu.clickEnchantmentLevelButton(message.enchantment, message.operation);
                }
            }
        };
    }

    public enum Operation implements IntUnaryOperator {
        ADD((int enchantmentLevel) -> enchantmentLevel + 1),
        REMOVE((int enchantmentLevel) -> enchantmentLevel - 1),
        ADD_ALL(Integer.MAX_VALUE),
        REMOVE_ALL(Integer.MIN_VALUE);

        private final IntUnaryOperator operator;

        Operation(int value) {
            this(operand -> value);
        }

        Operation(IntUnaryOperator operator) {
            this.operator = operator;
        }

        @Override
        public int applyAsInt(int enchantmentLevel) {
            return this.operator.applyAsInt(enchantmentLevel);
        }

        public static Operation remove() {
            return Screen.hasShiftDown() ? REMOVE_ALL : REMOVE;
        }

        public static Operation add() {
            return Screen.hasShiftDown() ? ADD_ALL : ADD;
        }
    }
}
