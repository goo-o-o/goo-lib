package com.goo.goo_lib.client.particle.gui;

import com.goo.goo_lib.util.RenderUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
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

    public float pitch, prevPitch, vPitch, aPitch;
    public float yaw, prevYaw, vYaw, aYaw;
    public float roll, prevRoll, vRoll, aRoll;

    protected ParticleType<?> particleType;
    protected ResourceLocation texture;
    protected int solidColor = -1;
    protected boolean useColor = false;

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

    public GuiParticle withRotation(float pitch, float yaw, float roll) {
        this.pitch = this.prevPitch = pitch;
        this.yaw = this.prevYaw = yaw;
        this.roll = this.prevRoll = roll;
        return this;
    }

    public GuiParticle withAngularVelocity(float vPitch, float vYaw, float vRoll) {
        this.vPitch = vPitch;
        this.vYaw = vYaw;
        this.vRoll = vRoll;
        return this;
    }

    public GuiParticle withAngularAcceleration(float aPitch, float aYaw, float aRoll) {
        this.aPitch = aPitch;
        this.aYaw = aYaw;
        this.aRoll = aRoll;
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

        prevPitch = pitch;
        prevYaw = yaw;
        prevRoll = roll;

        vx += ax;
        vy += ay;
        x += vx;
        y += vy;

        vPitch += aPitch;
        vYaw += aYaw;
        vRoll += aRoll;
        pitch += vPitch;
        yaw += vYaw;
        roll += vRoll;

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

        PoseStack pose = graphics.pose();
        pose.pushPose();

        // 1. Move origin to the particle's center position in GUI space
        pose.translate(drawX, drawY, (float) z);

        // 2. Interpolate and apply 3D rotations around center pivot (0, 0, 0)
        float drawPitch = Mth.lerp(partialTick, prevPitch, pitch);
        float drawYaw = Mth.lerp(partialTick, prevYaw, yaw);
        float drawRoll = Mth.lerp(partialTick, prevRoll, roll);

        if (drawPitch != 0.0F) pose.mulPose(Axis.XP.rotationDegrees(drawPitch));
        if (drawYaw != 0.0F)   pose.mulPose(Axis.YP.rotationDegrees(drawYaw));
        if (drawRoll != 0.0F)  pose.mulPose(Axis.ZP.rotationDegrees(drawRoll));

        // 3. Define quad boundaries centered at (0, 0)
        float halfSize = drawScale / 2f;
        float left = -halfSize;
        float top = -halfSize;
        float right = halfSize;
        float bottom = halfSize;

        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }

        // 4. Render relative to (0, 0) at local z = 0 (z translation handled by pose.translate above)
        if (useColor) {
            int colour = solidColor != -1 ? solidColor : FastColor.ARGB32.colorFromFloat(drawAlpha, drawR, drawG, drawB);
            RenderUtil.fillWithUv(RenderType.gui(), graphics, left, top, right, bottom, 0, colour);
        } else if (particleType != null) {
            SpriteSet sprites = GuiParticleSystem.getSprites(particleType);
            if (sprites != null) {
                TextureAtlasSprite sprite = sprites.get(age, lifetime);
                RenderUtil.blit(graphics, left, top, 0F, drawScale, drawScale, sprite, drawR, drawG, drawB, drawAlpha);
            }
        } else if (texture != null) {
            RenderSystem.setShaderColor(drawR, drawG, drawB, drawAlpha);
            RenderUtil.blitSprite(graphics, texture, left, top, 0F, drawScale, drawScale);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }

    public boolean isDead() {
        return age >= lifetime;
    }
}