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

public class OrientedBoundingBox extends BaseBoundingBox {
    public Vec3 halfExtents;

    // ────────────────── Constructors ──────────────────
    public OrientedBoundingBox(Vec3 center, Vec3 halfExtents, Matrix3f rotation) {
        super(); // Hitbox already creates identity rotation
        this.center = center;
        this.halfExtents = halfExtents;
        if (rotation != null) this.rotation.set(rotation);
    }

    /**
     * Constructs an {@link OrientedBoundingBox} with a specified center, half-extents, and rotation
     * defined by pitch, yaw, and roll angles. The rotation is internally converted into a
     * {@link Matrix3f} representation.
     *
     * @param center The center position of the bounding box as {@link Vec3}.
     * @param halfExtents A vector of half-dimensions (x, y, z) indicating the size of the bounding box along each axis.
     * @param pitch Rotation around the X-axis in degrees.
     * @param yaw Rotation around the Y-axis in degrees.
     * @param roll Rotation around the Z-axis in degrees.
     */
    public OrientedBoundingBox(Vec3 center, Vec3 halfExtents, float pitch, float yaw, float roll) {
        this(center, halfExtents, new Matrix3f().rotationXYZ(
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(yaw),
                (float) Math.toRadians(roll)));
    }

    public OrientedBoundingBox(Vec3 center, float hx, float hy, float hz) {
        this(center, new Vec3(hx, hy, hz), null);
    }

    public static OrientedBoundingBox fromAABB(AABB aabb) {
        return new OrientedBoundingBox(
                aabb.getCenter(),
                new Vec3((aabb.maxX - aabb.minX) * 0.5,
                        (aabb.maxY - aabb.minY) * 0.5,
                        (aabb.maxZ - aabb.minZ) * 0.5),
                null
        );
    }

    @Override
    public boolean intersectsAABB(AABB aabb) {
        OrientedBoundingBox temp = fromAABB(aabb);
        return obbVsObb(this, temp);
    }

    @Override
    public AABB getAABB() {
        float ex = (float) (Math.abs(rotation.m00()) * halfExtents.x +
                Math.abs(rotation.m01()) * halfExtents.y +
                Math.abs(rotation.m02()) * halfExtents.z);

        float ey = (float) (Math.abs(rotation.m10()) * halfExtents.x +
                Math.abs(rotation.m11()) * halfExtents.y +
                Math.abs(rotation.m12()) * halfExtents.z);

        float ez = (float) (Math.abs(rotation.m20()) * halfExtents.x +
                Math.abs(rotation.m21()) * halfExtents.y +
                Math.abs(rotation.m22()) * halfExtents.z);

        Vec3 he = new Vec3(ex, ey, ez);
        return new AABB(center.subtract(he), center.add(he));
    }

