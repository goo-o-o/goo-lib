package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.util.GLCodecs;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class PendulumEffect implements TextEffect<PendulumEffect.Config> {

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    @Builder
    public record Config(float frequency, float angle, float radius, Vector2f offset) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("frequency", 1.0F).forGetter(Config::frequency),
                Codec.FLOAT.optionalFieldOf("angle", 30.0F).forGetter(Config::angle),
                Codec.FLOAT.optionalFieldOf("radius", 0.0F).forGetter(Config::radius),
                GLCodecs.VECTOR2F.optionalFieldOf("offset", new Vector2f(0,0)).forGetter(Config::offset)
        ).apply(inst, Config::new));
    }


    @Override
    public Matrix4f applyMatrixTransforms(GlyphVertexData data, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, int codePoint, Config config) {
        if (config.angle() == 0) return matrix;

        double phase = (Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001)) - (index * 0.5);
        float angleRad = (float) ((Math.sin(phase) * config.angle()) * Mth.DEG_TO_RAD);
        Matrix4f renderMatrix = new Matrix4f(matrix);

        FontSet fontSet = font.getFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, font.filterFishyGlyphs);

        float halfWidth = glyphInfo.getAdvance(style.isBold()) / 2.0F;

        float halfHeight = font.lineHeight / 2.0F;

        Vector2f offset = config.offset();
        float centerX = pX + halfWidth + offset.x;
        float centerY = pY + halfHeight + offset.y;

        renderMatrix.translate(centerX, centerY, 0.0F);
        renderMatrix.rotateZ(angleRad);
        renderMatrix.translate(-centerX, -centerY, 0.0F);

        return renderMatrix;
    }
}
