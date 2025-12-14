package fuzs.enchantinginfuser.world.level.block;

import com.mojang.serialization.Codec;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.network.v4.codec.ExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;

public enum InfuserType implements StringRepresentable {
    NORMAL {
        @Override
        public TagKey<Enchantment> getAvailableEnchantments() {
            return ModRegistry.IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
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
        public ServerConfig.InfuserConfig getConfig() {
            return EnchantingInfuser.CONFIG.get(ServerConfig.class).advancedInfuser;
        }
    };

    public static final Codec<InfuserType> CODEC = StringRepresentable.fromEnum(InfuserType::values);
    public static final StreamCodec<ByteBuf, InfuserType> STREAM_CODEC = ExtraStreamCodecs.fromEnum(InfuserType.class);

    public abstract TagKey<Enchantment> getAvailableEnchantments();

    public abstract ServerConfig.InfuserConfig getConfig();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
