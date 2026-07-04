package com.goo.goo_lib.client.particle.gui;

import com.goo.goo_lib.utils.RenderUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class GuiParticle {

    public float x, y, prevX, prevY;
    public int z;
    public float vx, vy, ax, ay;
    public int age;
    public int lifetime;
    public float scale;
    public float prevScale;
    public float r, g, b, alpha;
    public float prevR, prevG, prevB, prevAlpha;
    public boolean additive = false;

    private ParticleType<?> particleType;
    private ResourceLocation texture;
    private int solidColor = -1;
    private boolean useColor = false;

    private GuiParticle(float x, float y, int z, float vx, float vy, int lifetime, float scale,
                        float r, float g, float b, float alpha) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.lifetime = Math.max(1, lifetime);
        this.alpha = this.prevAlpha = alpha;
        this.scale = this.prevScale = scale;
        this.r = this.prevR = r;
        this.g = this.prevG = g;
        this.b = this.prevB = b;
    }

    public GuiParticle(ParticleType<?> particleType,
                       float x, float y, int z, float vx, float vy, int lifetime, float scale,
                       float r, float g, float b, float alpha) {
        this(x, y, z, vx, vy, lifetime, scale, r, g, b, alpha);
        this.particleType = particleType;
    }

    public GuiParticle(ResourceLocation texture,
                       float x, float y, int z, float vx, float vy, int lifetime, float scale,
                       float r, float g, float b, float alpha) {
        this(x, y, z, vx, vy, lifetime, scale, r, g, b, alpha);
        this.texture = texture;
    }

    public GuiParticle(int solidColor,
                       float x, float y, int z, float vx, float vy, int lifetime, float scale,
                       float r, float g, float b, float alpha) {
        this(x, y, z, vx, vy, lifetime, scale, r, g, b, alpha);
        this.solidColor = solidColor;
        this.useColor = true;
    }

    public GuiParticle withGravity(float gravity) {
        this.ay = gravity;
        return this;
    }

    public GuiParticle withAcceleration(float ax, float ay) {
        this.ax = ax;
        this.ay = ay;
        return this;
    }

    public GuiParticle additiveRendering() {
        this.additive = true;
        return this;
    }

    public boolean tick() {
        if (isDead()) return false;
        prevX = x;
        prevY = y;
        prevScale = scale;
        prevR = r;
        prevG = g;
        prevB = b;
        prevAlpha = alpha;
        vx += ax;
        vy += ay;
        x += vx;
        y += vy;
        age++;
        return age < lifetime;
    }

    public void render(GuiGraphics graphics, float partialTick) {
        if (alpha <= 0f || scale <= 0f) return;

        float drawScale = Mth.lerp(partialTick, this.prevScale, this.scale);
        float drawR = Mth.lerp(partialTick, this.prevR, this.r);
        float drawG = Mth.lerp(partialTick, this.prevG, this.g);
        float drawB = Mth.lerp(partialTick, this.prevB, this.b);
        float drawAlpha = Mth.lerp(partialTick, this.prevAlpha, this.alpha);
        float drawX = Mth.lerp(partialTick, prevX, x);
        float drawY = Mth.lerp(partialTick, prevY, y);

        Window window = Minecraft.getInstance().getWindow();
        float guiScale = (float) window.getGuiScale();

        // Calculate float pixel positions
        float pixelX = drawX * guiScale;
        float pixelY = drawY * guiScale;
        float pixelSize = drawScale * guiScale;

        // Use floats directly - no snapping
        float halfSize = pixelSize / 2f;
        float left = pixelX - halfSize;
        float top = pixelY - halfSize;
        float right = pixelX + halfSize;
        float bottom = pixelY + halfSize;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(1f / guiScale, 1f / guiScale, 1f);

        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }

        if (useColor) {
            int colour = solidColor != -1 ? solidColor : FastColor.ARGB32.colorFromFloat(drawAlpha, drawR, drawG, drawB);
            RenderUtils.fillFloat(graphics, left, top, right, bottom, z, colour);
        } else if (particleType != null) {
            SpriteSet sprites = GuiParticleSystem.getSprites(particleType);
            if (sprites != null) {
                TextureAtlasSprite sprite = sprites.get(age, lifetime);
                RenderUtils.renderSpriteFloat(graphics, sprite, left, top, right, bottom, z, drawR, drawG, drawB, drawAlpha);
            }
        } else if (texture != null) {
            RenderSystem.setShaderColor(drawR, drawG, drawB, drawAlpha);
            RenderUtils.blitFloat(graphics, texture, left, top, right, bottom, z);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }


    public boolean isDead() {
        return age >= lifetime;
    }
}