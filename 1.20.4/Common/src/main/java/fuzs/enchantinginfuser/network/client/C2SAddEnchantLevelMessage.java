package fuzs.enchantinginfuser.network.client;

import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.puzzleslib.api.network.v2.MessageV2;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

public class C2SAddEnchantLevelMessage implements MessageV2<C2SAddEnchantLevelMessage> {
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
        buf.writeInt(BuiltInRegistries.ENCHANTMENT.getId(this.enchantment));
        buf.writeBoolean(this.increase);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.enchantment = BuiltInRegistries.ENCHANTMENT.byId(buf.readInt());
        this.increase = buf.readBoolean();
    }

    @Override
    public MessageHandler<C2SAddEnchantLevelMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SAddEnchantLevelMessage message, Player player, Object gameInstance) {
                if (player.containerMenu.containerId == message.containerId && player.containerMenu instanceof InfuserMenu menu) {
                    menu.clickEnchantmentLevelButton(player, message.enchantment, message.increase);
                }
            }
        };
    }
}
