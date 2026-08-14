package com.goo.goo_lib.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class VertexHelper {

    public static ThreadLocal<VertexConsumer> isEnchanted = ThreadLocal.withInitial(() -> null);

    public static Vector3f[] getVertexPos(int[] vertexData) {
        int vertices = vertexData.length / 8;
        Vector3f[] returnList = new Vector3f[vertices];
        for (int i = 0; i < vertices; i++) {
            int vertStride = (i * 8);
            Vector3f vertPos = new Vector3f(Float.intBitsToFloat(vertexData[vertStride]), Float.intBitsToFloat(vertexData[vertStride + 1]), Float.intBitsToFloat(vertexData[vertStride + 2]));
            returnList[i] = vertPos;
        }
        return returnList;
    }

    public static void setVertexData(int[] outVertexData, Vector3f[] newPos) {
        int vertices = outVertexData.length / 8;
        for (int i = 0; i < vertices; i++) {
            int vertStride = (i * 8);
            outVertexData[vertStride] = Float.floatToIntBits(newPos[i].x);
            outVertexData[vertStride + 1] = Float.floatToIntBits(newPos[i].y);
            outVertexData[vertStride + 2] = Float.floatToIntBits(newPos[i].z);
        }
    }

    private static Vector3f getQuadNormal(Vector3f[] verts) {

        Vector3f v1 = new Vector3f(verts[1]).sub(verts[0]);
        Vector3f v2 = new Vector3f(verts[2]).sub(verts[1]);
        Vector3f normal = new Vector3f();

        v1.cross(v2, normal);

        normal.normalize();

        return normal;

    }

    public static Vector3f[] inflateQuadLocally(Vector3f[] defaultVerts, float texelSize, BakedQuad quad) {
        float normalScale = 1/16F;
        Vector3f[] translatedVerts = new Vector3f[defaultVerts.length];

        // 1. decode UV coordinates from the baked vertex data
        // vertex data layouts typically pack U at index 4 and V at index 5 per vertex
        int[] vertexData = quad.getVertices();
        float u0 = Float.intBitsToFloat(vertexData[4]);
        float v0 = Float.intBitsToFloat(vertexData[5]);
        float u1 = Float.intBitsToFloat(vertexData[4 + 8]); // next vertex is 8 ints away
        float v1 = Float.intBitsToFloat(vertexData[5 + 8]);
        float u2 = Float.intBitsToFloat(vertexData[4 + 16]);
        float v2 = Float.intBitsToFloat(vertexData[5 + 16]);

        // 2. calculate edge deltas for positions and UVs
        Vector3f deltaPos1 = new Vector3f(defaultVerts[1]).sub(defaultVerts[0]);
        Vector3f deltaPos2 = new Vector3f(defaultVerts[2]).sub(defaultVerts[0]);

        float deltaU1 = u1 - u0;
        float deltaV1 = v1 - v0;
        float deltaU2 = u2 - u0;
        float deltaV2 = v2 - v0;

        // 3. solve for local U axis (tangent) using the matrix inverse determinant
        float r = 1.0f / (deltaU1 * deltaV2 - deltaV1 * deltaU2);

        // local horizontal axis (matches texture horizontal layout)
        Vector3f localX = new Vector3f(
                (deltaPos1.x() * deltaV2 - deltaPos2.x() * deltaV1) * r,
                (deltaPos1.y() * deltaV2 - deltaPos2.y() * deltaV1) * r,
                (deltaPos1.z() * deltaV2 - deltaPos2.z() * deltaV1) * r
        ).normalize();

        // 4. derive local vertical axis and normal
        Vector3f normal = getQuadNormal(defaultVerts);
        Vector3f localY = new Vector3f();
        normal.cross(localX, localY);
        localY.normalize();

        // 5. perform the inflation using the true local axes
        Vector3f center = new Vector3f(0, 0, 0);
        for (Vector3f vert : defaultVerts) center.add(vert);
        center.mul(1.0f / defaultVerts.length);

        for (int i = 0; i < defaultVerts.length; i++) {
            Vector3f toVert = new Vector3f(defaultVerts[i]).sub(center);

            float signX = Math.signum(toVert.dot(localX));
            float signY = Math.signum(toVert.dot(localY));

            Vector3f newVert = new Vector3f(defaultVerts[i]);

            newVert.add(normal.x() * normalScale, normal.y() * normalScale, normal.z() * normalScale);
            newVert.add(localX.x() * signX * texelSize, localX.y() * signX * texelSize, localX.z() * signX * texelSize);
            newVert.add(localY.x() * signY * texelSize, localY.y() * signY * texelSize, localY.z() * signY * texelSize);

            translatedVerts[i] = newVert;
        }

        return translatedVerts;
    }


    public static int[] flip(int[] inVertexData) {
        int vertices = inVertexData.length / 8;
        int[] outVertextData = new int[inVertexData.length];
        for (int i = 0; i < vertices; i++) {
            int stride = 8;
            System.arraycopy(inVertexData, i * stride, outVertextData, (vertices - i - 1) * stride, stride);
        }
        return outVertextData;
    }

    public static void emitQuad(PoseStack.Pose pose, int[] vertexData, Vector3f[] vertPoses, BakedQuad quad) {
        VertexHelper.setVertexData(vertexData, vertPoses);
        BakedQuad enchantmentQuad = new BakedQuad(VertexHelper.flip(vertexData), -1, quad.getDirection().getOpposite(), null, false, true);
        isEnchanted.get().putBulkData(pose, enchantmentQuad, 1.0F, 1.0F, 0.0F, 0.0F, LightTexture.FULL_BRIGHT, 0, false);
    }

}