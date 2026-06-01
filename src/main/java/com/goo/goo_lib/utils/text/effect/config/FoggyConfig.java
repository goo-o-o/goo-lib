package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.MapCodec;

public record FoggyConfig() implements EffectConfig {
    public static final FoggyConfig INSTANCE = new FoggyConfig();
    public static final MapCodec<FoggyConfig> CODEC = MapCodec.unit(INSTANCE);
}