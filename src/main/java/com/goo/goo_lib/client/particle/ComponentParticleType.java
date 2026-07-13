package com.goo.goo_lib.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class ComponentParticleType extends ParticleType<ComponentParticleOption> {
    public ComponentParticleType(boolean overrideLimitter) {
        super(overrideLimitter);
    }

    @Override
    public @NotNull MapCodec<ComponentParticleOption> codec() {
        return ComponentParticleOption.codec();
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ComponentParticleOption> streamCodec() { return ComponentParticleOption.streamCodec();
    }
}
