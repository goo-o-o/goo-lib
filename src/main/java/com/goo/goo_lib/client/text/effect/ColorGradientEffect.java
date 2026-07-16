package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.util.ColorUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ColorGradientEffect implements TextEffect<ColorGradientEffect.Config> {

    public record Config(List<Integer> colors, float spread, float waveSpeed) {
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

        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                HEX_OR_INT_CODEC.listOf().fieldOf("colors").forGetter(Config::colors),
                Codec.FLOAT.fieldOf("spread").forGetter(Config::spread),
                Codec.FLOAT.fieldOf("wave_speed").forGetter(Config::waveSpeed)
        ).apply(inst, Config::new));
    }


    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, Config config) {
        float leftWorldX = pX + vertexData.positions[0].x;
        float rightWorldX = pX + vertexData.positions[2].x;

        // Pulling dynamic settings straight from the config template
        int colorLeft = ColorUtil.getGradientAt(leftWorldX, config.spread(), config.waveSpeed(), config.colors());
        int colorRight = ColorUtil.getGradientAt(rightWorldX, config.spread(), config.waveSpeed(), config.colors());

        applyRGB(vertexData, 0, colorLeft, dimFactor);
        applyRGB(vertexData, 1, colorLeft, dimFactor);
        applyRGB(vertexData, 2, colorRight, dimFactor);
        applyRGB(vertexData, 3, colorRight, dimFactor);
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    private void applyRGB(GlyphVertexData data, int index, int color, float dimFactor) {
        float r = ((color >> 16) & 255) / 255.0F * dimFactor;
        float g = ((color >> 8) & 255) / 255.0F * dimFactor;
        float b = (color & 255) / 255.0F * dimFactor;
        data.setCornerColor(index, r, g, b);
    }
}