    @Override
    public BaseBoundingBox copy() {
        return new OrientedBoundingBox(center, halfExtents, new Matrix3f(rotation));
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderGradientSides(OrientedBoundingBox box, PoseStack poseStack, int bottomCol, int topCol) {
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        Vec3 camPos = camera.getPosition();
//
//        poseStack.pushPose();
//        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
//
//        // Use Translucent shader for gradients/alpha
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableCull(); // See inside and outside of the box
//
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//
//        // Start drawing QUADS
//        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//
//        Matrix4f mat = poseStack.last().pose();
//
//        // 1. Get our local axes from your existing method
//        Vec3 hx = box.getLocalAxis(0).scale(box.halfExtents.x);
//        Vec3 hy = box.getLocalAxis(1).scale(box.halfExtents.y); // Vertical/Height axis
//        Vec3 hz = box.getLocalAxis(2).scale(box.halfExtents.z);
//
//        // 2. Define the 4 corners of the BOTTOM face (Center minus hy)
//        Vec3 bottomCenter = box.center.subtract(hy);
//        Vec3 b1 = bottomCenter.add(hx).add(hz);
//        Vec3 b2 = bottomCenter.subtract(hx).add(hz);
//        Vec3 b3 = bottomCenter.subtract(hx).subtract(hz);
//        Vec3 b4 = bottomCenter.add(hx).subtract(hz);
//
//        // 3. Define the 4 corners of the TOP face (Center plus hy)
//        Vec3 topCenter = box.center.add(hy);
//        Vec3 t1 = topCenter.add(hx).add(hz);
//        Vec3 t2 = topCenter.subtract(hx).add(hz);
//        Vec3 t3 = topCenter.subtract(hx).subtract(hz);
//        Vec3 t4 = topCenter.add(hx).subtract(hz);
//
//        // 4. Draw the 4 side faces
//        box.drawSide(buffer, mat, b1, b2, t2, t1, bottomCol, topCol); // Side A
//        box.drawSide(buffer, mat, b2, b3, t3, t2, bottomCol, topCol); // Side B
//        box.drawSide(buffer, mat, b3, b4, t4, t3, bottomCol, topCol); // Side C
//        box.drawSide(buffer, mat, b4, b1, t1, t4, bottomCol, topCol); // Side D
//
//        tessellator.end();
//
//        RenderSystem.enableCull();
//        RenderSystem.disableBlend();
//        poseStack.popPose();
    }

//    private void drawSide(BufferBuilder buffer, Matrix4f mat, Vec3 bL, Vec3 bR, Vec3 tR, Vec3 tL, int bCol, int tCol) {
//        vertex(buffer, mat, bL, bCol);
//        vertex(buffer, mat, bR, bCol);
//        vertex(buffer, mat, tR, tCol);
//        vertex(buffer, mat, tL, tCol);
//    }
//
//    private void vertex(BufferBuilder buffer, Matrix4f mat, Vec3 pos, int c) {
//        // This matches DefaultVertexFormat.POSITION_COLOR exactly
//        buffer.vertex(mat, (float)pos.x, (float)pos.y, (float)pos.z)
//                .color(c) // BufferBuilder has a simplified .color(int) helper
//                .endVertex();
//    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack poseStack) {
//        // Get camera position
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        Vec3 camPos = camera.getPosition();
//
//        // Translate poseStack so OBB renders in world space correctly
//        poseStack.pushPose();
////        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
//
//        RenderSystem.enableDepthTest();
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.disableBlend();
//        RenderSystem.lineWidth(4F);
//
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
//
//        outlineOBB(poseStack, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.5F);
//
//        tessellator.end();
//
//        poseStack.popPose(); // Restore original poseStack
//
//        RenderSystem.enableBlend();
//        RenderSystem.lineWidth(1.0F);
    }

//    private void outlineOBB(PoseStack poseStack, BufferBuilder buffer, float r1, float g1, float b1, float r2, float g2, float b2, float alpha) {
//        Vec3[] v = getTransformedVertices(); // Your 8 corners in this exact order
//        Matrix4f mat = poseStack.last().pose();
//
//        // Draw 12 separate edges with DEBUG_LINES (no connectivity issues)
//        addLine(buffer, mat, v[0], v[1], r1, g1, b1, alpha);
//        addLine(buffer, mat, v[1], v[3], r1, g1, b1, alpha);
//        addLine(buffer, mat, v[3], v[2], r1, g1, b1, alpha);
//        addLine(buffer, mat, v[2], v[0], r1, g1, b1, alpha);
//
//        addLine(buffer, mat, v[4], v[5], r2, g2, b2, alpha);
//        addLine(buffer, mat, v[5], v[7], r2, g2, b2, alpha);
//        addLine(buffer, mat, v[7], v[6], r2, g2, b2, alpha);
//        addLine(buffer, mat, v[6], v[4], r2, g2, b2, alpha);
//
//        addLine(buffer, mat, v[0], v[4], r1, g1, b1, alpha);
//        addLine(buffer, mat, v[1], v[5], r1, g1, b1, alpha);
//        addLine(buffer, mat, v[2], v[6], r2, g2, b2, alpha);
//        addLine(buffer, mat, v[3], v[7], r2, g2, b2, alpha);
//    }
//
//    private void addLine(BufferBuilder buffer, Matrix4f mat, Vec3 first, Vec3 second, float r, float g, float b, float a) {
//        buffer.vertex(mat, (float)first.x, (float)first.y, (float)first.z).color(r, g, b, a).endVertex();
//        buffer.vertex(mat, (float)second.x, (float)second.y, (float)second.z).color(r, g, b, a).endVertex();
//    }

