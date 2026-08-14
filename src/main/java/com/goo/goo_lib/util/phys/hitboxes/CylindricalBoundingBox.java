package com.goo.goo_lib.util.phys.hitboxes;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;

public class CylindricalBoundingBox extends BaseBoundingBox {
    public float radius;
    public float innerRadius; // For ring shape, 0 for full circle
    public float height;
    public float halfHeight;

    public CylindricalBoundingBox(Vec3 center, float height, float radius, float innerRadius) {
        this.center = center;
        this.height = height;
        this.halfHeight = height * 0.5F;
        this.radius = radius;
        this.innerRadius = innerRadius;
    }

    @Override
    public AABB getAABB() {
        if (center == null) return new AABB(0, 0, 0, 0, 0, 0);

        // Use the LARGER of inner and outer radius
        float outer = Math.max(radius, innerRadius);

        // World X extent: contributions from local X, Y (height), and Z basis vectors
        float ex = Math.abs(rotation.m00()) * outer +   // local X axis (scaled by radius)
                Math.abs(rotation.m01()) * halfHeight +  // local Y axis (height)
                Math.abs(rotation.m02()) * outer;       // local Z axis (scaled by radius)

        float ey = Math.abs(rotation.m10()) * outer +
                Math.abs(rotation.m11()) * halfHeight +
                Math.abs(rotation.m12()) * outer;

        float ez = Math.abs(rotation.m20()) * outer +
                Math.abs(rotation.m21()) * halfHeight +
                Math.abs(rotation.m22()) * outer;

        Vec3 halfExtent = new Vec3(ex, ey, ez);
        return new AABB(center.subtract(halfExtent), center.add(halfExtent));
    }
    @Override
    public BaseBoundingBox copy() {
        CylindricalBoundingBox copy = new CylindricalBoundingBox(center, height, radius, innerRadius);
        copy.rotation.set(this.rotation);
        return copy;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack poseStack) {
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        Vec3 camPos = camera.getPosition();
//
//        poseStack.pushPose();
//        poseStack.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
//
//        // Apply rotation
//        Matrix4f pose = poseStack.last().pose();
//        Matrix3f normal = poseStack.last().normal();
//        Matrix4f rot4 = new Matrix4f();
//        rotation.get(rot4);
//        pose.mul(rot4);
//        normal.mul(rotation);
//
//        int segments = 32;
//        float step = (float) (Math.PI * 2 / segments);
//
//        // === THICK LINES SETUP (exact same as Better Combat) ===
//        RenderSystem.enableDepthTest();
//        RenderSystem.disableCull();
//        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
//        RenderSystem.lineWidth(4F);
//
//        Tesselator tesselator = Tesselator.getInstance();
//        BufferBuilder buffer = tesselator.getBuilder();
//        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
//
//        float r = 0.0f, g = 1.0f, b = 0.4f, alpha = 1.0f;
//
//        // Draw top & bottom circles
//        for (float y : new float[]{halfHeight, -halfHeight}) {
//            for (float rad : new float[]{radius, innerRadius > 0.02f ? innerRadius : 0f}) {
//                if (rad < 0.02f) continue;
//
//                for (int i = 0; i < segments; i++) {
//                    float a1 = i * step;
//                    float a2 = (i + 1) * step;
//                    float x1 = (float)Math.cos(a1) * rad;
//                    float z1 = (float)Math.sin(a1) * rad;
//                    float x2 = (float)Math.cos(a2) * rad;
//                    float z2 = (float)Math.sin(a2) * rad;
//
//                    buffer.vertex(pose, x1, y, z1).color(r,g,b,alpha).normal(normal, 0,1,0).endVertex();
//                    buffer.vertex(pose, x2, y, z2).color(r,g,b,alpha).normal(normal, 0,1,0).endVertex();
//                }
//            }
//        }
//
//        // Vertical pillars (8 of them)
//        for (int i = 0; i < 8; i++) {
//            float a = i * (step * 4);
//            float x = (float)Math.cos(a);
//            float z = (float)Math.sin(a);
//
//            for (float rad : new float[]{radius, innerRadius > 0.02f ? innerRadius : 0f}) {
//                if (rad < 0.02f) continue;
//
//                float vx = x * rad;
//                float vz = z * rad;
//
//                buffer.vertex(pose, vx, -halfHeight, vz).color(r,g,b,a).normal(normal, 0,1,0).endVertex();
//                buffer.vertex(pose, vx,  halfHeight, vz).color(r,g,b,a).normal(normal, 0,1,0).endVertex();
//            }
//        }
//
//        RenderSystem.lineWidth(1);
//        tesselator.end();
//        poseStack.popPose();
    }

    @Override
    public boolean intersectsAABB(AABB aabb) {
        if (center == null) return false;

        Matrix3f invRot = new Matrix3f(rotation).invert();

        Vector3f[] corners = new Vector3f[8];
        int i = 0;
        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                for (int z = 0; z < 2; z++)
                    corners[i++] = new Vector3f(
                            (float)(x==0 ? aabb.minX : aabb.maxX) - (float)center.x,
                            (float)(y==0 ? aabb.minY : aabb.maxY) - (float)center.y,
                            (float)(z==0 ? aabb.minZ : aabb.maxZ) - (float)center.z
                    );

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f c : corners) {
            invRot.transform(c);
            minX = Math.min(minX, c.x); maxX = Math.max(maxX, c.x);
            minY = Math.min(minY, c.y); maxY = Math.max(maxY, c.y);
            minZ = Math.min(minZ, c.z); maxZ = Math.max(maxZ, c.z);
        }

        // 1. Height (local Y)
        if (maxY < -halfHeight || minY > halfHeight) return false;

        // 2. Closest distance from axis to local AABB (in XZ plane)
        float closestX = Math.max(minX, Math.min(maxX, 0f));
        float closestZ = Math.max(minZ, Math.min(maxZ, 0f));
        float closestDistSq = closestX*closestX + closestZ*closestZ;

        // Outside the outer radius → no hit
        if (closestDistSq > radius * radius) return false;

        // 3. For hollow cylinder only: check that we are not completely inside the inner radius
        if (innerRadius > 0.01f) {
            // Squared distance to the farthest corner of the local AABB from the axis
            float farthestDistSq = 0f;
            farthestDistSq = Math.max(farthestDistSq, minX*minX + minZ*minZ);
            farthestDistSq = Math.max(farthestDistSq, minX*minX + maxZ*maxZ);
            farthestDistSq = Math.max(farthestDistSq, maxX*maxX + minZ*minZ);
            farthestDistSq = Math.max(farthestDistSq, maxX*maxX + maxZ*maxZ);

            return !(farthestDistSq < innerRadius * innerRadius); // entirely inside hole
        }

        return true; // intersects the cylinder/ring
    }

    @Override
    public <T extends Entity> List<T> findEntitiesHit(Player player, Class<T> clazz) {
        List<T> candidates = player.level().getEntitiesOfClass(clazz, getAABB().inflate(2), e -> e.isAlive() && e.isPickable() && e != player && e != player.getVehicle());
        return filter(candidates);
    }
}