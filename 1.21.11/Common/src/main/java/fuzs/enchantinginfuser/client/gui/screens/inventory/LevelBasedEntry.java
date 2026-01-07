package fuzs.enchantinginfuser.client.gui.screens.inventory;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface LevelBasedEntry<T> {
    int level();

    int maxLevel();

    int availableLevel();

    default boolean isPresent() {
        return this.level() > 0;
    }

    default boolean isIncompatible() {
        return false;
    }

    default boolean isInactive() {
        return this.isNotAvailable();
    }

    default boolean isNotAvailable() {
        return this.availableLevel() == 0;
    }

    Component getDisplayName(Holder<T> holder, int maxWidth, int seed);

    List<Component> getTooltip(Holder<T> holder);

    List<Component> getWeakPowerTooltip(Component component);
}
