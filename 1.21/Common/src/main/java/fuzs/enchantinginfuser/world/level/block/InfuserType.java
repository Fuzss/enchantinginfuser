package fuzs.enchantinginfuser.world.level.block;

import com.mojang.serialization.Codec;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;

public enum InfuserType implements StringRepresentable {
    NORMAL, ADVANCED;

    public static final Codec<InfuserType> CODEC = StringRepresentable.fromEnum(InfuserType::values);

    public TagKey<Enchantment> getAvailableEnchantments() {
        return switch (this) {
            case NORMAL -> ModRegistry.IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
            case ADVANCED -> ModRegistry.IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
        };
    }

    public MenuType<?> getMenuType() {
        return switch (this) {
            case NORMAL -> ModRegistry.INFUSING_MENU_TYPE.value();
            case ADVANCED -> ModRegistry.ADVANCED_INFUSING_MENU_TYPE.value();
        };
    }

    public ServerConfig.InfuserConfig getConfig() {
        return switch (this) {
            case NORMAL -> EnchantingInfuser.CONFIG.get(ServerConfig.class).normalInfuser;
            case ADVANCED -> EnchantingInfuser.CONFIG.get(ServerConfig.class).advancedInfuser;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
