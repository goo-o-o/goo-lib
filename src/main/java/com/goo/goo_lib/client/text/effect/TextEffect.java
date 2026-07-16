package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

import java.util.List;

public interface TextEffect<C> {
    record RenderPass(Matrix4f matrix, int color) {}

    /**
     * @param vertexData The geometric layout data of the character glyph.
     * @param matrix
     * @param style
     * @param dropShadow
     * @param index
     * @param font
     * @param pX         Original horizontal drawing axis offset.
     * @param pY         Original vertical drawing axis offset.
     * @param dimFactor  Drop shadow intensity modifier.
     * @param codePoint
     * @param config     The custom type-safe configuration object containing parameters.
     */
    default void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, C config) {

    }

    default Matrix4f applyMatrixTransforms(GlyphVertexData data, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, int codepoint, C config) {
        return matrix;
    }

    default void addExtraRenderPasses(List<RenderPass> passes, GlyphVertexData vertexData, Matrix4f matrix, Style style, int index, Font font, float pX, float pY, int codepoint, C config) {
    }

    MapCodec<C> codec();

}