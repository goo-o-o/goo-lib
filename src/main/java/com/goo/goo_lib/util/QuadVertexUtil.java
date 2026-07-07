package com.goo.goo_lib.util;

import org.joml.Vector3f;

public final class QuadVertexUtil {
    private static final int STRIDE = 8; // ints per vertex for DefaultVertexFormat.BLOCK

    public static Vector3f getPosition(int[] verts, int vertexIndex) {
        int base = vertexIndex * STRIDE;
        return new Vector3f(
            Float.intBitsToFloat(verts[base]),
            Float.intBitsToFloat(verts[base + 1]),
            Float.intBitsToFloat(verts[base + 2])
        );
    }

    public static void setPosition(int[] verts, int vertexIndex, Vector3f pos) {
        int base = vertexIndex * STRIDE;
        verts[base] = Float.floatToRawIntBits(pos.x());
        verts[base + 1] = Float.floatToRawIntBits(pos.y());
        verts[base + 2] = Float.floatToRawIntBits(pos.z());
    }

    // Face normal from a quad's winding (fallback if you don't want to trust packed normal)
    public static Vector3f computeFaceNormal(int[] verts) {
        Vector3f p0 = getPosition(verts, 0);
        Vector3f p1 = getPosition(verts, 1);
        Vector3f p2 = getPosition(verts, 2);
        Vector3f e1 = new Vector3f(p1).sub(p0);
        Vector3f e2 = new Vector3f(p2).sub(p0);
        Vector3f n = new Vector3f(e1).cross(e2);
        if (n.lengthSquared() > 1.0e-8f) n.normalize();
        return n;
    }
}