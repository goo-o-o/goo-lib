package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.MapCodec;

public record BloomConfig() implements EffectConfig {
    public static final BloomConfig INSTANCE = new BloomConfig();
    public static final MapCodec<BloomConfig> CODEC = MapCodec.unit(INSTANCE);
}