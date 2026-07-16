package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class CircleEffect implements TextEffect<CircleEffect.Config> {

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    public record Config(float radius, float frequency, float phase) {
        public static final MapCodec<CircleEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("radius", 1.0F).forGetter(CircleEffect.Config::radius),
                Codec.FLOAT.optionalFieldOf("frequency", 1.0F).forGetter(CircleEffect.Config::frequency), // in pixels
                Codec.FLOAT.optionalFieldOf("phase", 0.0F).forGetter(CircleEffect.Config::phase)
        ).apply(inst, CircleEffect.Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        float t = Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001F) + index * config.phase();
        float waveX = Mth.cos(t) * config.radius();
        float waveY = Mth.sin(t) * config.radius();

        vertexData.shiftCornerPosiions(waveX, waveY);
    }
}
