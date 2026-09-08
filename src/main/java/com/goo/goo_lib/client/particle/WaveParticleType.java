package com.goo.goo_lib.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class WaveParticleType extends ParticleType<WaveParticleOption> {
    public WaveParticleType(boolean overrideLimitter) {
        super(overrideLimitter);
    }

    @Override
    public @NotNull MapCodec<WaveParticleOption> codec() {
        return WaveParticleOption.codec();
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, WaveParticleOption> streamCodec() {
        return WaveParticleOption.streamCodec();
    }
}
