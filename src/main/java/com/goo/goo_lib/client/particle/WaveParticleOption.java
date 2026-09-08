package com.goo.goo_lib.client.particle;

import com.goo.goo_lib.util.Easing;
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

public record WaveParticleOption(
        ParticleType<?> particleType,
        float radius,
        float rotX,
        float rotY,
        float rotZ,
        int growthDuration,
        Easing easing
) implements ParticleOptions {

    public static MapCodec<WaveParticleOption> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(WaveParticleOption::particleType),
                Codec.FLOAT.fieldOf("radius").forGetter(WaveParticleOption::radius),
                Codec.FLOAT.fieldOf("rotX").forGetter(WaveParticleOption::rotX),
                Codec.FLOAT.fieldOf("rotY").forGetter(WaveParticleOption::rotY),
                Codec.FLOAT.fieldOf("rotZ").forGetter(WaveParticleOption::rotZ),
                Codec.INT.fieldOf("growthDuration").forGetter(WaveParticleOption::growthDuration),
                Codec.INT.xmap(id -> Easing.values()[id], Easing::ordinal).fieldOf("easing").forGetter(WaveParticleOption::easing)
        ).apply(instance, WaveParticleOption::new));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, WaveParticleOption> streamCodec() {
        return StreamCodec.of(
                (buf, option) -> {
                    ByteBufCodecs.registry(Registries.PARTICLE_TYPE).encode(buf, option.particleType());
                    buf.writeFloat(option.radius());
                    buf.writeFloat(option.rotX());
                    buf.writeFloat(option.rotY());
                    buf.writeFloat(option.rotZ());
                    buf.writeVarInt(option.growthDuration());
                    buf.writeEnum(option.easing());
                },
                buf -> new WaveParticleOption(
                        ByteBufCodecs.registry(Registries.PARTICLE_TYPE).decode(buf),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readVarInt(),
                        buf.readEnum(Easing.class)
                )
        );
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return this.particleType;
    }
}