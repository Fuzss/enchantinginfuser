package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuser {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        registerMessages();
        ModRegistry.touch();
    }

    private static void registerMessages() {
//        NETWORK.register(S2CEnchantingDataMessage.class, S2CEnchantingDataMessage::new, MessageDirection.TO_CLIENT);
    }
}
