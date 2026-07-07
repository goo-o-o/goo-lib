package com.goo.goo_lib.client.events;

import com.goo.goo_lib.client.particle.ComponentParticle;
import com.goo.goo_lib.client.particle.gui.GuiParticleSystem;
import com.goo.goo_lib.client.registry.GLParticles;
import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.registry.PostEffectRegistry;
import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.screenshake.ScreenShakeUtil;
import com.goo.goo_lib.util.screenshake.ShakeInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            ShakeInstance.CalculatedOffsets offsets = ScreenShakeUtil.getCompositeOffsets(event.getPartialTick().getGameTimeDeltaTicks());

            if (offsets != ShakeInstance.CalculatedOffsets.ZERO) {
                PoseStack poseStack = event.getPoseStack();

                // apply positional jitter translation directly to the world rendering stack
                poseStack.translate(offsets.x(), offsets.y(), 0.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        ShakeInstance.CalculatedOffsets offsets = ScreenShakeUtil.getCompositeOffsets((float) event.getPartialTick());
        event.setPitch(event.getPitch() + offsets.pitch());
        event.setYaw(event.getYaw() + offsets.yaw());
        event.setRoll(event.getRoll() + offsets.roll());
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                GLRenderTypes.clearCaches();
            }
        });
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ScreenShakeUtil.clientTick();
        GuiParticleSystem.getInstance().tick();
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(GLParticles.COMPONENT_PARTICLE.get(), new ComponentParticle.Provider());
    }

    // ── GUI pipelines: clear input targets at start of each frame ─────────
    @SubscribeEvent
    public static void onPreRenderGui(RenderGuiEvent.Pre event) {
        PostEffectRegistry.dispatchPreGui();
    }

    // ── GUI pipelines: process + blit after HUD renders ──────────────────
    // Only fires when no screen is open (hotbar, health, etc.)
    @SubscribeEvent
    public static void onPostRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen != null) return;
        PostEffectRegistry.dispatchPostGui();
    }

    // ── GUI pipelines: process + blit after screen renders ───────────────
    // Handles inventory, tooltips, custom screens
    @SubscribeEvent
    public static void onPostRenderScreen(ScreenEvent.Render.Post event) {
        if (event.getScreen().isPauseScreen()) return;

        GuiParticleSystem.getInstance().render(
                event.getGuiGraphics(),
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));

        PostEffectRegistry.dispatchPostGui();
    }


}