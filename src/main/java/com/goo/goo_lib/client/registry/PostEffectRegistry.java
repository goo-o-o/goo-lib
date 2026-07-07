package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.client.render.pipeline.EntityShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.GuiShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.ScreenPostEffectPipeline;
import com.goo.goo_lib.client.render.pipeline.ShaderPipeline;
import com.goo.goo_lib.common.GooLib;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages post-processing effect chains.
 * Adapted from <a href="https://github.com/AlexModGuy/Citadel">Citadel</a>.<br>
 * <p>
 * Three pipeline types are supported, each with its own lifecycle:<br>
 * <br>
 * {@link GuiShaderPipeline} — driven by ClientEvents (RenderGuiEvent/ScreenEvent):<br>
 * dispatchPreGui() → GUI renders → dispatchPostGui()<br>
 * <br>
 * {@link EntityShaderPipeline} — driven by LevelRendererMixin:<br>
 * clearAndBindWrite() → entities render → processEffects() → blitEffects()<br>
 * <br>
 * {@link ScreenPostEffectPipeline} — driven by this class (AFTER_LEVEL event):<br>
 * onBeforeProcess() → processAndBlitWith()<br>
 */
@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class PostEffectRegistry {

    private static final List<ShaderPipeline> pipelines = new ArrayList<>();
    private static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public static void registerPipeline(ShaderPipeline pipeline) {
        pipelines.add(pipeline);
    }

    public static void onInitializeOutline() {
        clear();
        Minecraft mc = Minecraft.getInstance();
        for (ShaderPipeline pipeline : pipelines) {
            ResourceLocation location = pipeline.getLocation();
            PostChain postChain = null;
            RenderTarget renderTarget = null;
            try {
                postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), location);
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                renderTarget = postChain.getTempTarget("final");
            } catch (IOException e) {
                GooLib.LOGGER.warn("Failed to load shader: {}", location, e);
            } catch (JsonSyntaxException e) {
                GooLib.LOGGER.warn("Failed to parse shader: {}", location, e);
            }
            postEffects.put(location, new PostEffect(postChain, renderTarget));
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

    public static RenderTarget getRenderTargetFor(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        return effect == null ? null : effect.renderTarget;
    }

    public static RenderTarget getTempTarget(ResourceLocation location, String name) {
        PostEffect effect = postEffects.get(location);
        if (effect == null || effect.postChain == null) return null;
        return effect.postChain.getTempTarget(name);
    }

    // ── Enable / Disable ───────────────────────────────────────────────────

    public static void renderEffectForNextTick(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        if (effect != null) effect.enabled = true;
    }

    public static void setEnabled(ResourceLocation location, boolean enabled) {
        PostEffect effect = postEffects.get(location);
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
            if (!sp.isEnabled() || !sp.isReadyThisFrame(mc)) continue;
            PostChain chain = getPostChain(pipeline.getLocation());
            if (chain == null) continue;
            sp.onBeforeProcess(chain, event);
            renderEffectForNextTick(pipeline.getLocation());
            processAndBlitWith(pipeline.getLocation(), sp.getBlitMode());
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
                clearTarget(pipeline.getLocation(), inputName);
            }
        }
    }

    /**
     * Call after flushing GUI buffer sources to process and blit all GUI pipelines.
     */
    public static void dispatchPostGui() {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof GuiShaderPipeline gui)) continue;
            if (!gui.isEnabled()) continue;
            gui.flushBuffers();
            PostChain chain = getPostChain(pipeline.getLocation());
            if (chain != null) gui.onBeforeProcess(chain);
            renderEffectForNextTick(pipeline.getLocation());
            processAndBlitWith(pipeline.getLocation(), gui.getBlitMode());
        }
    }

    // ── Entity Pipeline ────────────────────────────────────────────────────
    // Called from LevelRendererMixin in three separate phases.

    /**
     * Phase 1 — before entities render: clear effect targets and bind for writing.
     */
    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof EntityShaderPipeline entity)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation());
            if (postEffect == null || !postEffect.enabled || postEffect.postChain == null) continue;
            PostChain chain = postEffect.postChain;
            postEffect.renderTarget.clear(Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
            entity.onBeforeEntities(mainTarget, chain);
        }
    }

    /**
     * Phase 2 — after entities render, before outline batch ends: run post chains.
     */
    public static void processEffects(RenderTarget mainTarget) {
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof EntityShaderPipeline)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation());
            if (postEffect == null || !postEffect.enabled || postEffect.postChain == null) continue;
            postEffect.postChain.process(Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
            mainTarget.bindWrite(false);
        }
    }

    /**
     * Phase 3 — end of renderLevel: blit all enabled entity effects to screen.
     */
    public static void blitEffects() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        for (ShaderPipeline pipeline : pipelines) {
            if (!(pipeline instanceof EntityShaderPipeline)) continue;
            PostEffect postEffect = postEffects.get(pipeline.getLocation());
            if (postEffect == null || postEffect.postChain == null || !postEffect.enabled) continue;
            Minecraft mc = Minecraft.getInstance();
            postEffect.renderTarget.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
            postEffect.renderTarget.clear(Minecraft.ON_OSX);
            mc.getMainRenderTarget().bindWrite(false);
            postEffect.enabled = false;
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    // ── GUI Bloom Pipeline (legacy helpers) ───────────────────────────────
    // Still used by renderGuiWithEffect for manual GUI elements.

    /**
     * Clears a named intermediate target.
     */
    public static void clearTarget(ResourceLocation location, String targetName) {
        RenderTarget target = getTempTarget(location, targetName);
        if (target != null) {
            target.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    /**
     * Processes a single effect and blits its "final" target additively.
     */
    public static void processAndBlit(ResourceLocation location) {
        processAndBlitWith(location, ShaderPipeline.BlitMode.ADDITIVE);
    }

    public static void blitEffectOpaque(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        if (effect == null || effect.postChain == null || !effect.enabled) return;
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        effect.renderTarget.blitToScreen(
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight(), false);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        effect.enabled = false;
    }

    /**
     * Convenience wrapper for manual GUI elements (images, sprites, etc).
     */
    public static void renderGuiWithEffect(ResourceLocation location, GuiGraphics guiGraphics, Runnable renderContent) {
        PostEffect effect = postEffects.get(location);
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
        processAndBlit(location);
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private static void processAndBlitWith(ResourceLocation location, ShaderPipeline.BlitMode mode) {
        PostEffect effect = postEffects.get(location);
        if (effect == null || !effect.enabled || effect.postChain == null) return;
        Minecraft mc = Minecraft.getInstance();
        effect.postChain.process(mc.getTimer().getGameTimeDeltaTicks());
        mc.getMainRenderTarget().bindWrite(false);
        blitToScreen(effect.renderTarget, mode);
        effect.enabled = false;
    }

    private static void blitToScreen(RenderTarget target, ShaderPipeline.BlitMode mode) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();
        if (mode == ShaderPipeline.BlitMode.ADDITIVE) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.disableDepthTest();
            target.blitToScreen(w, h, false);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            target.blitToScreen(w, h, false);
        }
    }

    /** Returns the raw PostChain for uniform manipulation. */
    @Nullable
    public static PostChain getPostChain(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        return effect == null ? null : effect.postChain;
    }

    private static class PostEffect {
        final PostChain postChain;
        final RenderTarget renderTarget;
        boolean enabled = false;

        PostEffect(PostChain postChain, RenderTarget renderTarget) {
            this.postChain = postChain;
            this.renderTarget = renderTarget;
        }

        void close() {
            if (postChain != null) postChain.close();
        }

        void resize(int w, int h) {
            if (postChain != null) postChain.resize(w, h);
        }
    }
}
