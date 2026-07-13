package com.goo.goo_lib.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class FlatParticleType extends ParticleType<FlatParticleOption> {
    public FlatParticleType(boolean overrideLimitter) {
        super(overrideLimitter);
    }

    @Override
    public @NotNull MapCodec<FlatParticleOption> codec() {
        return FlatParticleOption.codec();
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, FlatParticleOption> streamCodec() {
        return FlatParticleOption.streamCodec();
    }
}
