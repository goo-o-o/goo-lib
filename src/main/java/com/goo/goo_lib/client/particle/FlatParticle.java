package com.goo.goo_lib.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FlatParticle extends TextureSheetParticle {
    protected float oQuadSize;
    protected float pitch, oPitch;
    protected float yaw, oYaw;

    public FlatParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float size, float pitch, float yaw, float roll) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize = size;
        this.oQuadSize = size;
        this.pitch = pitch;
        this.oPitch = pitch;
        this.yaw = yaw;
        this.oYaw = yaw;
        this.roll = roll;
        this.oRoll = roll;
        this.lifetime = 40;
    }

    public FlatParticle(ClientLevel level, double x, double y, double z, float size, float pitch, float yaw, float roll) {
        this(level, x, y, z, 0, 0, 0, size, pitch, yaw, roll);
    }

    @Override
    public void tick() {
        this.oQuadSize = quadSize;
        this.oPitch = pitch;
        this.oYaw = yaw;
        this.oRoll = roll;
        super.tick();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        double rx = xo + (x - xo) * partialTicks;
        double ry = yo + (y - yo) * partialTicks;
        double rz = zo + (z - zo) * partialTicks;

        Vec3 cameraPos = camera.getPosition();
        float px = (float) (rx - cameraPos.x());
        float py = (float) (ry - cameraPos.y());
        float pz = (float) (rz - cameraPos.z());

        float renderSize = Mth.lerp(partialTicks, this.oQuadSize, this.quadSize);
        float halfSize = renderSize / 2.0F;

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-halfSize, 0.0F, -halfSize),
                new Vector3f(-halfSize, 0.0F, halfSize),
                new Vector3f(halfSize, 0.0F, halfSize),
                new Vector3f(halfSize, 0.0F, -halfSize)
        };


        Quaternionf rotation = new Quaternionf();

        float renderYaw = Mth.lerp(partialTicks, this.oYaw, this.yaw);
        float renderPitch = Mth.lerp(partialTicks, this.oPitch, this.pitch);
        float renderRoll = Mth.lerp(partialTicks, this.oRoll, this.roll);

        rotation.rotationY(renderYaw * Mth.DEG_TO_RAD);
        rotation.rotateX(renderPitch * Mth.DEG_TO_RAD);
        rotation.rotateZ(renderRoll * Mth.DEG_TO_RAD);

        for (Vector3f vertex : vertices) {
            rotation.transform(vertex);
            vertex.add(px, py, pz);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int light = this.getLightColor(partialTicks);

        // front
        buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);

        // back
        buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);

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
            FlatParticle flatParticle = new FlatParticle(level, x, y, z, data.size(), data.rotX(), data.rotY(), data.rotZ());
            flatParticle.pickSprite(this.sprites);
            return flatParticle;
        }
    }
}