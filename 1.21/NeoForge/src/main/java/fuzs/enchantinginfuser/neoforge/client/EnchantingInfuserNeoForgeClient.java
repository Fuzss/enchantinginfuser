package fuzs.enchantinginfuser.neoforge.client;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.EnchantingInfuserClient;
import fuzs.enchantinginfuser.data.client.ModLanguageProvider;
import fuzs.enchantinginfuser.neoforge.data.client.ModSpriteSourceProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = EnchantingInfuser.MOD_ID, dist = Dist.CLIENT)
public class EnchantingInfuserNeoForgeClient {

    public EnchantingInfuserNeoForgeClient() {
        ClientModConstructor.construct(EnchantingInfuser.MOD_ID, EnchantingInfuserClient::new);
        DataProviderHelper.registerDataProviders(EnchantingInfuser.MOD_ID,
                ModLanguageProvider::new,
                ModSpriteSourceProvider::new
        );
    }
}
