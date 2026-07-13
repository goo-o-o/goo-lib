package com.goo.goo_lib.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

public record ComponentParticleOption(
        ParticleType<?> particleType,
        Component component,
        int backgroundColor,
        boolean dropShadow
) implements ParticleOptions {

    public static MapCodec<ComponentParticleOption> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(ComponentParticleOption::particleType),
                ComponentSerialization.CODEC.fieldOf("component").forGetter(ComponentParticleOption::component),
                ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("background_color", 0x00000000).forGetter(ComponentParticleOption::backgroundColor),
                Codec.BOOL.optionalFieldOf("drop_shadow", true).forGetter(ComponentParticleOption::dropShadow)
        ).apply(instance, ComponentParticleOption::new));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ComponentParticleOption> streamCodec() {
        return StreamCodec.composite(
                ByteBufCodecs.registry(Registries.PARTICLE_TYPE), ComponentParticleOption::particleType,
                ComponentSerialization.STREAM_CODEC, ComponentParticleOption::component,
                ByteBufCodecs.INT, ComponentParticleOption::backgroundColor,
                ByteBufCodecs.BOOL, ComponentParticleOption::dropShadow,
                ComponentParticleOption::new
        );
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return particleType;
    }

}