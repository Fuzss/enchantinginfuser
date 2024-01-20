package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.network.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.network.client.C2SAddEnchantLevelMessage;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.BuildCreativeModeTabContentsContext;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantingInfuser implements ModConstructor {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);
    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID, false);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
    }

    private static void registerMessages() {
        NETWORK.registerClientbound(S2CCompatibleEnchantsMessage.class, S2CCompatibleEnchantsMessage::new);
        NETWORK.registerServerbound(C2SAddEnchantLevelMessage.class, C2SAddEnchantLevelMessage::new);
    }

    @Override
    public void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsContext context) {
        context.registerBuildListener(CreativeModeTabs.FUNCTIONAL_BLOCKS, (itemDisplayParameters, output) -> {
            output.accept(ModRegistry.INFUSER_ITEM.value());
            output.accept(ModRegistry.ADVANCED_INFUSER_ITEM.value());
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
