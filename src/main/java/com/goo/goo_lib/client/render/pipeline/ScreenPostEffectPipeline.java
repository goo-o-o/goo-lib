package com.goo.goo_lib.client.render.pipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public abstract class ScreenPostEffectPipeline extends ShaderPipeline {

    @Override
    public final PipelineStage getStage() {
        return PipelineStage.SCREEN;
    }

    /**
     * Called before the post chain processes. Override to push per-frame uniforms.
     */
    public void onBeforeProcess(PostChain chain, RenderLevelStageEvent event) {}

    /**
     * Additional frame-readiness check beyond isEnabled().
     * Default guards against processing during pause or without a loaded level.
     */
    public boolean isReadyThisFrame(Minecraft mc) {
        return mc.level != null && !mc.isPaused();
    }

    public BlitMode getBlitMode() {
        return BlitMode.OPAQUE;
    }
}
