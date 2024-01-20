package fuzs.enchantinginfuser.api;

import fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main class of Enchanting Infuser API
 */
public class EnchantingInfuserAPI {
    public static final Logger LOGGER = LoggerFactory.getLogger("Enchanting Infuser API");

    /**
     * the {@link EnchantStatsProvider} that will be used for retrieving certain information from enchantments
     */
    private static EnchantStatsProvider enchantStatsProvider = EnchantStatsProvider.INSTANCE;

    /**
     * @param provider the new provider
     * @return 'true' if <code>provider</code> was successfully set as the new {@link #enchantStatsProvider}, 'false' if a provider with a higher priority (defined by {@link EnchantStatsProvider#getPriority()}) is already present
     */
    public static synchronized boolean setEnchantStatsProvider(EnchantStatsProvider provider) {
        if (provider == null) throw new IllegalStateException();
        if (enchantStatsProvider != null) {
            if (provider != EnchantStatsProvider.INSTANCE && provider.getPriority() <= enchantStatsProvider.getPriority()) {
                return false;
            }
        }
        enchantStatsProvider = provider;
        LOGGER.info("Set new EnchantStatsProvider for mod {}", provider.getSourceNamespace());
        return true;
    }

    /**
     * @return get the current {@link EnchantStatsProvider}
     */
    public static EnchantStatsProvider getEnchantStatsProvider() {
        if (enchantStatsProvider == null) throw new IllegalStateException();
        return enchantStatsProvider;
    }
}
