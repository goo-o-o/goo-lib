package com.goo.goo_lib.util;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;

import java.util.*;

public final class HullExpander {

    // Round position to avoid float-equality issues when matching shared corners
    private static long keyFor(Vector3f p) {
        int x = Math.round(p.x() * 4096f);
        int y = Math.round(p.y() * 4096f);
        int z = Math.round(p.z() * 4096f);
        return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
    }

    public static List<BakedQuad> expand(List<BakedQuad> quads, float thickness, int outlineColorARGB) {
        // Pass 1: accumulate normals per unique vertex position
        Map<Long, Vector3f> normalAccum = new HashMap<>();
        List<int[]> rawCopies = new ArrayList<>();
        List<Vector3f> faceNormals = new ArrayList<>();

        for (BakedQuad q : quads) {
            int[] verts = q.getVertices().clone();
            rawCopies.add(verts);
            Vector3f faceN = QuadVertexUtil.computeFaceNormal(verts);
            faceNormals.add(faceN);
            for (int v = 0; v < 4; v++) {
                Vector3f pos = QuadVertexUtil.getPosition(verts, v);
                long key = keyFor(pos);
                normalAccum.merge(key, new Vector3f(faceN), (a, b) -> a.add(b));
            }
        }
        // normalize accumulated normals
        Map<Long, Vector3f> avgNormal = new HashMap<>();
        for (var e : normalAccum.entrySet()) {
            Vector3f n = e.getValue();
            if (n.lengthSquared() > 1.0e-8f) n.normalize();
            avgNormal.put(e.getKey(), n);
        }

        // Pass 2: push vertices out along averaged normal, rebuild quads with flipped winding
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad src = quads.get(i);
            int[] verts = rawCopies.get(i);

            for (int v = 0; v < 4; v++) {
                Vector3f pos = QuadVertexUtil.getPosition(verts, v);
                Vector3f n = avgNormal.get(keyFor(pos));
                Vector3f pushed = new Vector3f(pos).add(new Vector3f(n).mul(thickness));
                QuadVertexUtil.setPosition(verts, v, pushed);
            }

            reverseWinding(verts);
            paintFlatColor(verts, outlineColorARGB);
            setFullbright(verts);

            result.add(new BakedQuad(
                verts,
                src.getTintIndex(),
                src.getDirection(),
                src.getSprite(),
                false // shade: outline should be unlit/flat
            ));
        }
        return result;
    }

    private static void reverseWinding(int[] verts) {
        // swap vertex 1 and 3 (keeps 0 and 2 fixed) -> reverses triangle order
        int[] tmp = Arrays.copyOfRange(verts, 8, 16);
        System.arraycopy(verts, 24, verts, 8, 8);
        System.arraycopy(tmp, 0, verts, 24, 8);
    }

    private static void paintFlatColor(int[] verts, int argb) {
        for (int v = 0; v < 4; v++) verts[v * 8 + 3] = argb;
    }

    private static void setFullbright(int[] verts) {
        int fullbright = 0xF000F0; // block 15, sky 15 packed lightmap
        for (int v = 0; v < 4; v++) verts[v * 8 + 6] = fullbright;
    }
}