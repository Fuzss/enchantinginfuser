package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.network.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.network.S2CInfuserDataMessage;
import fuzs.enchantinginfuser.network.client.C2SAddEnchantLevelMessage;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CommonFactories;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantingInfuser implements ModConstructor {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CommonFactories.INSTANCE.serverConfig(ServerConfig.class, () -> new ServerConfig());
    public static final NetworkHandler NETWORK = CommonFactories.INSTANCE.network(MOD_ID);

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
        ModRegistry.touch();
        registerMessages();
    }

    private static void registerMessages() {
        NETWORK.register(S2CCompatibleEnchantsMessage.class, S2CCompatibleEnchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CInfuserDataMessage.class, S2CInfuserDataMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SAddEnchantLevelMessage.class, C2SAddEnchantLevelMessage::new, MessageDirection.TO_SERVER);
    }
}
