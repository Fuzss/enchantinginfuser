package fuzs.enchantinginfuser.api.v2;

import fuzs.enchantinginfuser.world.item.enchantment.VanillaEnchantStatsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * main class of Enchanting Infuser Api
 */
public final class EnchantingInfuserApi {
    private static EnchantStatsProvider provider = VanillaEnchantStatsProvider.INSTANCE;

    /**
     * Set a new {@link EnchantStatsProvider}.
     * <p>Will fail and return <code>false</code> if the current provider returns a higher priority from {@link EnchantStatsProvider#getPriority()}.
     *
     * @param newProvider the new provider
     * @return if successful
     */
    public static boolean setEnchantStatsProvider(@NotNull EnchantStatsProvider newProvider) {
        Objects.requireNonNull(newProvider, "new provider is null");
        if (newProvider == VanillaEnchantStatsProvider.INSTANCE || newProvider.getPriority() > EnchantingInfuserApi.provider.getPriority()) {
            EnchantingInfuserApi.provider = newProvider;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return get the current {@link EnchantStatsProvider}
     */
    public static EnchantStatsProvider getEnchantStatsProvider() {
        Objects.requireNonNull(EnchantingInfuserApi.provider, "provider is null");
        return EnchantingInfuserApi.provider;
    }
}
