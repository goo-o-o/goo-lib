package com.goo.goo_lib.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record FlatParticleOption(
        ParticleType<?> particleType,
        float radius,
        float rotX,
        float rotY,
        float rotZ
) implements ParticleOptions {

    public static MapCodec<FlatParticleOption> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(FlatParticleOption::particleType),
                Codec.FLOAT.fieldOf("radius").forGetter(FlatParticleOption::radius),
                Codec.FLOAT.fieldOf("rotX").forGetter(FlatParticleOption::rotX),
                Codec.FLOAT.fieldOf("rotY").forGetter(FlatParticleOption::rotY),
                Codec.FLOAT.fieldOf("rotZ").forGetter(FlatParticleOption::rotZ)
        ).apply(instance, FlatParticleOption::new));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, FlatParticleOption> streamCodec() {
        return StreamCodec.composite(
                ByteBufCodecs.registry(Registries.PARTICLE_TYPE), FlatParticleOption::particleType,
                ByteBufCodecs.FLOAT, FlatParticleOption::radius,
                ByteBufCodecs.FLOAT, FlatParticleOption::rotX,
                ByteBufCodecs.FLOAT, FlatParticleOption::rotY,
                ByteBufCodecs.FLOAT, FlatParticleOption::rotZ,
                FlatParticleOption::new
        );
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return this.particleType;
    }
}