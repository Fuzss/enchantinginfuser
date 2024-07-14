package fuzs.enchantinginfuser.neoforge;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.data.ModBlockLootProvider;
import fuzs.enchantinginfuser.data.ModBlockTagsProvider;
import fuzs.enchantinginfuser.data.ModEnchantmentTagsProvider;
import fuzs.enchantinginfuser.data.ModRecipeProvider;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.puzzleslib.neoforge.api.init.v3.capability.NeoForgeCapabilityHelper;
import net.neoforged.fml.common.Mod;

@Mod(EnchantingInfuser.MOD_ID)
public class EnchantingInfuserNeoForge {

    public EnchantingInfuserNeoForge() {
        ModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuser::new);
        NeoForgeCapabilityHelper.registerRestrictedBlockEntityContainer(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE);
        DataProviderHelper.registerDataProviders(EnchantingInfuser.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagsProvider::new,
                ModEnchantmentTagsProvider::new,
                ModRecipeProvider::new
        );
    }
}
