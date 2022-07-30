package fuzs.enchantinginfuser.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

/**
 * need to make this a common config instead of server as the value is used during start-up
 */
public class CommonConfig implements ConfigCore {
    @Config(description = {"Enable compat for Apotheosis if it is installed. Allows for using the full range of changes Apotheosis applies to vanilla enchantments.", "Should only really be disabled if compat breaks due to internal changes."})
    public boolean apotheosisCompat = true;
}