    public Vec3[] getTransformedVertices() {
        Vec3 hx = getLocalAxis(0).scale(halfExtents.x);
        Vec3 hy = getLocalAxis(1).scale(halfExtents.y);
        Vec3 hz = getLocalAxis(2).scale(halfExtents.z);

        return new Vec3[] {
                center.add(hx).add(hy).add(hz),       // 0 vertex1
                center.add(hx).add(hy).subtract(hz),  // 1 vertex2
                center.add(hx).subtract(hy).add(hz),  // 2 vertex3
                center.add(hx).subtract(hy).subtract(hz), // 3 vertex4
                center.subtract(hx).add(hy).add(hz),   // 4 vertex5
                center.subtract(hx).add(hy).subtract(hz), // 5 vertex6
                center.subtract(hx).subtract(hy).add(hz), // 6 vertex7
                center.subtract(hx).subtract(hy).subtract(hz)  // 7 vertex8
        };
    }
    public Vec3 getLocalAxis(int i) {
        return new Vec3(
                rotation.get(i, 0),
                rotation.get(i, 1),
                rotation.get(i, 2)
        );
    }


    @Override
    public BaseBoundingBox inflate(float f) {
        halfExtents = halfExtents.scale(f);
        return this;
    }

    private record Proj(float min, float max) {
        boolean diverges(Proj o) {
            return max < o.min || o.max < min;
        }
    }

    private Vector3f rotationX() {
        return new Vector3f(rotation.m00(), rotation.m01(), rotation.m02());
    }

    private Vector3f rotationY() {
        return new Vector3f(rotation.m10(), rotation.m11(), rotation.m12());
    }

    private Vector3f rotationZ() {
        return new Vector3f(rotation.m20(), rotation.m21(), rotation.m22());
    }

    private Proj project(Vector3f axis) {
        Vector3f c = center.toVector3f();
        float r = (float) (halfExtents.x * Math.abs(axis.dot(rotationX())) +
                halfExtents.y * Math.abs(axis.dot(rotationY())) +
                halfExtents.z * Math.abs(axis.dot(rotationZ())));
        float p = axis.dot(c);
        return new Proj(p - r, p + r);
    }


    private static boolean obbVsObb(OrientedBoundingBox a, OrientedBoundingBox b) {
        Vector3f[] axes = new Vector3f[15];
        int i = 0;

        // A axes
        axes[i++] = a.rotationX();
        axes[i++] = a.rotationY();
        axes[i++] = a.rotationZ();
        // B axes
        axes[i++] = b.rotationX();
        axes[i++] = b.rotationY();
        axes[i++] = b.rotationZ();

        // Cross products
        for (int j = 0; j < 3; j++) {
            for (int k = 3; k < 6; k++) {
                axes[i++] = new Vector3f().cross(axes[j], axes[k]);
            }
        }

        for (Vector3f axis : axes) {
            if (axis.lengthSquared() < 1e-6f) continue;
            axis.normalize();
            if (a.project(axis).diverges(b.project(axis))) return false;
        }
        return true;
    }

    @Override
    public <T extends Entity> List<T> findEntitiesHit(Player player, Class<T> clazz) {
        List<T> candidates = player.level().getEntitiesOfClass(clazz, getAABB().inflate(20), e -> e.isAlive() && e.isPickable() && e != player && e != player.getVehicle());
        return filter(candidates);
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.rotation.rotationXYZ(
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(yaw),
                (float) Math.toRadians(roll)
        );
    }
}
