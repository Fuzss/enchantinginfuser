package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.compat.ModCompatHandler;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.handler.KnownEnchantsSyncHandler;
import fuzs.enchantinginfuser.network.client.message.C2SAddEnchantLevelMessage;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.network.message.S2CInfuserDataMessage;
import fuzs.enchantinginfuser.network.message.S2CSyncKnownEnchantsMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuser {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<AbstractConfig, ServerConfig> CONFIG = ConfigHolder.server(() -> new ServerConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        ModRegistry.touch();
        registerHandlers();
        registerMessages();
        ModCompatHandler.init();
    }

    private static void registerHandlers() {
        KnownEnchantsSyncHandler knownEnchantsSyncHandler = new KnownEnchantsSyncHandler();
        MinecraftForge.EVENT_BUS.addListener(knownEnchantsSyncHandler::onEntityJoinWorld);
    }

    private static void registerMessages() {
        NETWORK.register(S2CCompatibleEnchantsMessage.class, S2CCompatibleEnchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CInfuserDataMessage.class, S2CInfuserDataMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CSyncKnownEnchantsMessage.class, S2CSyncKnownEnchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SAddEnchantLevelMessage.class, C2SAddEnchantLevelMessage::new, MessageDirection.TO_SERVER);
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        ModCompatHandler.setup();
    }
}
