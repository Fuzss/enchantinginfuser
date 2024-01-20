package fuzs.enchantinginfuser.neoforge;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.api.v2.EnchantingInfuserApi;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.data.ModBlockLootProvider;
import fuzs.enchantinginfuser.data.ModBlockTagsProvider;
import fuzs.enchantinginfuser.data.ModEnchantmentTagsProvider;
import fuzs.enchantinginfuser.data.ModRecipeProvider;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.neoforge.data.client.ModSpriteSourceProvider;
import fuzs.enchantinginfuser.world.item.enchantment.VanillaEnchantStatsProvider;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.neoforge.api.core.v1.NeoForgeModContainerHelper;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuserNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
        registerHandlers();
        registerIntegration();
        DataProviderHelper.registerDataProviders(EnchantingInfuser.MOD_ID, ModBlockLootProvider::new, ModBlockTagsProvider::new, ModEnchantmentTagsProvider::new, ModRecipeProvider::new, ModSpriteSourceProvider::new);
    }

    private static void registerHandlers() {
        NeoForgeModContainerHelper.getModEventBus(EnchantingInfuser.MOD_ID).addListener((final RegisterCapabilitiesEvent evt) -> {
            evt.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.value(), (InfuserBlockEntity blockEntity, @Nullable Direction direction) -> {
                // Always use sided wrapper so that WorldlyContainer::canPlaceItemThroughFace is called
                return new SidedInvWrapper(blockEntity, null);
            });
        });
    }

    private static void registerIntegration() {
        if (false) {
            EnchantingInfuser.CONFIG.getHolder(ServerConfig.class).accept(() -> {
                if (ModLoaderEnvironment.INSTANCE.isModLoaded("apotheosis") && EnchantingInfuser.CONFIG.get(ServerConfig.class).apotheosisIntegration) {
    //                EnchantingInfuserApi.setEnchantStatsProvider(ApotheosisEnchantStatsProvider.INSTANCE);
                } else {
                    EnchantingInfuserApi.setEnchantStatsProvider(VanillaEnchantStatsProvider.INSTANCE);
                }
            });
        }
    }
}
