package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.client.particle.ComponentParticleOption;
import com.goo.goo_lib.client.particle.ComponentParticleType;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GLParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, GooLib.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ComponentParticleOption>> COMPONENT_PARTICLE = PARTICLE_TYPES.register(
            "component_particle", () -> new ComponentParticleType(true)
    );
}
