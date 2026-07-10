package com.goo.goo_lib.client.render.model;

import com.goo.goo_lib.util.VertexHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InflatedBakedModel extends BakedModelWrapper<BakedModel> {
    private final float texelSize;
    private final Map<ItemDisplayContext, Float> contextScales;
    private final float defaultTexelSize;

    // the model transform cache map
    private final Map<ItemDisplayContext, BakedModel> transformCache;

    private static final int[][] FACE_STAMP_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};

    public InflatedBakedModel(BakedModel originalModel, ContextScale... scales) {
        super(originalModel);
        this.contextScales = new EnumMap<>(ItemDisplayContext.class);
        this.transformCache = new EnumMap<>(ItemDisplayContext.class); // init cache on root parent

        if (scales != null && scales.length != 0) {
            this.defaultTexelSize = scales[0].texelSize();
            this.texelSize = this.defaultTexelSize;

            for (ContextScale config : scales) {
                this.contextScales.put(config.context(), config.texelSize());
            }
        } else {
            this.defaultTexelSize = 1 / 16F;
            this.texelSize = this.defaultTexelSize;
        }
    }

    private InflatedBakedModel(BakedModel originalModel, Map<ItemDisplayContext, Float> contextScales, float defaultTexelSize, float activeTexelSize, Map<ItemDisplayContext, BakedModel> transformCache) {
        super(originalModel);
        this.contextScales = contextScales;
        this.defaultTexelSize = defaultTexelSize;
        this.texelSize = activeTexelSize;
        this.transformCache = transformCache; // pass through reference to shared cache
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext cameraTransformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        if (this.transformCache.containsKey(cameraTransformType)) {
            BakedModel cachedModel = this.transformCache.get(cameraTransformType);
            cachedModel.getTransforms().getTransform(cameraTransformType).apply(applyLeftHandTransform, poseStack);
            return cachedModel;
        }

        // 1. apply transforms to the posestack first
        BakedModel transformed = this.originalModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);

        // 2. determine the true scale applied by the model context
        Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
        ItemTransforms transforms = this.originalModel.getTransforms();
        if (transforms != ItemTransforms.NO_TRANSFORMS) {
            scale.set(transforms.getTransform(cameraTransformType).scale);
        }

        // 3. if root scale was flat, check the returned transformed perspective sub-model
        if (scale.x() == 1.0F && scale.y() == 1.0F) {
            ItemTransforms subTransforms = transformed.getTransforms();
            if (subTransforms != ItemTransforms.NO_TRANSFORMS) {
                ItemTransform subTransform = subTransforms.getTransform(cameraTransformType);
                if (subTransform == ItemTransform.NO_TRANSFORM) {
                    subTransform = subTransforms.getTransform(ItemDisplayContext.FIXED);
                }
                if (subTransform != ItemTransform.NO_TRANSFORM) {
                    scale.set(subTransform.scale);
                }
            }
        }

        // 4. normalize thickness or fallback to base if scale matches screen space execution
        float perspectiveBaseTexel = this.contextScales.getOrDefault(cameraTransformType, this.defaultTexelSize);

        // if the model architecture returns itself, it means scale is applied via matrix stack only
        boolean isVanillaStyle = transformed == this.originalModel;
        float finalTexelSize = (scale.x() != 0.0F && !isVanillaStyle) ? perspectiveBaseTexel / Math.abs(scale.x()) : perspectiveBaseTexel;

        BakedModel finalModel = new InflatedBakedModel(transformed, this.contextScales, this.defaultTexelSize, finalTexelSize, this.transformCache);
        this.transformCache.put(cameraTransformType, finalModel);
        return finalModel;
    }


