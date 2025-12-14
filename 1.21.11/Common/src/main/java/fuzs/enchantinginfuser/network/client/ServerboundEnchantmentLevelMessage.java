package fuzs.enchantinginfuser.network.client;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v4.codec.ExtraStreamCodecs;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ServerboundPlayMessage;
import fuzs.puzzleslib.api.util.v1.CommonHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.IntUnaryOperator;

public record ServerboundEnchantmentLevelMessage(int containerId,
                                                 Holder<Enchantment> enchantment,
                                                 Operation operation) implements ServerboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEnchantmentLevelMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundEnchantmentLevelMessage::containerId,
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            ServerboundEnchantmentLevelMessage::enchantment,
            Operation.STREAM_CODEC,
            ServerboundEnchantmentLevelMessage::operation,
            ServerboundEnchantmentLevelMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof InfuserMenu menu
                        && menu.containerId == ServerboundEnchantmentLevelMessage.this.containerId) {
                    menu.clickEnchantmentLevelButton(ServerboundEnchantmentLevelMessage.this.enchantment,
                            ServerboundEnchantmentLevelMessage.this.operation);
                }
            }
        };
    }

    public enum Operation implements IntUnaryOperator {
        ADD((int enchantmentLevel) -> enchantmentLevel + 1),
        REMOVE((int enchantmentLevel) -> enchantmentLevel - 1),
        ADD_ALL(Integer.MAX_VALUE),
        REMOVE_ALL(Integer.MIN_VALUE);

        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC = ExtraStreamCodecs.fromEnum(Operation.class);

        private final IntUnaryOperator operator;

        Operation(int value) {
            this((int operand) -> value);
        }

        Operation(IntUnaryOperator operator) {
            this.operator = operator;
        }

        @Override
        public int applyAsInt(int enchantmentLevel) {
            return this.operator.applyAsInt(enchantmentLevel);
        }

        public static Operation remove() {
            return CommonHelper.hasShiftDown() ? REMOVE_ALL : REMOVE;
        }

        public static Operation add() {
            return CommonHelper.hasShiftDown() ? ADD_ALL : ADD;
        }
    }
}
