package com.goo.goo_lib.client.render.models;

import com.goo.goo_lib.util.HullExpander;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class OutlineBakedModel extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation MARKER = ResourceLocation.fromNamespaceAndPath("mymod", "item/sword_outline_marker");
    private static final float THICKNESS = 1f / 16f;
    private static final int OUTLINE_COLOR = 0xFF000000;

    private final Map<Direction, List<BakedQuad>> outlineCache = new EnumMap<>(Direction.class);
    private List<BakedQuad> nullDirCache;

    public OutlineBakedModel(BakedModel original) {
        super(original);
    }

    @Override
    public @NotNull List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(originalModel, new SinglePass());
    }

    private List<BakedQuad> filterAndExpand(@Nullable Direction dir, RandomSource random) {
        List<BakedQuad> all = originalModel.getQuads(null, dir, random);
        List<BakedQuad> marked = all.stream()
            .filter(q -> q.getSprite().contents().name().equals(MARKER))
            .toList();
        return HullExpander.expand(marked, THICKNESS, OUTLINE_COLOR);
    }

    private class SinglePass implements BakedModel {
        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            if (direction == null) {
                if (nullDirCache == null) nullDirCache = filterAndExpand(null, random);
                return nullDirCache;
            }
            return outlineCache.computeIfAbsent(direction, d -> filterAndExpand(d, random));
        }

        @Override
        public @NotNull List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
            return List.of(RenderType.CUTOUT);
        }

        @Override public boolean useAmbientOcclusion() { return false; }
        @Override public boolean isGui3d() { return originalModel.isGui3d(); }
        @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
        @Override public boolean isCustomRenderer() { return false; }
        @Override public TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
        @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    }
}