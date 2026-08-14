package com.goo.goo_lib.client.render;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.render.pipeline.WorldShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.GuiShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.ScreenPostEffectPipeline;
import com.goo.goo_lib.client.render.pipeline.ShaderPipeline;
import com.goo.goo_lib.common.GooLib;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages post-processing effect chains.
 * Adapted from <a href="https://github.com/AlexModGuy/Citadel">Citadel</a>.<br>
 * <p>
 * Three pipeline types are supported, each with its own lifecycle:<br>
 * <br>
 * {@link GuiShaderPipeline} — driven by ClientEvents (RenderGuiEvent/ScreenEvent):<br>
 * dispatchPreGui() → GUI renders → dispatchPostGui()<br>
 * <br>
 * {@link WorldShaderPipeline} — driven by LevelRendererMixin:<br>
 * clearAndBindWrite() → entities render → processEffects() → blitEffects()<br>
 * <br>
 * {@link ScreenPostEffectPipeline} — driven by this class (AFTER_LEVEL event):<br>
 * onBeforeProcess() → processAndBlitWith()<br>
 */
@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PostEffectRegistry {

    private static final List<ShaderPipeline> pipelines = new ArrayList<>();
    public static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public static void registerPipeline(ShaderPipeline pipeline) {
        pipelines.add(pipeline);
    }

    public static void onInitializeOutline(ResourceManager resourceManager) {
        clear();
        Minecraft mc = Minecraft.getInstance();
        for (ShaderPipeline pipeline : pipelines) {
            ResourceLocation location = pipeline.getLocation();
            ShaderPipeline.PipelineStage stage = ShaderPipeline.PipelineStage.SCREEN;
            if (pipeline instanceof GuiShaderPipeline) stage = ShaderPipeline.PipelineStage.GUI;
            else if (pipeline instanceof WorldShaderPipeline) stage = ShaderPipeline.PipelineStage.WORLD;

            ResourceLocation uniqueKey = location.withSuffix(stage.suffix());

            PostChain postChain = null;
            RenderTarget renderTarget = null;
            try {
                // pass original location since thats the file path, but store with unique key
                postChain = new PostChain(mc.getTextureManager(), resourceManager, mc.getMainRenderTarget(), location);
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                renderTarget = postChain.getTempTarget("final");
            } catch (IOException e) {
                GooLib.LOGGER.warn("Failed to load shader: {}", location, e);
            } catch (JsonSyntaxException e) {
                GooLib.LOGGER.warn("Failed to parse shader: {}", location, e);
            }
            postEffects.put(uniqueKey, new PostEffect(postChain, renderTarget));
        }
    }

    public static void clear() {
        postEffects.values().forEach(PostEffect::close);
        postEffects.clear();
    }

    public static void resize(int width, int height) {
        postEffects.values().forEach(e -> e.resize(width, height));
    }

    // ── Target Access ──────────────────────────────────────────────────────

    public static RenderTarget getRenderTargetFor(ResourceLocation location, ShaderPipeline.PipelineStage stage) {
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        return effect == null ? null : effect.renderTarget;
    }

    public static RenderTarget getTempTarget(ResourceLocation location, ShaderPipeline.PipelineStage stage, String name) {
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        if (effect == null || effect.postChain == null) return null;
        return effect.postChain.getTempTarget(name);
    }

    // ── Enable / Disable ───────────────────────────────────────────────────

    public static void renderEffectForNextTick(ResourceLocation location, ShaderPipeline.PipelineStage stage) {
        setEnabled(location, stage, true);
    }

    public static void setEnabled(ResourceLocation location, ShaderPipeline.PipelineStage stage, boolean enabled) {
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        if (effect != null) effect.enabled = enabled;
    }

    // ── Screen Post-Effect Pipeline ────────────────────────────────────────
    // Dispatched automatically for all registered ScreenPostEffectPipeline instances.

    @SubscribeEvent
    public static void onRenderLevelForScreen(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft mc = Minecraft.getInstance();
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof ScreenPostEffectPipeline sp)) continue;
            if (sp.isDisabled() || !sp.isReadyThisFrame(mc)) continue;
            PostChain chain = getPostChain(pipeline.getLocation(), pipeline.getStage());
            if (chain == null) continue;
            sp.onBeforeProcess(chain, event);
            renderEffectForNextTick(pipeline.getLocation(), sp.getStage());
            processAndBlitWith(pipeline);
        }
    }

    // ── GUI Pipeline Dispatch ──────────────────────────────────────────────
    // Called from ClientEvents at RenderGuiEvent.Pre / Post and ScreenEvent.Render.Post.

    /**
     * Call on RenderGuiEvent.Pre to clear input targets before the GUI frame.
     */
    public static void dispatchPreGui() {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof GuiShaderPipeline gui)) continue;
            String inputName = gui.getInputTargetName();
            if (inputName != null) {
                clearTarget(pipeline.getLocation(), pipeline.getStage(), inputName);
            }
        }
    }

    /**
     * Call after flushing GUI buffer sources to process and blit all GUI pipelines.
     */
    public static void dispatchPostGui() {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof GuiShaderPipeline gui)) continue;
            if (gui.isDisabled()) continue;

            // Clear the input target BEFORE this frame's declarative content
            // accumulates into it. Without this, any earlier manual/immediate
            // usage of the same (location, stage) this frame (e.g. RageMeterOverlay's
            // renderGuiWithEffect calls) leaves leftover un-bloomed pixels sitting
            // in this target, which then get swept up and reprocessed here with
            // whatever Intensity this pass happens to be using — producing a
            // second, wrongly-tinted bloom pass stacked on top of the correct one.
            String inputName = gui.getInputTargetName();
            if (inputName != null) {
                clearTarget(pipeline.getLocation(), pipeline.getStage(), inputName);
            }

            gui.flushBuffers();
            PostChain chain = getPostChain(pipeline.getLocation(), pipeline.getStage());
            if (chain != null) gui.onBeforeProcess(chain);
            renderEffectForNextTick(pipeline.getLocation(), pipeline.getStage());
            processAndBlitWith(pipeline.getLocation(), pipeline.getStage(), gui.getBlitMode());
        }
    }

    private static void blitDepthAwareTranslucent(RenderTarget colorTarget, RenderTarget depthTarget) {
        ShaderInstance shader = GLRenderTypes.InternalShaders.PIXELATE_COMPOSITE.getInstance();
        if (shader == null) return;
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._depthMask(true);
        GlStateManager._viewport(0, 0, w, h);
        shader.setSampler("DiffuseSampler", colorTarget.getColorTextureId());
        shader.setSampler("DepthSampler", depthTarget.getDepthTextureId());

        shader.apply();
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        shader.clear();

        GlStateManager._colorMask(true, true, true, true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Universal blit helper that respects the pipeline's BlitMode.
     */
    private static void blitToScreen(RenderTarget target, ShaderPipeline.BlitMode mode) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();

        switch (mode) {
            case ADDITIVE -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                RenderSystem.disableDepthTest();
                target.blitToScreen(w, h, false);
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
            case TRANSLUCENT -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.enableDepthTest();
                target.blitToScreen(w, h, false);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
            case OPAQUE -> {
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                target.blitToScreen(w, h, false);
            }
        }
    }
    // ── Entity Pipeline ────────────────────────────────────────────────────
    // Called from LevelRendererMixin in three separate phases.

    /**
     * Phase 1 — before entities render: clear effect targets and bind for writing.
     */
    @SuppressWarnings("ConstantConditions")
    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof WorldShaderPipeline world)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation().withSuffix(ShaderPipeline.PipelineStage.WORLD.suffix())); // is always a world pipeline stage
            if (postEffect == null || postEffect.postChain == null) continue;
