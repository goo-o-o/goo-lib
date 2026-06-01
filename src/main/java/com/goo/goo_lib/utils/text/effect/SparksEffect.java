package com.goo.goo_lib.utils.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.base.QuadEffect;
import com.goo.goo_lib.utils.text.effect.config.SparksConfig;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class SparksEffect implements TextEffect<SparksConfig>, QuadEffect {
    /**
     * @param vertexData The geometric layout data of the character glyph.
     * @param pX         Original horizontal drawing axis offset.
     * @param pY         Original vertical drawing axis offset.
     * @param dimFactor  Drop shadow intensity modifier.
     * @param config     The custom type-safe configuration object containing parameters.
     */
    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, SparksConfig config) {

    }

    /**
     * Executed when a specific block of text containing this effect finishes measuring.
     *
     * @param minX         Starting X coordinate of the text segment.
     * @param maxX         Ending X coordinate of the text segment.
     * @param startY       Y coordinate of the text line.
     * @param matrix       The active transform pose matrix.
     * @param bufferSource The active MultiBufferSource batching geometries.
     */
    @Override
    public void renderQuad(float minX, float maxX, float startY, Matrix4f matrix, MultiBufferSource bufferSource) {
        float maxY = startY + 9.0f;
        float padding = 5.0f;

        float finalMinX = minX - padding;
        float finalMaxX = maxX + padding;
        float finalMinY = startY - padding;
        float finalMaxY = maxY + padding;

        // Use your custom shader render type instead of guiOverlay
        VertexConsumer consumer = bufferSource.getBuffer(GLRenderTypes.sparksOverlay());

        // Strict Counter-Clockwise Winding Order with mapped UV coordinates (0.0 to 1.0)

        // Top-Left (U: 0.0, V: 0.0)
        consumer.addVertex(matrix, finalMinX, finalMinY, 0.0f)
                .setColor(1.0f, 0.8f, 0.0f, 0.3f)
                .setUv(0.0f, 0.0f);

        // Top-Right (U: 1.0, V: 0.0)
        consumer.addVertex(matrix, finalMaxX, finalMinY, 0.0f)
                .setColor(1.0f, 0.8f, 0.0f, 0.3f)
                .setUv(1.0f, 0.0f);

        // Bottom-Right (U: 1.0, V: 1.0)
        consumer.addVertex(matrix, finalMaxX, finalMaxY, 0.0f)
                .setColor(1.0f, 0.8f, 0.0f, 0.3f)
                .setUv(1.0f, 1.0f);

        // Bottom-Left (U: 0.0, V: 1.0)
        consumer.addVertex(matrix, finalMinX, finalMaxY, 0.0f)
                .setColor(1.0f, 0.8f, 0.0f, 0.3f)
                .setUv(0.0f, 1.0f);
    }
}
