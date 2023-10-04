package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.data.ModBlockLootProvider;
import fuzs.enchantinginfuser.data.ModBlockTagsProvider;
import fuzs.enchantinginfuser.data.ModRecipeProvider;
import fuzs.enchantinginfuser.data.ModSpriteSourceProvider;
import fuzs.enchantinginfuser.init.ForgeModRegistry;
import fuzs.enchantinginfuser.integration.ApotheosisEnchantStatsProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.concurrent.CompletableFuture;

@Mod(EnchantingInfuser.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantingInfuserForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
        ForgeModRegistry.touch();
        registerIntegration();
    }

    private static void registerIntegration() {
        EnchantingInfuser.CONFIG.getHolder(ServerConfig.class).accept(() -> {
            if (!EnchantingInfuser.CONFIG.get(ServerConfig.class).apotheosisIntegration) {
                EnchantingInfuserAPI.setEnchantStatsProvider(EnchantStatsProvider.INSTANCE);
            } else if (ModLoaderEnvironment.INSTANCE.isModLoaded("apotheosis")) {
                EnchantingInfuserAPI.setEnchantStatsProvider(ApotheosisEnchantStatsProvider.INSTANCE);
            }
        });
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        final DataGenerator dataGenerator = evt.getGenerator();
        final PackOutput packOutput = dataGenerator.getPackOutput();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = evt.getLookupProvider();
        final ExistingFileHelper fileHelper = evt.getExistingFileHelper();
        dataGenerator.addProvider(true, new ModBlockLootProvider(packOutput, EnchantingInfuser.MOD_ID));
        dataGenerator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider, EnchantingInfuser.MOD_ID, fileHelper));
        dataGenerator.addProvider(true, new ModRecipeProvider(packOutput));
        dataGenerator.addProvider(true, new ModSpriteSourceProvider(packOutput, fileHelper));
    }
}