//            if (postEffect == null || !postEffect.enabled || postEffect.postChain == null) continue;
            PostChain chain = postEffect.postChain;
            postEffect.renderTarget.clear(Minecraft.ON_OSX);

            mainTarget.bindWrite(false);
            world.onRenderStart(mainTarget, chain);
        }
    }

    /**
     *
     * Phase 2 — after entities render, before outline batch ends: run post chains.
     * Edit: process stage now configurable
     */
    public static void processEffects(RenderTarget mainTarget, RenderLevelStageEvent.Stage stage) {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof WorldShaderPipeline worldShaderPipeline)) continue;
            if (!worldShaderPipeline.shouldProcessForStage(stage)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation().withSuffix(ShaderPipeline.PipelineStage.WORLD.suffix())); // is always an entity pipeline stage
            if (postEffect == null || !postEffect.enabled || postEffect.postChain == null) continue;
            worldShaderPipeline.onBeforeProcess(postEffect, mainTarget);
            if (postEffect.depthSnapshot != null) {
                postEffect.depthSnapshot.copyDepthFrom(postEffect.renderTarget); // grab real entity depth BEFORE process() flattens it
            }
            postEffect.postChain.process(Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
            worldShaderPipeline.onAfterProcess(postEffect, mainTarget);
            mainTarget.bindWrite(false);
        }
    }

    /**
     * Phase 3 — end of renderLevel: blit all enabled entity effects to screen using their configured BlitMode.
     */
    public static void blitEffects(RenderLevelStageEvent.Stage stage) {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof WorldShaderPipeline entityPipeline)) continue;
            if (!entityPipeline.shouldBlitForStage(stage)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation().withSuffix(ShaderPipeline.PipelineStage.WORLD.suffix()));
            if (postEffect == null || postEffect.postChain == null || !postEffect.enabled) continue;

            Minecraft mc = Minecraft.getInstance();

            // Dispatch using the pipeline's configured BlitMode (e.g. OPAQUE or TRANSLUCENT)
            entityPipeline.onBeforeBlit(postEffect);
            if (entityPipeline.getBlitMode() == ShaderPipeline.BlitMode.DEPTH_AWARE_TRANSLUCENT) {
                blitDepthAwareTranslucent(postEffect.renderTarget, postEffect.depthSnapshot);
            } else {
                blitToScreen(postEffect.renderTarget, entityPipeline.getBlitMode());
            }
            entityPipeline.onAfterBlit(postEffect);
            postEffect.renderTarget.clear(Minecraft.ON_OSX);
            mc.getMainRenderTarget().bindWrite(false);
            postEffect.enabled = false;
        }
    }

    // ── GUI Bloom Pipeline (legacy helpers) ───────────────────────────────
    // Still used by renderGuiWithEffect for manual GUI elements.

    /**
     * Clears a named intermediate target.
     */
    public static void clearTarget(ResourceLocation location, ShaderPipeline.PipelineStage stage, String targetName) {
        RenderTarget target = getTempTarget(location, stage, targetName);
        if (target != null) {
            target.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    public static void processAndBlitWith(ShaderPipeline pipeline) {
        ResourceLocation location = pipeline.getLocation();
        ShaderPipeline.PipelineStage stage = pipeline.getStage();
        ShaderPipeline.BlitMode mode = pipeline.getBlitMode();
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        if (effect == null || !effect.enabled || effect.postChain == null) return;


        Minecraft mc = Minecraft.getInstance();
        pipeline.onBeforeProcess(effect, mc.getMainRenderTarget());
        effect.postChain.process(mc.getTimer().getGameTimeDeltaTicks());
        pipeline.onAfterProcess(effect, mc.getMainRenderTarget());
        mc.getMainRenderTarget().bindWrite(false);

        pipeline.onBeforeBlit(effect);

        if (mode == ShaderPipeline.BlitMode.DEPTH_AWARE_TRANSLUCENT) {
            blitDepthAwareTranslucent(effect.renderTarget, effect.depthSnapshot);
        } else {
            blitToScreen(effect.renderTarget, mode);
        }
        pipeline.onAfterBlit(effect);


        effect.enabled = false;
    }

    public static void processAndBlitWith(ResourceLocation location, ShaderPipeline.PipelineStage stage, ShaderPipeline.BlitMode mode) {
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        if (effect == null || !effect.enabled || effect.postChain == null) return;

        Minecraft mc = Minecraft.getInstance();
        effect.postChain.process(mc.getTimer().getGameTimeDeltaTicks());
        mc.getMainRenderTarget().bindWrite(false);

        if (mode == ShaderPipeline.BlitMode.DEPTH_AWARE_TRANSLUCENT) {
            blitDepthAwareTranslucent(effect.renderTarget, effect.depthSnapshot);
        } else {
            blitToScreen(effect.renderTarget, mode);
        }
        effect.enabled = false;
    }

    /**
     * Convenience wrapper that configures post-pass uniforms before executing
     * and blitting isolated contents.
     */
    public static void renderGuiWithEffect(
            ResourceLocation location,
            GuiGraphics guiGraphics,
            Consumer<PostPass> uniformModifier,
            Runnable renderContent,
            ShaderPipeline.BlitMode blitMode
    ) {
        modifyUniforms(location, ShaderPipeline.PipelineStage.GUI, uniformModifier);
        renderGuiWithEffect(location, guiGraphics, renderContent, blitMode);
    }

    /**
     * Convenience wrapper for manual GUI elements (images, sprites, etc).
     */
    public static void renderGuiWithEffect(ResourceLocation location, GuiGraphics guiGraphics, Runnable renderContent, ShaderPipeline.BlitMode blitMode) {
        PostEffect effect = postEffects.get(location.withSuffix(ShaderPipeline.PipelineStage.GUI.suffix()));
        if (effect == null || effect.postChain == null) {
            renderContent.run();
            return;
        }

        RenderTarget inputTarget = effect.postChain.getTempTarget("input");
        if (inputTarget == null) {
            renderContent.run();
            return;
        }

        guiGraphics.flush();
        inputTarget.clear(Minecraft.ON_OSX);
        inputTarget.bindWrite(false);

        renderContent.run();

        guiGraphics.flush();

        effect.enabled = true;
        processAndBlitWith(location, ShaderPipeline.PipelineStage.GUI, blitMode);
    }

    // ── Internal ───────────────────────────────────────────────────────────

    /**
     * Returns the raw PostChain for uniform manipulation.
     */
    @Nullable
    public static PostChain getPostChain(ResourceLocation location, ShaderPipeline.PipelineStage stage) {
        PostEffect effect = postEffects.get(location.withSuffix(stage.suffix()));
        return effect == null ? null : effect.postChain;
    }

    @Nullable
    public static List<PostPass> getPostPasses(ResourceLocation location, ShaderPipeline.PipelineStage stage) {
        PostChain chain = getPostChain(location, stage);
        return chain != null ? chain.passes : null;
    }

    public static void modifyUniforms(ResourceLocation location, ShaderPipeline.PipelineStage stage, Consumer<PostPass> consumer) {
        List<PostPass> passes = getPostPasses(location, stage);
        if (passes != null) {
            passes.forEach(consumer);
        }
    }


    public static class PostEffect {
        public final PostChain postChain;
        public final RenderTarget renderTarget;
        public boolean enabled = false;
        public RenderTarget depthSnapshot; // NEW

        PostEffect(PostChain postChain, RenderTarget renderTarget) {
            this.postChain = postChain;
            this.renderTarget = renderTarget;
            if (renderTarget != null) {
                this.depthSnapshot = new TextureTarget(renderTarget.width, renderTarget.height, true, Minecraft.ON_OSX);
            }
        }

        void resize(int w, int h) {
            if (postChain != null) postChain.resize(w, h);
            if (depthSnapshot != null) depthSnapshot.resize(w, h, Minecraft.ON_OSX); // add this
        }

        void close() {
            if (postChain != null) postChain.close();
        }
    }
}
