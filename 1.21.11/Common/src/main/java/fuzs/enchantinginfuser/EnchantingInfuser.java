package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.network.ClientboundInfuserEnchantmentsMessage;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.event.v1.BuildCreativeModeTabContentsCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantingInfuser implements ModConstructor {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);
    public static final Identifier TREASURE_ENCHANTMENTS_LOCATION = EnchantingInfuser.id("treasure_enchantments");

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerLoadingHandlers();
    }

    private static void registerLoadingHandlers() {
        BuildCreativeModeTabContentsCallback.buildCreativeModeTabContents(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register((CreativeModeTab creativeModeTab, CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) -> {
                    output.accept(ModRegistry.INFUSER_ITEM.value());
                    output.accept(ModRegistry.ADVANCED_INFUSER_ITEM.value());
                });
    }

    @Override
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToClient(ClientboundInfuserEnchantmentsMessage.class,
                ClientboundInfuserEnchantmentsMessage.STREAM_CODEC);
        context.playToServer(ServerboundEnchantmentLevelMessage.class, ServerboundEnchantmentLevelMessage.STREAM_CODEC);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        context.registerBuiltInPack(TREASURE_ENCHANTMENTS_LOCATION, Component.literal("Treasure Enchantments"), false);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
