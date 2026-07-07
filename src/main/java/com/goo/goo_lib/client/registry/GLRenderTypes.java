package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.mixin.CompositeRenderTypeAccessor;
import com.goo.goo_lib.mixin.CompositeStateAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.client.renderer.RenderStateShard.*;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class GLRenderTypes {
    private static final Map<RenderType, RenderType> BLOOM_CACHE = new HashMap<>();

    public static void clearCaches() {
        BLOOM_CACHE.clear();
    }
    // ── Helper to Remove Text RenderType Duplication ─────────────────────

    private static RenderType createTextRenderType(String name, RenderType sourceType, Supplier<ShaderInstance> shaderSupplier) {
        return RenderType.create(
                GooLib.MOD_ID + ":" + name,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(shaderSupplier))
                        .setTextureState(((CompositeStateAccessor) (Object) ((CompositeRenderTypeAccessor) sourceType).getState()).getTextureState())
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(false)
        );
    }

    // ── Custom Dynamic Library Text Shaders ───────────────────────────────

    public static RenderType getBloom(RenderType sourceType) {
        return BLOOM_CACHE.computeIfAbsent(sourceType, s -> RenderType.create(
                        GooLib.MOD_ID + ":text_bloom",
                        DefaultVertexFormat.POSITION_TEX_COLOR,
                        VertexFormat.Mode.QUADS,
                        256,
                        false,
                        false,
                        RenderType.CompositeState.builder()
                                .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getTextBloomShader))
                                .setTextureState(((CompositeStateAccessor) (Object) ((CompositeRenderTypeAccessor) sourceType).getState()).getTextureState())
                                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                                .setDepthTestState(NO_DEPTH_TEST) // needed for overlay elements
                                .setOutputState(BLOOM_OUTPUT) // crucial: forces this text pass into your bloom target
                                .createCompositeState(false) // false means no sorting overhead, great for 2D UI elements
                )
        );
    }

    public static RenderType getNeon(RenderType source) {
        return createTextRenderType("neon", source, InternalShaders::getTextBloomShader);
    }

    public static RenderType getFlame(RenderType source) {
        return createTextRenderType("flame", source, InternalShaders::getRenderTypeFlameShader);
    }

    public static RenderType getAbyssal(RenderType source) {
        return createTextRenderType("abyssal", source, InternalShaders::getRenderTypeAbyssalShader);
    }

    // ── Standard World / UI Custom Render Types ───────────────────────────

    public static RenderType getEntitySeeThroughRenderType() {
        return RenderType.create(
                GooLib.MOD_ID + ":entity_see_through",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }

    public static RenderType getGalaxyRenderType() {
        return getGalaxyRenderType(LEQUAL_DEPTH_TEST);
    }

    public static RenderType getGalaxyRenderType(DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":galaxy_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeGalaxyShader))
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .createCompositeState(true)
        );
    }

    public static RenderType getMoltenRenderType() {
        return getMoltenRenderType(LEQUAL_DEPTH_TEST);
    }

    public static RenderType getMoltenRenderType(DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":molten_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeMoltenShader))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .createCompositeState(true)
        );
    }

    public static RenderType getMoltenRenderType(ResourceLocation resourceLocation) {
        return getMoltenRenderType(resourceLocation, LEQUAL_DEPTH_TEST);
    }

    public static RenderType getMoltenRenderType(ResourceLocation resourceLocation, DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":molten_textured_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeMoltenTextureShader))
                        .setTextureState(new TextureStateShard(resourceLocation, false, true))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .createCompositeState(true)
        );
    }

    public static RenderType getFireRenderType() {
        return getFireRenderType(LEQUAL_DEPTH_TEST);
    }

    public static RenderType getFireRenderType(DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":fire_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeFireShader))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .createCompositeState(true)
        );
    }

    public static RenderType getFireRenderType(ResourceLocation resourceLocation) {
        return getFireRenderType(resourceLocation, LEQUAL_DEPTH_TEST);
    }

    public static RenderType getFireRenderType(ResourceLocation resourceLocation, DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":fire_textured_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                1536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeFireTextureShader))
                        .setTextureState(new TextureStateShard(resourceLocation, false, true))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .createCompositeState(true)
        );
    }

    public static RenderType getBlurRenderType(ResourceLocation locationIn) {
        return getBlurRenderType(locationIn, LEQUAL_DEPTH_TEST);
    }

    public static RenderType getBlurRenderType(ResourceLocation locationIn, DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":blur_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeBlurShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .setOutputState(BLUR_OUTPUT)
                        .createCompositeState(true)
        );
    }

    public static RenderType getBloomRenderType(ResourceLocation locationIn) {
        return getBloomRenderType(locationIn, LEQUAL_DEPTH_TEST);
    }

    public static RenderType getBloomRenderType(ResourceLocation locationIn, DepthTestStateShard depthTestStateShard) {
        return RenderType.create(
                GooLib.MOD_ID + ":bloom_" + depthTestStateShard,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders::getRenderTypeBlurShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .setOutputState(BLOOM_OUTPUT)
                        .createCompositeState(true)
        );
    }

    /**
     * Post Effects
     */
    public static final ResourceLocation MOTION_BLUR_SHADER_LOCATION = GooLib.loc("shaders/post/motion_blur.json");


    public static final ResourceLocation BLUR_SHADER_LOCATION = GooLib.loc("shaders/post/blur.json");
    protected static final RenderStateShard.OutputStateShard BLUR_OUTPUT = new RenderStateShard.OutputStateShard("blur_target", () -> {
        RenderTarget target = PostEffectRegistry.getRenderTargetFor(BLUR_SHADER_LOCATION);
        if (target != null) {
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            target.bindWrite(false);
        }
    }, () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));


    public static final ResourceLocation BLOOM_SHADER_LOCATION = GooLib.loc("shaders/post/bloom.json");
    protected static final RenderStateShard.OutputStateShard BLOOM_OUTPUT = new RenderStateShard.OutputStateShard("bloom_target", () -> {
        RenderTarget target = PostEffectRegistry.getTempTarget(BLOOM_SHADER_LOCATION, "input");
        if (target != null) {
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            target.bindWrite(false);
        }
    }, () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));

    // ── Global Shader Registration Loop ───────────────────────────────────

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_bloom"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setTextBloomShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_flame"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setRenderTypeFlameShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_abyssal"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setRenderTypeAbyssalShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_blur"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setRenderTypeBlurShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_fire_texture"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setRenderTypeFireTextureShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_fire"), DefaultVertexFormat.POSITION_COLOR), InternalShaders::setRenderTypeFireShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_molten_texture"), DefaultVertexFormat.POSITION_TEX_COLOR), InternalShaders::setRenderTypeMoltenTextureShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_molten"), DefaultVertexFormat.POSITION_COLOR), InternalShaders::setRenderTypeMoltenShader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_galaxy"), DefaultVertexFormat.POSITION_COLOR), InternalShaders::setRenderTypeGalaxyShader);

            GooLib.LOGGER.info("Successfully consolidated and loaded internal shaders.");
        } catch (IOException exception) {
            GooLib.LOGGER.error("Failed to register unified pipeline shaders");
            exception.printStackTrace();
        }
    }

    // ── Encapsulated Internal Shader State Holder ─────────────────────────

    public static class InternalShaders {
        private static ShaderInstance renderTypeNeonShader;
        private static ShaderInstance renderTypeFlameShader;
        private static ShaderInstance renderTypeAbyssalShader;
        private static ShaderInstance renderTypeBlurShader;
        private static ShaderInstance renderTypeFireTextureShader;
        private static ShaderInstance renderTypeFireShader;
        private static ShaderInstance renderTypeMoltenTextureShader;
        private static ShaderInstance renderTypeMoltenShader;
        private static ShaderInstance renderTypeGalaxyShader;

        public static ShaderInstance getTextBloomShader() {
            return renderTypeNeonShader;
        }

        public static void setTextBloomShader(ShaderInstance instance) {
            renderTypeNeonShader = instance;
        }

        public static ShaderInstance getRenderTypeFlameShader() {
            return renderTypeFlameShader;
        }

        public static void setRenderTypeFlameShader(ShaderInstance instance) {
            renderTypeFlameShader = instance;
        }

        public static ShaderInstance getRenderTypeAbyssalShader() {
            return renderTypeAbyssalShader;
        }

        public static void setRenderTypeAbyssalShader(ShaderInstance instance) {
            renderTypeAbyssalShader = instance;
        }

        public static ShaderInstance getRenderTypeBlurShader() {
            return renderTypeBlurShader;
        }

        public static void setRenderTypeBlurShader(ShaderInstance instance) {
            renderTypeBlurShader = instance;
        }

        public static ShaderInstance getRenderTypeFireTextureShader() {
            return renderTypeFireTextureShader;
        }

        public static void setRenderTypeFireTextureShader(ShaderInstance instance) {
            renderTypeFireTextureShader = instance;
        }

        public static ShaderInstance getRenderTypeFireShader() {
            return renderTypeFireShader;
        }

        public static void setRenderTypeFireShader(ShaderInstance instance) {
            renderTypeFireShader = instance;
        }

        public static ShaderInstance getRenderTypeMoltenTextureShader() {
            return renderTypeMoltenTextureShader;
        }

        public static void setRenderTypeMoltenTextureShader(ShaderInstance instance) {
            renderTypeMoltenTextureShader = instance;
        }

        public static ShaderInstance getRenderTypeMoltenShader() {
            return renderTypeMoltenShader;
        }

        public static void setRenderTypeMoltenShader(ShaderInstance instance) {
            renderTypeMoltenShader = instance;
        }

        public static ShaderInstance getRenderTypeGalaxyShader() {
            return renderTypeGalaxyShader;
        }

        public static void setRenderTypeGalaxyShader(ShaderInstance instance) {
            renderTypeGalaxyShader = instance;
        }
    }
}