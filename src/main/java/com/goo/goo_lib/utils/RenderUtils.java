package com.goo.goo_lib.utils;

import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void writeQuad(VertexConsumer buffer, Matrix4f matrix, GlyphVertexData data, float alpha, float u0, float v0, float u1, float v1, int light) {
        buffer.addVertex(matrix, data.positions[0].x, data.positions[0].y, data.positions[0].z).setColor(data.reds[0], data.greens[0], data.blues[0], alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(matrix, data.positions[1].x, data.positions[1].y, data.positions[1].z).setColor(data.reds[1], data.greens[1], data.blues[1], alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[2].x, data.positions[2].y, data.positions[2].z).setColor(data.reds[2], data.greens[2], data.blues[2], alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[3].x, data.positions[3].y, data.positions[3].z).setColor(data.reds[3], data.greens[3], data.blues[3], alpha).setUv(u1, v0).setLight(light);
    }
}
