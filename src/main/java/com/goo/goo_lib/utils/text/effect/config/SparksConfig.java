package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.MapCodec;

public record SparksConfig() implements EffectConfig {
    public static final SparksConfig INSTANCE = new SparksConfig();
    public static final MapCodec<SparksConfig> CODEC = MapCodec.unit(INSTANCE);
}