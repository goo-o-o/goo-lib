package com.goo.goo_lib.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class TrailParticleType extends ParticleType<TrailParticleOption> {
    public TrailParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<TrailParticleOption> codec() {
        return TrailParticleOption.codec();
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, TrailParticleOption> streamCodec() {
        return TrailParticleOption.streamCodec();
    }
}