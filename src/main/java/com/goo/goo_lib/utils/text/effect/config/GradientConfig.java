package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record GradientConfig(List<Integer> colors, float spread, float waveSpeed) implements EffectConfig {
    public static final Codec<Integer> HEX_OR_INT_CODEC = Codec.STRING.flatXmap(
            s -> {
                try {
                    int value = s.startsWith("#")
                            ? Integer.parseUnsignedInt(s.substring(1), 16)
                            : s.startsWith("0x") || s.startsWith("0X")
                              ? Integer.parseUnsignedInt(s.substring(2), 16)
                              : Integer.parseInt(s);
                    return DataResult.success(value);
                } catch (NumberFormatException e) {
                    return DataResult.error(() -> "Invalid color: " + s);
                }
            },
            i -> DataResult.success(String.format("#%08X", i))
    ).xmap(Integer::intValue, Integer::intValue);

    public static final MapCodec<GradientConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            HEX_OR_INT_CODEC.listOf().fieldOf("colors").forGetter(GradientConfig::colors),
            Codec.FLOAT.fieldOf("spread").forGetter(GradientConfig::spread),
            Codec.FLOAT.fieldOf("wave_speed").forGetter(GradientConfig::waveSpeed)
    ).apply(inst, GradientConfig::new));
}