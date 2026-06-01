package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.mixin.CompositeRenderTypeAccessor;
import com.goo.goo_lib.mixin.CompositeStateAccessor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GLRenderTypes {

    private static final Map<RenderType, RenderType> BLOOM_CACHE = new HashMap<>();
    private static final Map<RenderType, RenderType> FIRE_CACHE = new HashMap<>();
    private static final Map<RenderType, RenderType> ABYSSAL_CACHE = new HashMap<>();

    private static RenderType create(String name, RenderType sourceType, Supplier<ShaderInstance> shader) {
        RenderStateShard.EmptyTextureStateShard textureState =
                ((CompositeStateAccessor) (Object) ((CompositeRenderTypeAccessor) sourceType).getState()).getTextureState();

        return RenderType.create(
                GooLib.MOD_ID + ":" + name,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(shader))
                        .setTextureState(textureState)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .createCompositeState(false)
        );
    }

    public static RenderType textBloom(RenderType source) {
        return BLOOM_CACHE.computeIfAbsent(source, s -> create("text_bloom", s, GLShaders::getTextBloomShader));
    }

    public static RenderType textFire(RenderType source) {
        return FIRE_CACHE.computeIfAbsent(source, s -> create("text_fire", s, GLShaders::getTextFireShader));
    }

    public static RenderType textAbyssal(RenderType source) {
        return ABYSSAL_CACHE.computeIfAbsent(source, s -> create("text_abyssal", s, GLShaders::getTextAbyssalShader));
    }

    public static RenderType sparksOverlay() {
        return RenderType.create(
                "sparks_overlay",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        // CRITICAL CHANGE: We supply a custom execution block to the ShaderStateShard!
                        .setShaderState(new RenderStateShard.ShaderStateShard(GLShaders::getSparksOverlayShader) {
                            @Override
                            public void setupRenderState() {
                                super.setupRenderState();
                                // Force the shader to look at Texture Unit 1 every single time this render type binds
                                if (GLShaders.getSparksOverlayShader() != null) {
                                    GLShaders.getSparksOverlayShader().setSampler("NoiseSampler", 1);
                                }
                            }
                        })
                        .setTextureState(new RenderStateShard.MultiTextureStateShard.Builder()
                                .add(TextureAtlas.LOCATION_BLOCKS, false, false) // Slot 0
                                .add(GooLib.loc("textures/effect/noise.png"), false, true) // Slot 1 (Must be true for tile/repeat!)
                                .build())
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false)
        );
    }
}