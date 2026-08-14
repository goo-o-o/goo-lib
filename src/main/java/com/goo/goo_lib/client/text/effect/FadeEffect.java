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

public class FadeEffect implements TextEffect<FadeEffect.Config> {

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    @Builder
    public record Config(float minAlpha, float frequency, float phase) {
        public static final MapCodec<FadeEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("minAlpha", 0.3F).forGetter(FadeEffect.Config::minAlpha),
                Codec.FLOAT.optionalFieldOf("frequency", 1.0F).forGetter(FadeEffect.Config::frequency), // in pixels
                Codec.FLOAT.optionalFieldOf("phase", 0.0F).forGetter(FadeEffect.Config::phase)
        ).apply(inst, FadeEffect.Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        float t = Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001f) + index * config.phase();
        float k = config.minAlpha() + (1f - config.minAlpha()) * (0.5f + 0.5f * Mth.sin(t));
        vertexData.mulAlphas(k);
    }
}
