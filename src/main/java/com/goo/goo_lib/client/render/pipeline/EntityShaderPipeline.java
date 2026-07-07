package com.goo.goo_lib.client.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;

public abstract class EntityShaderPipeline extends ShaderPipeline {

    @Override
    public final PipelineStage getStage() {
        return PipelineStage.ENTITY;
    }

    /**
     * Called before entities render. Override to set up custom render targets.
     */
    public void onBeforeEntities(RenderTarget mainTarget, PostChain chain) {}

    public BlitMode getBlitMode() {
        return BlitMode.ADDITIVE;
    }
}
