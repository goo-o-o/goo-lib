package com.goo.goo_lib.util.phys.hitboxes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ArcCylindricalBoundingBox extends CylindricalBoundingBox {

    public float arcDegrees;

    public ArcCylindricalBoundingBox(Vec3 center, float height, float radius, float innerRadius, float arcDegrees) {
        super(center, height, radius, innerRadius);
        this.arcDegrees = arcDegrees;
    }

    public ArcCylindricalBoundingBox(Vec3 center, float height, float radius, float innerRadius, float arcDegrees, Matrix3f rotation) {
        super(center, height, radius, innerRadius);
        this.rotation = rotation;
        this.arcDegrees = arcDegrees;
    }

    public void setArc(float arcDegrees) {
        this.arcDegrees = arcDegrees;
    }

    @Override
    public ArcCylindricalBoundingBox copy() {
        ArcCylindricalBoundingBox copy = new ArcCylindricalBoundingBox(center, height, radius, innerRadius, arcDegrees);
        copy.rotation.set(this.rotation);
        return copy;
    }

    @Override
    public boolean intersectsAABB(AABB aabb) {
        if (center == null) return false;
        if (arcDegrees <= 0f) return false;

        Matrix3f invRot = new Matrix3f(rotation).invert();

        // setup bounds trackers
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        Vector3f[] local = new Vector3f[8];
        int index = 0;

        // transform corners and update local bounds
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Vector3f c = new Vector3f(
                            (float)(x == 0 ? aabb.minX : aabb.maxX) - (float)center.x,
                            (float)(y == 0 ? aabb.minY : aabb.maxY) - (float)center.y,
                            (float)(z == 0 ? aabb.minZ : aabb.maxZ) - (float)center.z
                    );

                    invRot.transform(c);
                    local[index++] = c;

                    // Track the min/max in local space
                    minX = Math.min(minX, c.x); maxX = Math.max(maxX, c.x);
                    minY = Math.min(minY, c.y); maxY = Math.max(maxY, c.y);
                    minZ = Math.min(minZ, c.z); maxZ = Math.max(maxZ, c.z);
                }
            }
        }

        // check height
        if (maxY < -halfHeight || minY > halfHeight) return false;

        // do not change to clamp()
        float closestX = Math.max(minX, Math.min(maxX, 0f));
        float closestZ = Math.max(minZ, Math.min(maxZ, 0f));
        float closestDistSq = closestX * closestX + closestZ * closestZ;

        if (closestDistSq > radius * radius) return false;

        // if full circle just return true
        if (arcDegrees >= 360f) return true;

        // if not check the angle
        float halfArc = (float) Math.toRadians(arcDegrees * 0.5f);
        float forwardAngle = (float) (Math.PI / 2.0); // +Z direction

        for (Vector3f c : local) {
            float distSq = c.x * c.x + c.z * c.z;

            // skip corners outside maximum radius and minimum radius (inner hole)
            if (distSq > radius * radius) continue;
            if (innerRadius > 0.01f && distSq < innerRadius * innerRadius) continue;
            if (distSq < 0.001f) return true; // direct hit on center

            float angle = (float) Math.atan2(c.z, c.x);
            float delta = angle - forwardAngle;
            delta = (float) ((delta + Math.PI) % (2f * Math.PI) - Math.PI);

            if (Math.abs(delta) <= halfArc) return true;
        }

        return false;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack poseStack) {
        if (arcDegrees <= 0) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);

        // apply hitbox rotation
        Matrix4f rot4 = new Matrix4f();
        rotation.get(rot4);
        poseStack.last().pose().mul(rot4);
        poseStack.last().normal().mul(rotation);

        PoseStack.Pose currentPose = poseStack.last();
        Matrix4f pose = currentPose.pose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        RenderSystem.lineWidth(4.0f);

        float r = 0.0f, g = 1.0f, b = 0.4f, a = 1.0f;

        float halfArcRad = (float) Math.toRadians(arcDegrees * 0.5f);
        int segments = Math.max(8, (int) (32 * arcDegrees / 360f));
        float step = (2f * halfArcRad) / segments;

        float[] radii = innerRadius > 0.02f ? new float[]{radius, innerRadius} : new float[]{radius};

        for (float y : new float[]{halfHeight, -halfHeight}) {
            for (float rad : radii) {
                if (rad < 0.02f) continue;

                for (int i = 0; i < segments; i++) {
                    float angle1 = (float) Math.PI / 2 - halfArcRad + i * step;
                    float angle2 = (float) Math.PI / 2 - halfArcRad + (i + 1) * step;

                    float x1 = (float) Math.cos(angle1) * rad;
                    float z1 = (float) Math.sin(angle1) * rad;
                    float x2 = (float) Math.cos(angle2) * rad;
                    float z2 = (float) Math.sin(angle2) * rad;

                    buffer.addVertex(pose, x1, y, z1).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
                    buffer.addVertex(pose, x2, y, z2).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
                }
            }

            if (innerRadius > 0.02f) {
                for (float angle : new float[]{(float) Math.PI / 2 - halfArcRad, (float) Math.PI / 2 + halfArcRad}) {
                    float xOuter = (float) Math.cos(angle) * radius;
                    float zOuter = (float) Math.sin(angle) * radius;
                    float xInner = (float) Math.cos(angle) * innerRadius;
                    float zInner = (float) Math.sin(angle) * innerRadius;

                    buffer.addVertex(pose, xOuter, y, zOuter).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
                    buffer.addVertex(pose, xInner, y, zInner).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
                }
            }
        }

        for (float rad : radii) {
            if (rad < 0.02f) continue;
            for (float offset : new float[]{-halfArcRad, +halfArcRad}) {
                float angle = (float) Math.PI / 2 + offset;
                float x = (float) Math.cos(angle) * rad;
                float z = (float) Math.sin(angle) * rad;

                buffer.addVertex(pose, x, -halfHeight, z).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
                buffer.addVertex(pose, x, halfHeight, z).setColor(r, g, b, a).setNormal(currentPose, 0, 1, 0);
            }
        }

        bufferSource.endBatch(RenderType.lines());
        RenderSystem.lineWidth(1.0f);

        poseStack.popPose();
    }


}