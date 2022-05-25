package fuzs.enchantinginfuser.network.message;

import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.puzzleslib.capability.data.CapabilityComponent;
import fuzs.puzzleslib.network.message.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class S2CSyncKnownEnchantsMessage implements Message {
    private CompoundTag tag;

    public S2CSyncKnownEnchantsMessage() {

    }

    public S2CSyncKnownEnchantsMessage(CapabilityComponent capability) {
        CompoundTag tag = new CompoundTag();
        capability.write(tag);
        this.tag = tag;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(this.tag);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    @Override
    public SyncKnownEnchantsHandler makeHandler() {
        return new SyncKnownEnchantsHandler();
    }

    private static class SyncKnownEnchantsHandler extends PacketHandler<S2CSyncKnownEnchantsMessage> {

        @Override
        public void handle(S2CSyncKnownEnchantsMessage packet, Player player, Object gameInstance) {
            player.getCapability(ModRegistry.KNOWN_ENCHANTS_CAPABILITY).ifPresent(capability -> {
                capability.read(packet.tag);
            });
        }
    }
}
