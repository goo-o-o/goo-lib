package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.MapCodec;

public record FireConfig() implements EffectConfig {
    public static final FireConfig INSTANCE = new FireConfig();
    public static final MapCodec<FireConfig> CODEC = MapCodec.unit(INSTANCE);
}