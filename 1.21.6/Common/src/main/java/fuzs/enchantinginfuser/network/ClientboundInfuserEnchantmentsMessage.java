package fuzs.enchantinginfuser.network;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

public record ClientboundInfuserEnchantmentsMessage(int containerId,
                                                    Optional<ItemEnchantments> itemEnchantments) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundInfuserEnchantmentsMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundInfuserEnchantmentsMessage::containerId,
            ItemEnchantments.STREAM_CODEC.apply(ByteBufCodecs::optional),
            ClientboundInfuserEnchantmentsMessage::itemEnchantments,
            ClientboundInfuserEnchantmentsMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof InfuserMenu menu
                        && menu.containerId == ClientboundInfuserEnchantmentsMessage.this.containerId) {
                    menu.setInitialEnchantments(context.level(),
                            ClientboundInfuserEnchantmentsMessage.this.itemEnchantments);
                    if (context.client().screen instanceof InfuserScreen screen) {
                        screen.refreshSearchResults();
                    }
                }
            }
        };
    }
}
