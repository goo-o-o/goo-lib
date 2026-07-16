package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.client.render.pipeline.ShaderPipeline;
import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.mixin.CompositeRenderTypeAccessor;
import com.goo.goo_lib.mixin.CompositeStateAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.client.renderer.RenderStateShard.*;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class GLRenderTypes {

    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_FOG = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader); // if crashes in the future, add our own core shader
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT_NO_FOG";
        }
    };

    private static final Map<RenderType, RenderType> TEXT_BLOOM_CACHE = new HashMap<>();

    public static void clearCaches() {
        TEXT_BLOOM_CACHE.clear();
    }


    public static RenderType getItemOutlineRenderType(float[] colors) {
        return RenderType.create(GooLib.MOD_ID + ":item_outline",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> {
                            ShaderInstance shader = InternalShaders.ITEM_OUTLINE.getInstance();
                            if (shader != null) {
                                shader.safeGetUniform("OutlineColor").set(colors[0], colors[1], colors[2], colors[3]);
                            }
                            return shader;
                        }))
                        .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true)
        );
    }

    private static RenderType createTextRenderType(String name, RenderType sourceType, Supplier<ShaderInstance> shaderSupplier) {
        return RenderType.create(
                GooLib.MOD_ID + ":text_" + name,
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

    public static RenderType getTextBloom(RenderType glyphRenderType) {
        return TEXT_BLOOM_CACHE.computeIfAbsent(glyphRenderType, s -> RenderType.create(
                GooLib.MOD_ID + ":text_bloom",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(InternalShaders.TEXT_BLOOM::getInstance))
                        .setTextureState(((CompositeStateAccessor) (Object) ((CompositeRenderTypeAccessor) s).getState()).getTextureState())
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setOutputState(BLOOM_OUTPUT_GUI)
                        .createCompositeState(false)
        ));
    }

    public static RenderType getSmoothWave(RenderType source) {
        return createTextRenderType("smooth_wave", source, InternalShaders.TEXT_SMOOTH_WAVE::getInstance);
    }


    public static RenderType getFlame(RenderType source) {
        return createTextRenderType("flame", source, InternalShaders.TEXT_FLAME::getInstance);
    }

    public static RenderType getAbyssal(RenderType source) {
        return createTextRenderType("abyssal", source, InternalShaders.TEXT_ABYSSAL::getInstance);
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.GALAXY::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.MOLTEN::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.MOLTEN_TEXTURE::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.FIRE::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.FIRE_TEXTURE::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.BLUR::getInstance))
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
                        .setShaderState(new RenderStateShard.ShaderStateShard(InternalShaders.BLOOM::getInstance))
                        .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(depthTestStateShard)
                        .setOutputState(BLOOM_OUTPUT_ENTITY)
                        .createCompositeState(true)
        );
    }

    /**
     * Post Effects
     */
    public static final ResourceLocation MOTION_BLUR_SHADER_LOCATION = GooLib.loc("shaders/post/motion_blur.json");


    public static final ResourceLocation BLUR_SHADER_LOCATION = GooLib.loc("shaders/post/blur.json");
    protected static final RenderStateShard.OutputStateShard BLUR_OUTPUT = new RenderStateShard.OutputStateShard("blur_target", () -> {
        RenderTarget target = PostEffectRegistry.getRenderTargetFor(BLUR_SHADER_LOCATION, ShaderPipeline.PipelineStage.ENTITY);
        if (target != null) {
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            target.bindWrite(false);
        }
    }, () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));


    public static final ResourceLocation BLOOM_SHADER_LOCATION = GooLib.loc("shaders/post/bloom.json");
    protected static final RenderStateShard.OutputStateShard BLOOM_OUTPUT_GUI = new RenderStateShard.OutputStateShard("bloom_target", () -> {
        RenderTarget target = PostEffectRegistry.getTempTarget(BLOOM_SHADER_LOCATION, ShaderPipeline.PipelineStage.GUI, "input");
        if (target != null) {
            // since we are getting a custom buffer named "input" and not the main minecraft render target, we need to clear it manually. done in onBeforeEntities now
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            target.bindWrite(false);
        }
    }, () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));

    protected static final RenderStateShard.OutputStateShard BLOOM_OUTPUT_ENTITY = new RenderStateShard.OutputStateShard("bloom_target", () -> {
        RenderTarget target = PostEffectRegistry.getTempTarget(BLOOM_SHADER_LOCATION, ShaderPipeline.PipelineStage.ENTITY, "input");
        if (target != null) {
            // since we are getting a custom buffer named "input" and not the main minecraft render target, we need to clear it manually. done in onBeforeEntities now
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            target.bindWrite(false);
        }
    }, () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));

    // ── Global Shader Registration Loop ───────────────────────────────────

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            for (InternalShaders shader : InternalShaders.values()) {
                event.registerShader(
                        new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_" + shader.name().toLowerCase(Locale.ROOT)), shader.getFormat()),
                        shader::setInstance
                );
            }
            GooLib.LOGGER.info("Successfully consolidated and loaded internal shaders.");
        } catch (IOException exception) {
            GooLib.LOGGER.error("Failed to register unified pipeline shaders", exception);
        }
    }

    // ── Encapsulated Internal Shader State Holder ─────────────────────────

    public enum InternalShaders {
        TEXT_BLOOM(DefaultVertexFormat.POSITION_TEX_COLOR),
        TEXT_FLAME(DefaultVertexFormat.POSITION_TEX_COLOR),
        TEXT_ABYSSAL(DefaultVertexFormat.POSITION_TEX_COLOR),
        TEXT_SMOOTH_WAVE(DefaultVertexFormat.POSITION_TEX_COLOR),
        BLUR(DefaultVertexFormat.POSITION_TEX_COLOR),
        BLOOM(DefaultVertexFormat.POSITION_TEX_COLOR),
        FIRE_TEXTURE(DefaultVertexFormat.POSITION_TEX_COLOR),
        ITEM_OUTLINE(DefaultVertexFormat.POSITION_TEX_COLOR),
        FIRE(DefaultVertexFormat.POSITION_COLOR),
        MOLTEN_TEXTURE(DefaultVertexFormat.POSITION_TEX_COLOR),
        MOLTEN(DefaultVertexFormat.POSITION_COLOR),
        GALAXY(DefaultVertexFormat.POSITION_COLOR);

        private final VertexFormat format;
        private ShaderInstance instance;

        InternalShaders(VertexFormat format) {
            this.format = format;
        }

        public VertexFormat getFormat() {
            return this.format;
        }

        @Nullable
        public ShaderInstance getInstance() {
            return this.instance;
        }

        public void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }
    }
}