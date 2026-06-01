package com.goo.goo_lib.utils.text.effect.base;

import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public interface QuadEffect {
    // Intended to use to draw quads over specific portions of text

    /**
     * Executed when a specific block of text containing this effect finishes measuring.
     *
     * @param minX         Starting X coordinate of the text segment.
     * @param maxX         Ending X coordinate of the text segment.
     * @param startY       Y coordinate of the text line.
     * @param matrix       The active transform pose matrix.
     * @param bufferSource The active MultiBufferSource batching geometries.
     */
    void renderQuad(float minX, float maxX, float startY, Matrix4f matrix, MultiBufferSource bufferSource);
}