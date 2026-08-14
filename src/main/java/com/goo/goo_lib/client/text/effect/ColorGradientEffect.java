package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.util.color.TooltipColorUtil;
import com.goo.goo_lib.util.GLCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

import java.util.List;

public class ColorGradientEffect implements TextEffect<ColorGradientEffect.Config> {

    @Builder
    public record Config(List<Integer> colors, float spread, float waveSpeed) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GLCodecs.UNIVERSAL_COLOR_CODEC.listOf().fieldOf("colors").forGetter(Config::colors),
                Codec.FLOAT.fieldOf("spread").forGetter(Config::spread),
                Codec.FLOAT.fieldOf("wave_speed").forGetter(Config::waveSpeed)
        ).apply(inst, Config::new));
    }


    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        float leftWorldX = pX + vertexData.positions[0].x;
        float rightWorldX = pX + vertexData.positions[2].x;

        // Pulling dynamic settings straight from the config template
        int colorLeft = TooltipColorUtil.getGradientAt(leftWorldX, config.spread(), config.waveSpeed(), config.colors());
        int colorRight = TooltipColorUtil.getGradientAt(rightWorldX, config.spread(), config.waveSpeed(), config.colors());

        applyRGBA(vertexData, 0, colorLeft, dimFactor);
        applyRGBA(vertexData, 1, colorLeft, dimFactor);
        applyRGBA(vertexData, 2, colorRight, dimFactor);
        applyRGBA(vertexData, 3, colorRight, dimFactor);
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    private void applyRGBA(GlyphVertexData data, int index, int color, float dimFactor) {
        float r = FastColor.ARGB32.red(color) / 255F * dimFactor;
        float g = FastColor.ARGB32.green(color) / 255F * dimFactor;
        float b = FastColor.ARGB32.blue(color) / 255F * dimFactor;
        float a = FastColor.ARGB32.alpha(color) / 255F * dimFactor;
        data.setCornerColor(index, r, g, b, a);
    }
}