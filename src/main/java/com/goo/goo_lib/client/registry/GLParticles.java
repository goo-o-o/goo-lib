package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.client.particle.ComponentParticleOption;
import com.goo.goo_lib.common.GooLib;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class GLParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, GooLib.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ComponentParticleOption>> COMPONENT_PARTICLE = PARTICLE_TYPES.register(
            "component_particle", () -> new ParticleType<ComponentParticleOption>(true) {
                @Override
                public @NotNull MapCodec<ComponentParticleOption> codec() {
                    return ComponentParticleOption.codec(this);
                }

                @Override
                public @NotNull StreamCodec<RegistryFriendlyByteBuf, ComponentParticleOption> streamCodec() {
                    return ComponentParticleOption.streamCodec(this);
                }
            }
    );
}
