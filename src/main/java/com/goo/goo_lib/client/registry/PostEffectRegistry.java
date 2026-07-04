package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.common.GooLib;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages post-processing effect chains.
 * Adapted from <a href="https://github.com/AlexModGuy/Citadel">Citadel</a>.
 *
 * Two pipelines are supported:
 *
 * 3D Entity pipeline (driven by LevelRendererMixin):
 *   clearAndBindWrite() → entities render into target → processEffects() → blitEffects()
 *
 * GUI Bloom pipeline (driven by ClientEvents):
 *   clearTarget("input") → text renders into input via BLOOM_OUTPUT → endBatch() → processAndBlit()
 *   OR
 *   renderGuiWithEffect() → wraps all of the above for manual GUI elements
 */
public class PostEffectRegistry {

    private static final List<ResourceLocation> registry = new ArrayList<>();
    private static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public static void registerEffect(ResourceLocation location) {
        registry.add(location);
    }

    public static void onInitializeOutline() {
        clear();
        Minecraft mc = Minecraft.getInstance();
        for (ResourceLocation location : registry) {
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

    /** Returns the "final" output target for this effect. */
    public static RenderTarget getRenderTargetFor(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        return effect == null ? null : effect.renderTarget;
    }

    /** Returns a named intermediate target from the post chain. */
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

    // ── 3D Entity Pipeline ─────────────────────────────────────────────────
    // Called from LevelRendererMixin in three separate phases.

    /** Phase 1 — before entities render: clear effect targets and bind for writing. */
    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (PostEffect postEffect : postEffects.values()) {
            if (postEffect.enabled && postEffect.postChain != null) {
                postEffect.renderTarget.clear(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);
            }
        }
    }

    /** Phase 2 — after entities render, before outline batch ends: run post chains. */
    public static void processEffects(RenderTarget mainTarget) {
        for (PostEffect postEffect : postEffects.values()) {
            if (postEffect.enabled && postEffect.postChain != null) {
                postEffect.postChain.process(Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
                mainTarget.bindWrite(false);
            }
        }
    }

    /** Phase 3 — end of renderLevel: blit all enabled effects to screen. Skips GUI-only effects. */
    public static void blitEffects() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        for (Map.Entry<ResourceLocation, PostEffect> entry : postEffects.entrySet()) {
            // Bloom is GUI-only — handled by ClientEvents, not the 3D pipeline
            if (entry.getKey().equals(GLRenderTypes.BLOOM_SHADER_LOCATION)) continue;
            PostEffect postEffect = entry.getValue();
            if (postEffect.postChain != null && postEffect.enabled) {
                postEffect.renderTarget.blitToScreen(
                        Minecraft.getInstance().getWindow().getWidth(),
                        Minecraft.getInstance().getWindow().getHeight(), false);
                postEffect.renderTarget.clear(Minecraft.ON_OSX);
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                postEffect.enabled = false;
            }
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    // ── GUI Bloom Pipeline ─────────────────────────────────────────────────
    // Called from ClientEvents. Bloom writes into "input", chain writes to "final".

    /**
     * Clears a named intermediate target.
     * Call on RenderGuiEvent.Pre to prevent stale bloom from previous frames.
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
     * Call after endBatch() so all pending vertex data has been flushed into "input".
     */
    public static void processAndBlit(ResourceLocation location) {
        PostEffect effect = postEffects.get(location);
        if (effect == null || !effect.enabled || effect.postChain == null) return;

        Minecraft mc = Minecraft.getInstance();
        effect.postChain.process(mc.getTimer().getGameTimeDeltaTicks());
        mc.getMainRenderTarget().bindWrite(false);

        RenderTarget finalTarget = effect.renderTarget;
        if (finalTarget != null) {
            blitAdditively(finalTarget);
        }

        effect.enabled = false;
    }

    /**
     * Convenience wrapper for manual GUI elements (images, sprites, etc).
     * Renders content into the effect's "input" target, then processes and blits.
     * For text bloom, use the BLOOM_OUTPUT render state + endBatch() + processAndBlit() instead.
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

    private static void blitAdditively(RenderTarget target) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        target.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static class PostEffect {
        final PostChain postChain;
        final RenderTarget renderTarget;
        boolean enabled = false;

        PostEffect(PostChain postChain, RenderTarget renderTarget) {
            this.postChain = postChain;
            this.renderTarget = renderTarget;
        }

        void close() { if (postChain != null) postChain.close(); }
        void resize(int w, int h) { if (postChain != null) postChain.resize(w, h); }
    }
}