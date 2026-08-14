package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
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

public class SwingEffect implements TextEffect<SwingEffect.Config> {

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    @Builder
    public record Config(float frequency, float angle, float phase) {
        public static final MapCodec<SwingEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("frequency", 1F).forGetter(SwingEffect.Config::frequency),
                Codec.FLOAT.optionalFieldOf("angle", 20F).forGetter(SwingEffect.Config::angle),
                Codec.FLOAT.optionalFieldOf("phase", 0F).forGetter(SwingEffect.Config::phase)
        ).apply(inst, SwingEffect.Config::new));
    }


    @Override
    public Matrix4f applyMatrixTransforms(GlyphVertexData data, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, int codePoint, Config config) {
        float w = Mth.TWO_PI * config.frequency() * (Util.getMillis() * 0.001f) + index * config.phase();

        float angleRad = (float) ((Math.sin(w) * config.angle()) * Mth.DEG_TO_RAD);
        Matrix4f renderMatrix = new Matrix4f(matrix);

        FontSet fontSet = font.getFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, font.filterFishyGlyphs);

        float halfWidth = glyphInfo.getAdvance(style.isBold()) / 2.0F;

        float halfHeight = font.lineHeight / 2.0F;

        float centerX = pX + halfWidth;
        float centerY = pY + halfHeight;

        renderMatrix.translate(centerX, centerY, 0.0F);
        renderMatrix.rotateZ(angleRad);
        renderMatrix.translate(-centerX, -centerY, 0.0F);

        return renderMatrix;

    }
}
