package fuzs.enchantinginfuser.world.level.block;

import com.mojang.serialization.Codec;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.item.enchantment.EnchantingBehavior;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.enchantinginfuser.world.item.enchantment.VanillaEnchantingBehavior;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;

public enum InfuserType implements StringRepresentable {
    NORMAL {
        @Override
        public TagKey<Enchantment> getAvailableEnchantments() {
            return ModRegistry.IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
        }

        @Override
        public MenuType<?> getMenuType() {
            return ModRegistry.INFUSING_MENU_TYPE.value();
        }

        @Override
        public ServerConfig.InfuserConfig getConfig() {
            return EnchantingInfuser.CONFIG.get(ServerConfig.class).normalInfuser;
        }
    },
    ADVANCED {
        @Override
        public TagKey<Enchantment> getAvailableEnchantments() {
            return ModRegistry.IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
        }

        @Override
        public MenuType<?> getMenuType() {
            return ModRegistry.ADVANCED_INFUSING_MENU_TYPE.value();
        }

        @Override
        public ServerConfig.InfuserConfig getConfig() {
            return EnchantingInfuser.CONFIG.get(ServerConfig.class).advancedInfuser;
        }
    };

    public static final Codec<InfuserType> CODEC = StringRepresentable.fromEnum(InfuserType::values);

    public abstract TagKey<Enchantment> getAvailableEnchantments();

    public abstract MenuType<?> getMenuType();

    public abstract ServerConfig.InfuserConfig getConfig();

    public EnchantingBehavior createBehavior() {
        return new VanillaEnchantingBehavior(this.getConfig());
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
