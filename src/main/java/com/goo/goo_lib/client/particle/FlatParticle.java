package com.goo.goo_lib.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FlatParticle extends TextureSheetParticle {
    protected float oQuadSize;
    protected float pitch, oPitch, vPitch, aPitch;
    protected float yaw, oYaw, vYaw, aYaw;
    protected float vRoll, aRoll;
    protected boolean doubleSided = true;
    protected float oRCol, oGCol, oBCol, oAlpha;


    public FlatParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float radius, float pitch, float yaw, float roll) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize = radius;
        this.oQuadSize = radius;
        this.pitch = pitch;
        this.oPitch = pitch;
        this.yaw = yaw;
        this.oYaw = yaw;
        this.roll = roll;
        this.oRoll = roll;
        this.oRCol = rCol;
        this.oBCol = bCol;
        this.oGCol = gCol;
        this.oAlpha = alpha;
        this.lifetime = 200;
    }

    public FlatParticle(ClientLevel level, double x, double y, double z, float radius, float pitch, float yaw, float roll) {
        this(level, x, y, z, 0, 0, 0, radius, pitch, yaw, roll);
    }

    public FlatParticle withRotation(float pitch, float yaw, float roll) {
        this.pitch = this.oPitch = pitch;
        this.yaw = this.oYaw = yaw;
        this.roll = this.oRoll = roll;
        return this;
    }

    public FlatParticle withAngularVelocity(float vPitch, float vYaw, float vRoll) {
        this.vPitch = vPitch;
        this.vYaw = vYaw;
        this.vRoll = vRoll;
        return this;
    }

    public FlatParticle withAngularAcceleration(float aPitch, float aYaw, float aRoll) {
        this.aPitch = aPitch;
        this.aYaw = aYaw;
        this.aRoll = aRoll;
        return this;
    }

    @Override
    public void tick() {
        this.oQuadSize = quadSize;
        this.oPitch = pitch;
        this.oYaw = yaw;
        this.oRoll = roll;
        this.oRCol = rCol;
        this.oBCol = bCol;
        this.oGCol = gCol;
        this.oAlpha = alpha;

        vPitch += aPitch;
        vYaw += aYaw;
        vRoll += aRoll;
        pitch += vPitch;
        yaw += vYaw;
        roll += vRoll;

        super.tick();
    }

    protected Quaternionf getRotations(float partialTicks) {
        float renderYaw = Mth.lerp(partialTicks, this.oYaw, this.yaw);
        float renderPitch = Mth.lerp(partialTicks, this.oPitch, this.pitch);
        float renderRoll = Mth.lerp(partialTicks, this.oRoll, this.roll);

        Quaternionf rotation = new Quaternionf();

        rotation.rotationY(renderYaw * ((float) Math.PI / 180F));
        rotation.rotateX(renderPitch * ((float) Math.PI / 180F));
        rotation.rotateZ(renderRoll * ((float) Math.PI / 180F));

        return rotation;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Quaternionf rotation = getRotations(partialTicks);

        renderRotatedQuad(buffer, camera, rotation, partialTicks);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return Mth.lerp(scaleFactor, oQuadSize, quadSize);
    }

    public int getLerpedColor(float partialTicks) {
        int oldColor = FastColor.ARGB32.colorFromFloat(oAlpha, oRCol, oGCol, oBCol);
        int newColor = FastColor.ARGB32.colorFromFloat(alpha, rCol, gCol, bCol);
        return FastColor.ARGB32.lerp(partialTicks, oldColor, newColor);
    }

    public void renderVertexLerped(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float quadSize, float u, float v, int packedLight, float partialTicks) {
        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(getLerpedColor(partialTicks))
                .setLight(packedLight);
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float partialTicks) {
        float f = this.getQuadSize(partialTicks);
        float f1 = this.getU0();
        float f2 = this.getU1();
        float f3 = this.getV0();
        float f4 = this.getV1();
        int i = this.getLightColor(partialTicks);
        renderVertexLerped(buffer, quaternion, x, y, z, 1.0F, -1.0F, f, f2, f4, i, partialTicks);
        renderVertexLerped(buffer, quaternion, x, y, z, 1.0F, 1.0F, f, f2, f3, i, partialTicks);
        renderVertexLerped(buffer, quaternion, x, y, z, -1.0F, 1.0F, f, f1, f3, i, partialTicks);
        renderVertexLerped(buffer, quaternion, x, y, z, -1.0F, -1.0F, f, f1, f4, i, partialTicks);

        if (doubleSided) {
            renderVertexLerped(buffer, quaternion, x, y, z, -1.0F, -1.0F, f, f1, f4, i, partialTicks);
            renderVertexLerped(buffer, quaternion, x, y, z, -1.0F, 1.0F, f, f1, f3, i, partialTicks);
            renderVertexLerped(buffer, quaternion, x, y, z, 1.0F, 1.0F, f, f2, f3, i, partialTicks);
            renderVertexLerped(buffer, quaternion, x, y, z, 1.0F, -1.0F, f, f2, f4, i, partialTicks);
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    public static class Provider implements ParticleProvider<FlatParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(FlatParticleOption data, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FlatParticle flatParticle = new FlatParticle(level, x, y, z, data.radius(), data.rotX(), data.rotY(), data.rotZ());
            flatParticle.pickSprite(this.sprites);
            return flatParticle;
        }
    }
}