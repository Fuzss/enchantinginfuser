package fuzs.enchantinginfuser.handler;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CSyncKnownEnchantsMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KnownEnchantsSyncHandler {

    @SubscribeEvent
    public void onEntityJoinWorld(final EntityJoinWorldEvent evt) {
        if (!EnchantingInfuser.CONFIG.server().limitedEnchantments) return;
        if (evt.getEntity() instanceof ServerPlayer player) {
            syncCapability(player);
        }
    }

    public static void syncCapability(ServerPlayer player) {
        player.getCapability(ModRegistry.KNOWN_ENCHANTS_CAPABILITY).ifPresent(capability -> {
            EnchantingInfuser.NETWORK.sendTo(new S2CSyncKnownEnchantsMessage(capability), player);
        });
    }
}
