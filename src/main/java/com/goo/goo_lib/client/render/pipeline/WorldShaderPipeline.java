package com.goo.goo_lib.client.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public abstract class WorldShaderPipeline extends ShaderPipeline {

    @Override
    public final PipelineStage getStage() {
        return PipelineStage.WORLD;
    }

    /**
     * Called at the start of the level render frame before any world objects draw.
     * Override to set up custom render targets or uniforms.
     */
    public void onRenderStart(RenderTarget mainTarget, PostChain chain) {}

    public BlitMode getBlitMode() {
        return BlitMode.ADDITIVE;
    }

    public boolean shouldBlitForStage(RenderLevelStageEvent.Stage stage) {
        return stage == RenderLevelStageEvent.Stage.AFTER_LEVEL;
    }

    public boolean shouldProcessForStage(RenderLevelStageEvent.Stage stage) {
        return stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES;
    }

}