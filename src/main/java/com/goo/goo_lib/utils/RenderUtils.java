package com.goo.goo_lib.utils;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void writeQuad(VertexConsumer buffer, Matrix4f matrix, GlyphVertexData data, float alpha, float u0, float v0, float u1, float v1, int light) {
        buffer.addVertex(matrix, data.positions[0].x, data.positions[0].y, data.positions[0].z).setColor(data.reds[0], data.greens[0], data.blues[0], alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(matrix, data.positions[1].x, data.positions[1].y, data.positions[1].z).setColor(data.reds[1], data.greens[1], data.blues[1], alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[2].x, data.positions[2].y, data.positions[2].z).setColor(data.reds[2], data.greens[2], data.blues[2], alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[3].x, data.positions[3].y, data.positions[3].z).setColor(data.reds[3], data.greens[3], data.blues[3], alpha).setUv(u1, v0).setLight(light);
    }

    /**
     * Float variation of {@link GuiGraphics#fill(RenderType, int, int, int, int, int, int)}
     */
    public static void fillFloat(GuiGraphics graphics, float left, float top, float right, float bottom, int z, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, left, bottom, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, right, bottom, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, right, top, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, left, top, z).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /**
     * Float variation of {@link GuiGraphics#blitSprite(TextureAtlasSprite, int, int, int, int, int, int, int, int, int)}
     */
    public static void renderSpriteFloat(GuiGraphics graphics, TextureAtlasSprite sprite,
                                         float left, float top, float right, float bottom, int z,
                                         float r, float g, float b, float a) {
        if (sprite == null) return;

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, left, bottom, z).setUv(minU, maxV).setColor(r, g, b, a);
        buffer.addVertex(matrix, right, bottom, z).setUv(maxU, maxV).setColor(r, g, b, a);
        buffer.addVertex(matrix, right, top, z).setUv(maxU, minV).setColor(r, g, b, a);
        buffer.addVertex(matrix, left, top, z).setUv(minU, minV).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /**
     * Float variation of {@link GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float, float, float, float, float)}
     */
    public static void blitFloat(GuiGraphics graphics, ResourceLocation texture,
                                 float left, float top, float right, float bottom, int z) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, left, bottom, z).setUv(0, 1);
        buffer.addVertex(matrix, right, bottom, z).setUv(1, 1);
        buffer.addVertex(matrix, right, top, z).setUv(1, 0);
        buffer.addVertex(matrix, left, top, z).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
