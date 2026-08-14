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

public class WiggleEffect implements TextEffect<WiggleEffect.Config> {

    @Builder
    public record Config(float amplitude, float frequency, float phase) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("amplitude", 1F).forGetter(Config::amplitude),
                Codec.FLOAT.optionalFieldOf("frequency", 1F).forGetter(Config::frequency),
                Codec.FLOAT.optionalFieldOf("phase", 0F).forGetter(Config::frequency)
        ).apply(inst, Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {

        float angle = (codePoint % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);
        float delta = Mth.sin(Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001f) + index * config.phase()) * config.amplitude();
        vertexData.shiftCornerPosiions(dirX * delta, dirY * delta);

    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

}