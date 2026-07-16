package com.goo.goo_lib.common.registry;


import com.goo.goo_lib.client.text.EffectType;
import com.goo.goo_lib.client.text.effect.*;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.common.GooLib;
import com.mojang.serialization.Codec;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class TextEffects {
    public static final Codec<List<ConfiguredEffect<?>>> CONFIGURED_EFFECT_LIST_CODEC =
            Codec.lazyInitialized(() -> ConfiguredEffect.codec(TextEffects.REGISTRY.getRegistry().get()).listOf());

    // Create the root dynamic registry container
    public static final DeferredRegister<EffectType<?>> REGISTRY =
            DeferredRegister.create(EffectType.REGISTRY_KEY, GooLib.MOD_ID);

    public static final Supplier<EffectType<ColorGradientEffect.Config>> COLOR_GRADIENT_TYPE = register("color_gradient", ColorGradientEffect::new);
    public static final Supplier<EffectType<WaveEffect.Config>> WAVE_TYPE = register("wave", WaveEffect::new);
    public static final Supplier<EffectType<SmoothWaveEffect.Config>> SMOOTH_WAVE_TYPE = register("smooth_wave", SmoothWaveEffect::new);
    public static final Supplier<EffectType<AcidEffect.Config>> ACID_TYPE = register("acid", AcidEffect::new);
    public static final Supplier<EffectType<CircleEffect.Config>> CIRCLE_TYPE = register("circle", CircleEffect::new);
    public static final Supplier<EffectType<FadeEffect.Config>> FADE_TYPE = register("fade", FadeEffect::new);
    public static final Supplier<EffectType<PendulumEffect.Config>> PENDULUM_TYPE = register("pendulum", PendulumEffect::new);
    public static final Supplier<EffectType<TurbulenceEffect.Config>> TURBULENCE_TYPE = register("turbulence", TurbulenceEffect::new);
    public static final Supplier<EffectType<WiggleEffect.Config>> WIGGLE_TYPE = register("wiggle", WiggleEffect::new);
    public static final Supplier<EffectType<JitterEffect.Config>> JITTER_TYPE = register("jitter", JitterEffect::new);
    public static final Supplier<EffectType<SwingEffect.Config>> SWING_TYPE = register("swing", SwingEffect::new);
    public static final Supplier<EffectType<OutlineEffect.Config>> OUTLINE_TYPE = register("outline", OutlineEffect::new);
    public static final Supplier<EffectType<ShakeEffect.Config>> SHAKE_TYPE = register("shake", ShakeEffect::new);
    public static final Supplier<EffectType<Float>> BLOOM_TYPE = register("bloom", BloomEffect::new);
    public static final Supplier<EffectType<Unit>> FOGGY_TYPE = register("foggy", FoggyEffect::new);
    public static final Supplier<EffectType<Unit>> FIRE_TYPE = register("fire", FireEffect::new);

    public static void init(IEventBus modBus) {
        // NeoForge passes a consumer configuring a builder; we catch it and do nothing to keep default settings
        REGISTRY.makeRegistry(builder -> {
        });
        REGISTRY.register(modBus);
    }

    public static <C, E extends TextEffect<C>> Supplier<EffectType<C>> register(String name, Supplier<E> effectFactory) {
        return REGISTRY.register(name, () -> {
            E effect = effectFactory.get();
            return new EffectType<>(effect.codec(), config -> effect);
        });
    }
}