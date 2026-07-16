package com.goo.goo_lib.util;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderUtil {

    /**
     * Renders a double sided quad
     */
    public static void drawDoubleSidedQuad(VertexConsumer consumer, Matrix4f matrix,
                                         float width, float height,
                                         float u0, float u1, float v0, float v1,
                                         int packedLight, int r, int g, int b, int a) {
        float halfWidth = width / 2;
        // front
        consumer.addVertex(matrix, -halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);

        // back
        consumer.addVertex(matrix, halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, -halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, -halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
    }

    public static void drawQuad(VertexConsumer consumer, Matrix4f matrix,
                                           float width, float height,
                                           float u0, float u1, float v0, float v1,
                                           int packedLight, int r, int g, int b, int a) {
        float halfWidth = width / 2;
        // front
        consumer.addVertex(matrix, -halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfWidth, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfWidth, height, 0.0F).setColor(r, g, b, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
    }

    public static void drawGlyphQuad(VertexConsumer buffer, Matrix4f matrix, GlyphVertexData data, float u0, float v0, float u1, float v1, int light) {
        buffer.addVertex(matrix, data.positions[0].x, data.positions[0].y, data.positions[0].z).setColor(data.reds[0], data.greens[0], data.blues[0], data.alphas[0]).setUv(u0, v0).setLight(light);
        buffer.addVertex(matrix, data.positions[1].x, data.positions[1].y, data.positions[1].z).setColor(data.reds[1], data.greens[1], data.blues[1], data.alphas[1]).setUv(u0, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[2].x, data.positions[2].y, data.positions[2].z).setColor(data.reds[2], data.greens[2], data.blues[2], data.alphas[2]).setUv(u1, v1).setLight(light);
        buffer.addVertex(matrix, data.positions[3].x, data.positions[3].y, data.positions[3].z).setColor(data.reds[3], data.greens[3], data.blues[3], data.alphas[3]).setUv(u1, v0).setLight(light);
    }

    /**
     * {@link GuiGraphics#fill(RenderType, int, int, int, int, int, int)} with float support
     */
    public static void fillWithUv(
            RenderType renderType, GuiGraphics guiGraphics,
            float x1, float y1, float x2, float y2, float z, int color) {

        float nextX1 = Math.max(x1, x2);
        float nextX2 = Math.min(x1, x2);

        float nextY1 = Math.max(y1, y2);
        float nextY2 = Math.min(y1, y2);

        x1 = nextX1;
        x2 = nextX2;
        y1 = nextY1;
        y2 = nextY2;

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(renderType);

        vertexconsumer.addVertex(matrix4f, x1, y1, z).setColor(red, green, blue, alpha).setUv(0, 0);
        vertexconsumer.addVertex(matrix4f, x1, y2, z).setColor(red, green, blue, alpha).setUv(0, 1);
        vertexconsumer.addVertex(matrix4f, x2, y2, z).setColor(red, green, blue, alpha).setUv(1, 1);
        vertexconsumer.addVertex(matrix4f, x2, y1, z).setColor(red, green, blue, alpha).setUv(1, 0);
    }

    /**
     * {@link GuiGraphics#fill(RenderType, int, int, int, int, int, int)} with float support and uv support
     */
    public static void fill(
            RenderType renderType, GuiGraphics guiGraphics,
            float x1, float y1, float x2, float y2, float z, int color) {

        float nextX1 = Math.max(x1, x2);
        float nextX2 = Math.min(x1, x2);

        float nextY1 = Math.max(y1, y2);
        float nextY2 = Math.min(y1, y2);

        x1 = nextX1;
        x2 = nextX2;
        y1 = nextY1;
        y2 = nextY2;

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(renderType);

        vertexconsumer.addVertex(matrix4f, x1, y1, z).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, x1, y2, z).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, x2, y2, z).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, x2, y1, z).setColor(red, green, blue, alpha);
    }

    /**
     * Float variation of {@link GuiGraphics#blitSprite(TextureAtlasSprite, int, int, int, int, int, int, int, int, int)}
     */
    public static void renderSpriteFloat(GuiGraphics graphics, TextureAtlasSprite sprite, float left, float top, float right, float bottom, int z, float r, float g, float b, float a) {
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
     * {@link GuiGraphics#blit(int, int, int, int, int, TextureAtlasSprite)} with float support
     */
    public static void blit(GuiGraphics gui, float x, float y, float blitOffset, float width, float height, TextureAtlasSprite sprite) {
        blitSprite(gui, sprite, x, y, blitOffset, width, height);
    }

    /**
     * {@link GuiGraphics#blit(int, int, int, int, int, TextureAtlasSprite, float, float, float, float)} with float support
     */
    public static void blit(GuiGraphics gui, float x, float y, float blitOffset, float width, float height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
        innerBlit(gui, sprite.atlasLocation(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), red, green, blue, alpha);
    }

    /**
     * {@link GuiGraphics#blitSprite(ResourceLocation, int, int, int, int)} with float support
     */
    public static void blitSprite(GuiGraphics gui, ResourceLocation sprite, float x, float y, float width, float height) {
        blitSprite(gui, sprite, x, y, 0, width, height);
    }

    /**
     * {@link GuiGraphics#blitSprite(TextureAtlasSprite, int, int, int, int, int)} with float support
     */
    public static void blitSprite(GuiGraphics gui, ResourceLocation sprite, float x, float y, float blitOffset, float width, float height) {
        TextureAtlasSprite textureAtlasSprite = gui.sprites.getSprite(sprite);
        GuiSpriteScaling spriteScaling = gui.sprites.getSpriteScaling(textureAtlasSprite);
        switch (spriteScaling) {
            case GuiSpriteScaling.Stretch stretch ->
                    blitSprite(gui, textureAtlasSprite, x, y, blitOffset, width, height);
            case GuiSpriteScaling.Tile(int width1, int height1) ->
                    blitTiledSprite(gui, textureAtlasSprite, x, y, blitOffset, width, height, 0, 0, width1, height1, width1, height1);
            case GuiSpriteScaling.NineSlice guispritescaling$nineslice ->
                    blitNineSlicedSprite(gui, textureAtlasSprite, guispritescaling$nineslice, x, y, blitOffset, width, height);
            default -> {
            }
        }
    }

    /**
     * {@link GuiGraphics#blitSprite(ResourceLocation, int, int, int, int, int, int, int, int)} with float support
     */
    public static void blitSprite(GuiGraphics gui, ResourceLocation sprite, float textureWidth, float textureHeight, float uPosition, float vPosition, float x, float y, float uWidth, float vHeight) {
        blitSprite(gui, sprite, textureWidth, textureHeight, uPosition, vPosition, x, y, 0, uWidth, vHeight);
    }

    /**
     * {@link GuiGraphics#blitSprite(ResourceLocation, int, int, int, int, int, int, int, int, int)} with float support
     */
    public static void blitSprite(GuiGraphics gui, ResourceLocation sprite, float textureWidth, float textureHeight, float uPosition, float vPosition, float x, float y, float blitOffset, float uWidth, float vHeight) {
        TextureAtlasSprite textureAtlasSprite = gui.sprites.getSprite(sprite);
        GuiSpriteScaling spriteScaling = gui.sprites.getSpriteScaling(textureAtlasSprite);
        if (spriteScaling instanceof GuiSpriteScaling.Stretch) {
            blitSprite(gui, textureAtlasSprite, textureWidth, textureHeight, uPosition, vPosition, x, y, blitOffset, uWidth, vHeight);
        } else {
            blitSprite(gui, textureAtlasSprite, x, y, blitOffset, uWidth, vHeight);
        }
    }

    /**
     * {@link GuiGraphics#blitSprite(TextureAtlasSprite, int, int, int, int, int, int, int, int, int)} with float support
     */
    private static void blitSprite(GuiGraphics gui, TextureAtlasSprite sprite, float textureWidth, float textureHeight, float uPosition, float vPosition, float x, float y, float blitOffset, float uWidth, float vHeight) {
        if (uWidth != 0 && vHeight != 0) {
            innerBlit(gui, sprite.atlasLocation(), x, x + uWidth, y, y + vHeight, blitOffset, sprite.getU(uPosition / textureWidth), sprite.getU((uPosition + uWidth) / textureWidth), sprite.getV(vPosition / textureHeight), sprite.getV((vPosition + vHeight) / textureHeight));
        }
    }

    /**
     * {@link GuiGraphics#blitSprite(TextureAtlasSprite, int, int, int, int, int)} with float support
     */
    private static void blitSprite(GuiGraphics gui, TextureAtlasSprite sprite, float x, float y, float blitOffset, float width, float height) {
        if (width != 0 && height != 0) {
            innerBlit(gui, sprite.atlasLocation(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
        }
    }

    /**
     * {@link GuiGraphics#blit(ResourceLocation, int, int, int, int, int, int)} with float support
     */
    public static void blit(GuiGraphics gui, ResourceLocation atlasLocation, float x, float y, float uOffset, float vOffset, float uWidth, float vHeight) {
        blit(gui, atlasLocation, x, y, 0, uOffset, vOffset, uWidth, vHeight, 256, 256);
    }

    /**
     * {@link GuiGraphics#blit(ResourceLocation, int, int, int, float, float, int, int, int, int)} with float support
     */
    public static void blit(GuiGraphics gui, ResourceLocation atlasLocation, float x, float y, float blitOffset, float uOffset, float vOffset, float uWidth, float vHeight, float textureWidth, float textureHeight) {
        blit(gui, atlasLocation, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    /**
     * {@link GuiGraphics#blit(ResourceLocation, int, int, int, int, float, float, int, int, int, int)} with float support
     */
    public static void blit(GuiGraphics gui, ResourceLocation atlasLocation, float x, float y, float width, float height, float uOffset, float vOffset, float uWidth, float vHeight, float textureWidth, float textureHeight) {
        blit(gui, atlasLocation, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    /**
     * {@link GuiGraphics#blit(ResourceLocation, int, int, float, float, int, int, int, int)} with float support
     */
    public static void blit(GuiGraphics gui, ResourceLocation atlasLocation, float x, float y, float uOffset, float vOffset, float width, float height, float textureWidth, float textureHeight) {
        blit(gui, atlasLocation, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    /**
     * {@link GuiGraphics#blit(ResourceLocation, int, int, int, int, int, int, int, float, float, int, int)} with float support
     */
    public static void blit(GuiGraphics gui, ResourceLocation atlasLocation, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight) {
        innerBlit(gui, atlasLocation, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight);
    }

    /**
     * {@link GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float)} with float support
     */
    public static void innerBlit(GuiGraphics gui, ResourceLocation atlasLocation, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = gui.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(minU, minV);
        bufferbuilder.addVertex(matrix4f, x1, y2, blitOffset).setUv(minU, maxV);
        bufferbuilder.addVertex(matrix4f, x2, y2, blitOffset).setUv(maxU, maxV);
        bufferbuilder.addVertex(matrix4f, x2, y1, blitOffset).setUv(maxU, minV);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    /**
     * {@link GuiGraphics#innerBlit(ResourceLocation, int, int, int, int, int, float, float, float, float, float, float, float, float)} with float support
     */
    public static void innerBlit(GuiGraphics gui, ResourceLocation atlasLocation, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = gui.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(minU, minV).setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, x1, y2, blitOffset).setUv(minU, maxV).setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, x2, y2, blitOffset).setUv(maxU, maxV).setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, x2, y1, blitOffset).setUv(maxU, minV).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    /**
     * {@link GuiGraphics#blitNineSlicedSprite(TextureAtlasSprite, GuiSpriteScaling.NineSlice, int, int, int, int, int)} with float support
     */
    public static void blitNineSlicedSprite(GuiGraphics gui, TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice nineSlice, float x, float y, float blitOffset, float width, float height) {
        GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
        float i = Math.min(border.left(), width / 2);
        float j = Math.min(border.right(), width / 2);
        float k = Math.min(border.top(), height / 2);
        float l = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, height);
        } else if (height == nineSlice.height()) {
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, i, height);
            blitTiledSprite(gui, sprite, x + i, y, blitOffset, width - j - i, height, i, 0, nineSlice.width() - j - i, nineSlice.height(), nineSlice.width(), nineSlice.height());
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, blitOffset, j, height);
        } else if (width == nineSlice.width()) {
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, k);
            blitTiledSprite(gui, sprite, x, y + k, blitOffset, width, height - l - k, 0, k, nineSlice.width(), nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, blitOffset, width, l);
        } else {
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, i, k);
            blitTiledSprite(gui, sprite, x + i, y, blitOffset, width - j - i, k, i, 0, nineSlice.width() - j - i, k, nineSlice.width(), nineSlice.height());
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, blitOffset, j, k);
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, blitOffset, i, l);
            blitTiledSprite(gui, sprite, x + i, y + height - l, blitOffset, width - j - i, l, i, nineSlice.height() - l, nineSlice.width() - j - i, l, nineSlice.width(), nineSlice.height());
            blitSprite(gui, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, nineSlice.height() - l, x + width - j, y + height - l, blitOffset, j, l);
            blitTiledSprite(gui, sprite, x, y + k, blitOffset, i, height - l - k, 0, k, i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitTiledSprite(gui, sprite, x + i, y + k, blitOffset, width - j - i, height - l - k, i, k, nineSlice.width() - j - i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            blitTiledSprite(gui, sprite, x + width - j, y + k, blitOffset, i, height - l - k, nineSlice.width() - j, k, j, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
        }
    }

    /**
     * {@link GuiGraphics#blitTiledSprite(TextureAtlasSprite, int, int, int, int, int, int, int, int, int, int, int)} with float support
     */
    public static void blitTiledSprite(GuiGraphics gui, TextureAtlasSprite sprite,
                                       float x, float y, float blitOffset,
                                       float width, float height,
                                       float uPosition, float vPosition,
                                       float spriteWidth, float spriteHeight,
                                       float nineSliceWidth, float nineSliceHeight) {
        if (width > 0 && height > 0) {
            if (spriteWidth > 0 && spriteHeight > 0) {
                for (int i = 0; i < width; i += (int) spriteWidth) {
                    float j = Math.min(spriteWidth, width - i);

                    for (int k = 0; k < height; k += (int) spriteHeight) {
                        float l = Math.min(spriteHeight, height - k);
                        blitSprite(gui, sprite, nineSliceWidth, nineSliceHeight, uPosition, vPosition, x + i, y + k, blitOffset, j, l);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tiled sprite texture onlsize must be positive, got " + spriteWidth + "x" + spriteHeight);
            }
        }
    }
}
