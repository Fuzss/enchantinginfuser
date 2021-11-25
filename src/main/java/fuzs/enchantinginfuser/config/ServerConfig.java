package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig extends AbstractConfig {
    @Config
    public final CostsConfig costs = new CostsConfig();

    public ServerConfig() {
        super("");
    }

    public static class CostsConfig extends AbstractConfig {
        @Config(description = "Base cost multiplier for each level for common enchantments.")
        public int commonCost = 1;
        @Config(description = "Base cost multiplier for each level for uncommon enchantments.")
        public int uncommonCost = 1;
        @Config(description = "Base cost multiplier for each level for rare enchantments.")
        public int rareCost = 1;
        @Config(description = "Base cost multiplier for each level for very rare enchantments.")
        public int veryRareCost = 1;

        public CostsConfig() {
            super("costs");
        }
    }
}