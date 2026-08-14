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

import java.util.Optional;

public record TrailParticleOption(
        ParticleType<?> particleType,
        Integer entityId,
        Float minVertexDistance,
        Float width,
        boolean smoothInterpolation,
        boolean bloom
) implements ParticleOptions {
    public TrailParticleOption {
        if (minVertexDistance == null) minVertexDistance = 0.1F;
        if (width == null) width = 0.15F;
    }
    public static MapCodec<TrailParticleOption> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(TrailParticleOption::particleType),
                Codec.INT.optionalFieldOf("entityId").forGetter(options -> Optional.ofNullable(options.entityId())),
                Codec.FLOAT.optionalFieldOf("minVertexDistance", 0.1F).forGetter(TrailParticleOption::minVertexDistance),
                Codec.FLOAT.optionalFieldOf("width", 0.15F).forGetter(TrailParticleOption::width),
                Codec.BOOL.fieldOf("smoothInterpolation").forGetter(TrailParticleOption::smoothInterpolation),
                Codec.BOOL.optionalFieldOf("bloom", false).forGetter(TrailParticleOption::bloom)
        ).apply(instance, (type, entityIdOpt, minVertexDistance, width, smooth, bloom) ->
                new TrailParticleOption(type, entityIdOpt.orElse(null), minVertexDistance, width, smooth, bloom)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, TrailParticleOption> streamCodec() {
        return StreamCodec.composite(
                ByteBufCodecs.registry(Registries.PARTICLE_TYPE), TrailParticleOption::particleType,
                ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), options -> Optional.ofNullable(options.entityId()),
                ByteBufCodecs.optional(ByteBufCodecs.FLOAT), options -> Optional.ofNullable(options.minVertexDistance()),
                ByteBufCodecs.optional(ByteBufCodecs.FLOAT), options -> Optional.ofNullable(options.width()),
                ByteBufCodecs.BOOL, TrailParticleOption::smoothInterpolation,
                ByteBufCodecs.BOOL, TrailParticleOption::bloom,
                (type, entityIdOpt, minVertexDistOpt, widthOpt, smooth, bloom) ->
                        new TrailParticleOption(
                                type,
                                entityIdOpt.orElse(null),
                                minVertexDistOpt.orElse(0.1F),
                                widthOpt.orElse(0.15F),
                                smooth,
                                bloom
                        )
        );
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return this.particleType;
    }
}