//    @Override
//    public @NotNull List<BakedModel> getRenderPasses(@NotNull ItemStack itemStack, boolean fabulous) {
//        return List.of(this, originalModel);
//    }
//
//    @Override
//    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
//        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
//        return List.of(GLRenderTypes.getItemOutlineRenderType(ItemOutlineUtil.getColorComponents(itemStack, partialTick)));
//
//    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        if (side != null) {
            return List.of();
        } else {
            List<BakedQuad> originalQuads = this.originalModel.getQuads(state, null, rand);
            List<BakedQuad> result = new ArrayList<>();

            for (BakedQuad quad : originalQuads) {
                if (isFaceQuad(quad)) {
                    result.addAll(this.stampFaceQuad(quad));
                } else {
                    result.addAll(this.stampEdgeQuad(quad));
                }
            }

            return result;
        }
    }

    private List<BakedQuad> stampFaceQuad(BakedQuad quad) {
        int[] v = quad.getVertices();
        Vector3f[] pos = new Vector3f[4];

        for (int i = 0; i < 4; ++i) {
            pos[i] = readPos(v, i);
        }

        Vector3f edgeU = (new Vector3f(pos[1])).sub(pos[0]);
        Vector3f edgeV = (new Vector3f(pos[3])).sub(pos[0]);
        if (!(edgeU.lengthSquared() < 1.0E-12F) && !(edgeV.lengthSquared() < 1.0E-12F)) {
            Vector3f dirU = (new Vector3f(edgeU)).normalize();
            Vector3f dirV = (new Vector3f(edgeV)).normalize();
            Vector3f normal = unpackNormal(v[7]);
            if (normal.lengthSquared() < 1.0E-6F) {
                normal = computeFaceNormal(v);
            }

            Vector3f normalPush = (new Vector3f(normal)).mul(this.texelSize);
            List<BakedQuad> stamps = new ArrayList<>(FACE_STAMP_OFFSETS.length);

            for (int[] signs : FACE_STAMP_OFFSETS) {
                Vector3f offset = (new Vector3f(dirU))
                        .mul(signs[0] * this.texelSize)
                        .add((new Vector3f(dirV)).mul(signs[1] * this.texelSize))
                        .add(normalPush);
                stamps.add(this.translateAndFlip(quad, v, offset));
            }

            return stamps;
        } else {
            return List.of(this.translateAndFlip(quad, v, new Vector3f()));
        }
    }

    private List<BakedQuad> stampEdgeQuad(BakedQuad quad) {
        int[] v = quad.getVertices();
        Vector3f[] pos = new Vector3f[4];

        for (int i = 0; i < 4; ++i) {
            pos[i] = readPos(v, i);
        }

        Vector3f edgeU = (new Vector3f(pos[1])).sub(pos[0]);
        Vector3f edgeV = (new Vector3f(pos[3])).sub(pos[0]);
        if (!(edgeU.lengthSquared() < 1.0E-6F) && !(edgeV.lengthSquared() < 1.0E-6F)) {
            Vector3f normal = unpackNormal(v[7]);
            if (normal.lengthSquared() < 1.0E-6F) {
                normal = computeFaceNormal(v);
            }

            boolean uIsDepth = edgeU.lengthSquared() < edgeV.lengthSquared();
            Vector3f dirDepth = uIsDepth ? (new Vector3f(edgeU)).normalize() : (new Vector3f(edgeV)).normalize();
            Vector3f dirRun = uIsDepth ? (new Vector3f(edgeV)).normalize() : (new Vector3f(edgeU)).normalize();
            Vector3f center = new Vector3f();

            for (Vector3f p : pos) {
                center.add(p);
            }

            center.mul(0.25F);
            float avgTexel = (this.texelSize + this.texelSize) * 0.5F;
            float halfDepth = (uIsDepth ? edgeU.length() : edgeV.length()) * 0.5F;
            float scale = (halfDepth + avgTexel) / halfDepth;
            List<BakedQuad> result = new ArrayList<>(3);
            result.add(this.createInflatedQuad(quad, v, pos, center, dirDepth, normal, scale, new Vector3f()));
            result.add(this.createInflatedQuad(quad, v, pos, center, dirDepth, normal, scale, (new Vector3f(dirRun)).mul(this.texelSize)));
            result.add(this.createInflatedQuad(quad, v, pos, center, dirDepth, normal, scale, (new Vector3f(dirRun)).mul(-this.texelSize)));
            return result;
        } else {
            return List.of(this.translateAndFlip(quad, v, new Vector3f()));
        }
    }

    private BakedQuad createInflatedQuad(BakedQuad quad, int[] v, Vector3f[] pos, Vector3f center, Vector3f dirDepth, Vector3f normal, float scale, Vector3f runOffset) {
        int[] out = v.clone();

        for (int i = 0; i < 4; ++i) {
            int base = i * 8;
            Vector3f rel = (new Vector3f(pos[i])).sub(center);
            float depthComponent = rel.dot(dirDepth);
            Vector3f nonDepthComponent = (new Vector3f(rel)).fma(-depthComponent, dirDepth);
            Vector3f newPos = (new Vector3f(center)).add(nonDepthComponent).fma(depthComponent * scale, dirDepth).fma(this.texelSize, normal).add(runOffset);
            out[base] = Float.floatToRawIntBits(newPos.x());
            out[base + 1] = Float.floatToRawIntBits(newPos.y());
            out[base + 2] = Float.floatToRawIntBits(newPos.z());
        }

        return this.finishFlippedQuad(quad, out);
    }

    private BakedQuad translateAndFlip(BakedQuad source, int[] original, Vector3f offset) {
        int[] v = original.clone();

        for (int i = 0; i < 4; ++i) {
            int base = i * 8;
            v[base] = Float.floatToRawIntBits(Float.intBitsToFloat(v[base]) + offset.x());
            v[base + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(v[base + 1]) + offset.y());
            v[base + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(v[base + 2]) + offset.z());
        }

        return this.finishFlippedQuad(source, v);
    }

    private BakedQuad finishFlippedQuad(BakedQuad source, int[] v) {
        int[] flipped = VertexHelper.flip(v);
        return new BakedQuad(flipped, source.getTintIndex(), source.getDirection().getOpposite(), source.getSprite(), source.isShade());
    }

    private static boolean isFaceQuad(BakedQuad quad) {
        Direction dir = quad.getDirection();
        return dir == Direction.NORTH || dir == Direction.SOUTH;
    }

    private static Vector3f unpackNormal(int packed) {
        float x = (float) ((byte) (packed & 255)) / 127.0F;
        float y = (float) ((byte) (packed >> 8 & 255)) / 127.0F;
        float z = (float) ((byte) (packed >> 16 & 255)) / 127.0F;
        return new Vector3f(x, y, z);
    }

    private static Vector3f computeFaceNormal(int[] v) {
        Vector3f p0 = readPos(v, 0);
        Vector3f p1 = readPos(v, 1);
        Vector3f p2 = readPos(v, 2);
        Vector3f e1 = (new Vector3f(p1)).sub(p0);
        Vector3f e2 = (new Vector3f(p2)).sub(p0);
        return e1.cross(e2).normalize();
    }

    private static Vector3f readPos(int[] v, int vertexIndex) {
        int base = vertexIndex * 8;
        return new Vector3f(Float.intBitsToFloat(v[base]), Float.intBitsToFloat(v[base + 1]), Float.intBitsToFloat(v[base + 2]));
    }

    public  record ContextScale(ItemDisplayContext context, float texelSize) {
    }
}