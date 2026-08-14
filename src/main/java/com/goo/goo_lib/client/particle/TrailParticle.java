package com.goo.goo_lib.client.particle;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.render.PostEffectRegistry;
import com.goo.goo_lib.client.render.pipeline.ShaderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TrailParticle extends TextureSheetParticle {
    protected final Deque<TailNode> rawTails = new ArrayDeque<>();
    protected final float minVertexDistance;
    protected final boolean smoothInterpolation;
    protected final Integer entityId;
    protected final boolean bloomEnabled;
    protected int nodeMaxLife;
    protected float width;

    public TrailParticle(ClientLevel level, double x, double y, double z,
                         double xSpeed, double ySpeed, double zSpeed,
                         Integer entityId, float minVertexDistance, float width, boolean smooth,
                         boolean bloomEnabled, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.minVertexDistance = minVertexDistance;
        this.smoothInterpolation = smooth;
        this.entityId = entityId;
        this.bloomEnabled = bloomEnabled;
        this.nodeMaxLife = 100;
        this.width = width;
        this.rCol = 1.0f;
        this.gCol = 0.0f;
        this.bCol = 0.0f;
        this.alpha = 1.0f;
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setLifetime(1000);

        this.setSpriteFromAge(spriteSet);

        this.rawTails.addFirst(new TailNode(new Vector3f((float) x, (float) y, (float) z), this.nodeMaxLife, this.nodeMaxLife));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // tick node lifetimes
        for (TailNode node : rawTails) {
            node.life--;
        }

        // remove expired nodes
        while (!rawTails.isEmpty() && rawTails.peekLast().life <= 0) {
            rawTails.removeLast();
        }

        boolean entityValid = false;

        if (entityId != null) {
            Entity entity = level.getEntity(entityId);
            if (entity != null && entity.isAlive()) {
                entityValid = true;
                this.setPos(entity.getX(), entity.getY(0.5), entity.getZ());
            }
        }

        // check if tracking particle should stop producing new nodes
        boolean entityLost = (entityId != null && !entityValid);
        boolean expired = this.age++ >= this.lifetime || entityLost;

        // add new nodes only if entity/particle is active
        if (!expired) {
            Vector3f lastTickPos = new Vector3f((float) this.xo, (float) this.yo, (float) this.zo);

            if (rawTails.isEmpty()) {
                rawTails.addFirst(new TailNode(lastTickPos, this.nodeMaxLife, this.nodeMaxLife));
            } else {
                Vector3f lastCommittedPos = rawTails.peekFirst().pos;
                if (lastTickPos.distanceSquared(lastCommittedPos) >= minVertexDistance * minVertexDistance) {
                    rawTails.addFirst(new TailNode(lastTickPos, this.nodeMaxLife, this.nodeMaxLife));
                }
            }
        } else if (rawTails.isEmpty()) {
            // remove particle when generation stops and all remaining nodes decay
            this.remove();
        }
    }

    public float getWidthAtProgress(float progress, float partialTicks) {
        float capThreshold = 0.15F;

        if (progress < capThreshold) {
            float x = progress / capThreshold;
            float dx = 1.0f - x;
            return Mth.sqrt(1.0f - (dx * dx)) * width;
        }

        return Math.max(0.0f, ((1.0f - progress) / (1.0f - capThreshold)) * width);
    }

    public int getColorAtProgress(float progress, float partialTicks) {
        int rgb = Mth.hsvToRgb(progress, 1.0f, 1.0f);
        int alphaInt = (int) ((1 - progress) * 255.0f) + 155;

        return FastColor.ARGB32.color(
                Math.min(alphaInt, 255),
                FastColor.ARGB32.red(rgb),
                FastColor.ARGB32.green(rgb),
                FastColor.ARGB32.blue(rgb)
        );
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        VertexConsumer effectConsumer = null;
        if (this.bloomEnabled) {
            PostEffectRegistry.renderEffectForNextTick(GLRenderTypes.BLOOM_SHADER_LOCATION, ShaderPipeline.PipelineStage.WORLD);
            effectConsumer = GLRenderTypes.BLOOM_BUFFER_SOURCE.getBuffer(
                    GLRenderTypes.getBloomRenderType(TextureAtlas.LOCATION_PARTICLES, RenderStateShard.LEQUAL_DEPTH_TEST, RenderStateShard.NO_CULL)
            );
        }

        Vector3f interpolatedHead;
        if (this.entityId != null) {
            Entity entity = this.level.getEntity(this.entityId);
            if (entity != null) {
                interpolatedHead = entity.getPosition(partialTicks).add(0.0F, entity.getBbHeight() * 0.5F, 0.0F).toVector3f();
            } else {
                interpolatedHead = this.getInterpolatedHead(partialTicks);
            }
        } else {
            interpolatedHead = this.getInterpolatedHead(partialTicks);
        }

        List<TailNode> frameTails = new ArrayList<>();
        if (this.age < this.lifetime) {
            frameTails.add(new TailNode(interpolatedHead, this.nodeMaxLife, this.nodeMaxLife));
        }

        for (TailNode node : this.rawTails) {
            if (frameTails.isEmpty() || frameTails.getLast().pos.distanceSquared(node.pos) >= 1.0E-4F) {
                frameTails.add(node);
            }
        }

        if (frameTails.size() < 2) {
            return;
        }

        List<TailNode> renderTails = this.smoothInterpolation ? this.generateSmoothPath(frameTails, 2) : frameTails;
        if (renderTails.size() >= 2) {
            Vec3 camPos = camera.getPosition();
            float cx = (float) camPos.x;
            float cy = (float) camPos.y;
            float cz = (float) camPos.z;
            int nodeCount = renderTails.size();
            Vector3f[] positions = new Vector3f[nodeCount];
            float[] accumulatedDistances = new float[nodeCount];
            float totalDistance = 0.0F;

            for (int i = 0; i < nodeCount; ++i) {
                TailNode node = renderTails.get(i);
                positions[i] = new Vector3f(node.pos.x - cx, node.pos.y - cy, node.pos.z - cz);
                if (i > 0) {
                    totalDistance += positions[i].distance(positions[i - 1]);
                }

                accumulatedDistances[i] = totalDistance;
            }

            if (!(totalDistance < 1.0E-4F)) {
                Vector3f[] normals = new Vector3f[nodeCount];

                for (int i = 0; i < nodeCount; ++i) {
                    Vector3f dir = new Vector3f();
                    if (i == 0) {
                        dir.set(positions[1]).sub(positions[0]);
                    } else if (i == nodeCount - 1) {
                        dir.set(positions[nodeCount - 1]).sub(positions[nodeCount - 2]);
                    } else {
                        Vector3f d1 = (new Vector3f(positions[i])).sub(positions[i - 1]).normalize();
                        Vector3f d2 = (new Vector3f(positions[i + 1])).sub(positions[i]).normalize();
                        dir.set(d1).add(d2);
                    }

                    if (dir.lengthSquared() < 1.0E-6F) {
                        dir.set(0.0F, 1.0F, 0.0F);
                    } else {
                        dir.normalize();
                    }

                    Vector3f toCamera = (new Vector3f(positions[i])).negate();
                    if (toCamera.lengthSquared() < 1.0E-6F) {
                        normals[i] = new Vector3f(0.0F, 1.0F, 0.0F);
                    } else {
                        toCamera.normalize();
                        Vector3f normal = (new Vector3f(dir)).cross(toCamera);
                        if (normal.lengthSquared() < 1.0E-6F) {
                            Vector3f fallback = (new Vector3f(0.0F, 1.0F, 0.0F)).cross(toCamera);
                            normals[i] = fallback.lengthSquared() < 1.0E-6F
                                    ? new Vector3f(1.0F, 0.0F, 0.0F)
                                    : fallback.normalize();
                        } else {
                            normals[i] = normal.normalize();
                        }
                    }
                }

                for (int i = 0; i < nodeCount - 1; ++i) {
                    Vector3f p1 = positions[i];
                    Vector3f p2 = positions[i + 1];
                    float progress1 = accumulatedDistances[i] / totalDistance;
                    float progress2 = accumulatedDistances[i + 1] / totalDistance;
                    float w1 = this.getWidthAtProgress(progress1, partialTicks);
                    float w2 = this.getWidthAtProgress(progress2, partialTicks);
                    int col1 = this.getColorAtProgress(progress1, partialTicks);
                    int col2 = this.getColorAtProgress(progress2, partialTicks);
                    Vector3f offset1 = (new Vector3f(normals[i])).mul(w1 * 0.5F);
                    Vector3f offset2 = (new Vector3f(normals[i + 1])).mul(w2 * 0.5F);

                    // stretch texture along segment length (u: 0.0 -> 1.0)
                    this.addQuad(buffer, p1, p2, offset1, offset2, col1, col2, progress1, progress2);

                    if (effectConsumer != null) {
                        this.addQuad(effectConsumer, p1, p2, offset1, offset2, col1, col2, progress1, progress2);
                    }
                }

            }
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }
    protected void addQuad(VertexConsumer consumer, Vector3f p1, Vector3f p2,
                           Vector3f offset1, Vector3f offset2,
                           int col1, int col2, float uStart, float uEnd) {
        int packedLight = getLightColor(0.0f);

        // interpolate correctly across atlas sprite bounds
        float minU = this.sprite.getU0();
        float maxU = this.sprite.getU1();
        float u0 = Mth.lerp(uStart, minU, maxU);
        float u1 = Mth.lerp(uEnd, minU, maxU);

        float v0 = this.sprite.getV0();
        float v1 = this.sprite.getV1();

        consumer.addVertex(p1.x - offset1.x, p1.y - offset1.y, p1.z - offset1.z)
                .setUv(u0, v0)
                .setLight(packedLight)
                .setColor(col1);

        consumer.addVertex(p1.x + offset1.x, p1.y + offset1.y, p1.z + offset1.z)
                .setUv(u0, v1)
                .setLight(packedLight)
                .setColor(col1);

        consumer.addVertex(p2.x + offset2.x, p2.y + offset2.y, p2.z + offset2.z)
                .setUv(u1, v1)
                .setLight(packedLight)
                .setColor(col2);

        consumer.addVertex(p2.x - offset2.x, p2.y - offset2.y, p2.z - offset2.z)
                .setUv(u1, v0)
                .setLight(packedLight)
                .setColor(col2);
    }

    protected Vector3f getInterpolatedHead(float partialTicks) {
        double ix = Mth.lerp(partialTicks, this.xo, this.x);
        double iy = Mth.lerp(partialTicks, this.yo, this.y);
        double iz = Mth.lerp(partialTicks, this.zo, this.z);
        return new Vector3f((float) ix, (float) iy, (float) iz);
    }

    protected List<TailNode> generateSmoothPath(List<TailNode> nodes, int iterations) {
        if (nodes.size() <= 2) return nodes;

        List<TailNode> current = nodes;

        for (int iter = 0; iter < iterations; iter++) {
            List<TailNode> next = new ArrayList<>((current.size() - 1) * 2);

            next.add(current.getFirst());

            for (int i = 0; i < current.size() - 1; i++) {
                TailNode p0 = current.get(i);
                TailNode p1 = current.get(i + 1);

                Vector3f qPos = new Vector3f(p0.pos).mul(0.75f).add(new Vector3f(p1.pos).mul(0.25f));
                int qLife = (int) Mth.lerp(0.25f, p0.life, p1.life);
                int qMaxLife = (int) Mth.lerp(0.25f, p0.maxLife, p1.maxLife);

                Vector3f rPos = new Vector3f(p0.pos).mul(0.25f).add(new Vector3f(p1.pos).mul(0.75f));
                int rLife = (int) Mth.lerp(0.75f, p0.life, p1.life);
                int rMaxLife = (int) Mth.lerp(0.75f, p0.maxLife, p1.maxLife);

                next.add(new TailNode(qPos, qLife, qMaxLife));
                next.add(new TailNode(rPos, rLife, rMaxLife));
            }

            next.add(current.getLast());
            current = next;
        }

        return current;
    }

    protected static final class TailNode {
        public Vector3f pos;
        public int life;
        public int maxLife;

        public TailNode(Vector3f pos, int life, int maxLife) {
            this.pos = pos;
            this.life = life;
            this.maxLife = maxLife;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TailNode) obj;
            return Objects.equals(this.pos, that.pos) &&
                    this.life == that.life &&
                    this.maxLife == that.maxLife;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<TrailParticleOption> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(
                TrailParticleOption options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new TrailParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed,
                    options.entityId(), options.minVertexDistance(), options.width(), options.smoothInterpolation(), options.bloom(),
                    this.spriteSet
            );
        }
    }
}