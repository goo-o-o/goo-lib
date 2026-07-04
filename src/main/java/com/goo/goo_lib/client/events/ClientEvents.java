package com.goo.goo_lib.client.events;

import com.goo.goo_lib.client.particle.ComponentParticle;
import com.goo.goo_lib.client.particle.gui.GuiParticleSystem;
import com.goo.goo_lib.client.registry.GLParticles;
import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.registry.PostEffectRegistry;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        GuiParticleSystem.getInstance().tick();
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(GLParticles.COMPONENT_PARTICLE.get(), new ComponentParticle.Provider());
    }

    // ── Bloom: clear input at start of each GUI frame ──────────────────────
    // Prevents stale bloom from previous frames accumulating
    @SubscribeEvent
    public static void onPreRenderGui(RenderGuiEvent.Pre event) {
        PostEffectRegistry.clearTarget(GLRenderTypes.BLOOM_SHADER_LOCATION, "input");
    }

    // ── Bloom: flush + process + blit after HUD renders ───────────────────
    // Only fires when no screen is open (hotbar, health, etc.)
    @SubscribeEvent
    public static void onPostRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen != null) return;
        flushAndBlitBloom();
    }

    // ── Bloom: flush + process + blit after screen renders ────────────────
    // Handles inventory, tooltips, custom screens — bloom renders on top
    @SubscribeEvent
    public static void onPostRenderScreen(ScreenEvent.Render.Post event) {
        if (event.getScreen().isPauseScreen()) return;

        GuiParticleSystem.getInstance().render(
                event.getGuiGraphics(),
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));

        flushAndBlitBloom();
    }

    // ── Shared logic ───────────────────────────────────────────────────────

    private static void flushAndBlitBloom() {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Flush pending bloom vertex data into the "input" target
        bufferSource.endBatch();

        // Process chain and blit "final" additively over screen
        PostEffectRegistry.renderEffectForNextTick(GLRenderTypes.BLOOM_SHADER_LOCATION);
        PostEffectRegistry.processAndBlit(GLRenderTypes.BLOOM_SHADER_LOCATION);
    }
}