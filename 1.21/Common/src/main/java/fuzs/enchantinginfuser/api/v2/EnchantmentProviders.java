package fuzs.enchantinginfuser.api.v2;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.world.item.enchantment.VanillaEnchantingBehavior;
import fuzs.enchantinginfuser.world.item.enchantment.VanillaEnchantmentAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * main class of Enchanting Infuser Api
 */
public final class EnchantmentProviders {
    private static EnchantmentAdapter enchantmentAdapter = VanillaEnchantmentAdapter.INSTANCE;
    private static Function<ServerConfig.InfuserConfig, EnchantingBehavior> behaviorFactory;

    /**
     * Set a new {@link EnchantingBehavior}.
     * <p>Will fail and return <code>false</code> if the current provider returns a higher priority from {@link EnchantingBehavior#getPriority()}.
     *
     * @param newProvider the new provider
     * @return if successful
     */
    public static boolean setEnchantStatsProvider(@NotNull EnchantingBehavior newProvider) {
        Objects.requireNonNull(newProvider, "new provider is null");
        if (newProvider == VanillaEnchantingBehavior.INSTANCE || newProvider.getPriority() > EnchantmentProviders.enchantmentAdapter.getPriority()) {
            EnchantmentProviders.enchantmentAdapter = newProvider;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return get the current {@link EnchantingBehavior}
     */
    public static EnchantmentAdapter getAdapter() {
        Objects.requireNonNull(enchantmentAdapter, "provider is null");
        return enchantmentAdapter;
    }
}
