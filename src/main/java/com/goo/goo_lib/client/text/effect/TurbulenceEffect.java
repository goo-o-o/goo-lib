package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class TurbulenceEffect implements TextEffect<TurbulenceEffect.Config> {

    @Builder
    public record Config(float amplitude, float frequency) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("amplitude", 1F).forGetter(Config::amplitude),
                Codec.FLOAT.optionalFieldOf("frequency", 1F).forGetter(Config::frequency)
        ).apply(inst, Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {

        float w = Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001f);
        float nx = Mth.sin(w * 1.7f + index * 0.31f + codePoint * 0.07f);
        float ny = Mth.sin(w * 2.3f + index * 0.27f + codePoint * 0.11f);
        vertexData.shiftCornerPosiions(nx * config.amplitude(), ny * config.amplitude());

    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

}