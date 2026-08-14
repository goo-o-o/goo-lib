package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
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

public class OutlineEffect implements TextEffect<OutlineEffect.Config> {

    @Builder
    public record Config(Integer color, float thickness, float alpha) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GLCodecs.UNIVERSAL_COLOR_CODEC.optionalFieldOf("color", FastColor.ARGB32.color(255,255,255,255)).forGetter(Config::color),
                Codec.FLOAT.optionalFieldOf("thickness", 1F).forGetter(Config::thickness),
                Codec.FLOAT.optionalFieldOf("alpha", 1F).forGetter(Config::alpha)
        ).apply(inst, Config::new));
    }

    @Override
    public void addExtraRenderPasses(List<RenderPass> passes, GlyphVertexData vertexData, Matrix4f matrix, Style style, int index, Font font, float pX, float pY, int codePoint, Config config) {
        float width = config.thickness();

        float[][] offsets = {
                {-width, -width}, {-width, 0f}, {-width, width},
                {0f, -width}, {0f, width},
                {width, -width}, {width, 0f}, {width, width}
        };

        for (float[] off : offsets) {
            Matrix4f passMatrix = new Matrix4f(matrix);
            passMatrix.translate(off[0], off[1], -0.0F);

            passes.add(new RenderPass(passMatrix, config.color));
        }
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

}