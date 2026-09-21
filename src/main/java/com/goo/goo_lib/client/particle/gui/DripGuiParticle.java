package com.goo.goo_lib.client.particle.gui;

import com.goo.goo_lib.util.RenderUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class DripGuiParticle extends GuiParticle {


    public DripGuiParticle(int solidColor, float x, float y, int z, float vx, float vy, int lifetime, float scale, float r, float g, float b, float alpha) {
        super(solidColor, x, y, z, vx, vy, lifetime, scale, r, g, b, alpha);
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        if (!(this.alpha <= 0.0F) && !(this.scale <= 0.0F)) {
            float drawScale = Mth.lerp(partialTick, this.prevScale, this.scale);
            float drawR = Mth.lerp(partialTick, this.prevR, this.r);
            float drawG = Mth.lerp(partialTick, this.prevG, this.g);
            float drawB = Mth.lerp(partialTick, this.prevB, this.b);
            float drawAlpha = Mth.lerp(partialTick, this.prevAlpha, this.alpha);
            float drawX = Mth.lerp(partialTick, this.prevX, this.x);
            float drawY = Mth.lerp(partialTick, this.prevY, this.y);
            Window window = Minecraft.getInstance().getWindow();
            float guiScale = (float)window.getGuiScale();
            float pixelX = drawX * guiScale;
            float pixelY = drawY * guiScale;
            float pixelSize = drawScale * guiScale;
            float halfSize = pixelSize / 2.0F;
            float left = pixelX - halfSize;
            float top = pixelY - halfSize * 5; // make it 3 times as long as wide, originating from top
            float right = pixelX + halfSize;
            float bottom = pixelY + halfSize;
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.scale(1.0F / guiScale, 1.0F / guiScale, 1.0F);
            RenderSystem.enableBlend();
            if (this.additive) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.defaultBlendFunc();
            }

            if (this.useColor) {
                int colour = this.solidColor != -1 ? this.solidColor : FastColor.ARGB32.colorFromFloat(drawAlpha, drawR, drawG, drawB);
                RenderUtil.fillWithUv(RenderType.gui(), graphics, left, top, right, bottom, (float)this.z, colour);
            } else if (this.particleType != null) {
                SpriteSet sprites = GuiParticleSystem.getSprites(this.particleType);
                if (sprites != null) {
                    TextureAtlasSprite sprite = sprites.get(this.age, this.lifetime);
                    RenderUtil.blit(graphics, left, top, (float)this.z, pixelSize, pixelSize, sprite, drawR, drawG, drawB, drawAlpha);
                }
            } else if (this.texture != null) {
                RenderSystem.setShaderColor(drawR, drawG, drawB, drawAlpha);
                RenderUtil.blitSprite(graphics, this.texture, left, top, (float)this.z, pixelSize, pixelSize);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }

            RenderSystem.disableBlend();
            pose.popPose();
        }
    }
}
