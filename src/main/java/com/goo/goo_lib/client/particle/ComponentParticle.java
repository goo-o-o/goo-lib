package com.goo.goo_lib.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ComponentParticle extends Particle {
    protected final ComponentParticleOption options;
    protected MutableComponent component;
    protected boolean dropShadow;
    protected int bgColor;
    protected final Font font;
    protected float size, oSize;

    public ComponentParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ComponentParticleOption options) {
        super(level, x, y, z);
        // mojang decided to hardcode speed randomness? it baffles me
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.font = Minecraft.getInstance().font;


        this.options = options;
        this.component = options.component().copy();
        this.dropShadow = options.dropShadow();
        this.bgColor = options.backgroundColor();

        this.lifetime = 60;
        this.hasPhysics = false;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
        this.size = this.oSize = 0.025F;
    }

    public ComponentParticle(ClientLevel level, double x, double y, double z, ComponentParticleOption options) {
        this(level, x, y, z, 0, 0, 0, options);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void tick() {
        this.oRoll = roll;
        this.oSize = size;
        super.tick();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float particleX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float particleY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float particleZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        int light = this.getLightColor(partialTicks);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        // 1. move to world space position position
        poseStack.translate(particleX, particleY, particleZ);

        // 2. lock rotation to face the player camera view flat
        poseStack.mulPose(camera.rotation());

        // 3. handle scaling layout flip
        float size = Mth.lerp(partialTicks, this.oSize, this.size);
        poseStack.scale(size, -size, size);

        // 4. grab absolute widths for layout centering
        float width = (float) this.font.width(this.component);
        float height = 9.0F;

        // translate to center point
        //        poseStack.translate(-width / 2.0F, -height / 2.0F, 0.0F);

        // rotate around center
        //        poseStack.translate(width / 2.0F, height / 2.0F, 0.0F);
        // consolidate instead
        float roll = Mth.lerp(partialTicks, this.oRoll, this.roll);

        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.translate(-width / 2.0F, -height / 2.0F, 0.0F);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        // 6. render at 0, 0 since the entire matrix was offset to center in step 5
        this.font.drawInBatch(this.component, 0.0F, 0.0F, FastColor.ARGB32.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol), this.dropShadow, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, this.bgColor, light);

        buffer.endBatch();
        poseStack.popPose();
    }

    public static class Provider implements ParticleProvider<ComponentParticleOption> {
        @Nullable
        @Override
        public Particle createParticle(
                ComponentParticleOption options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new ComponentParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options);
        }
    }
}