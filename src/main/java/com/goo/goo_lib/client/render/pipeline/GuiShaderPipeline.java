package com.goo.goo_lib.client.render.pipeline;

import net.minecraft.client.renderer.PostChain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class GuiShaderPipeline extends ShaderPipeline {

    @Override
    public final PipelineStage getStage() {
        return PipelineStage.GUI;
    }

    /**
     * Name of the intermediate target to clear before the GUI frame renders.
     * Return null to skip clearing.
     */
    @Nullable
    public String getInputTargetName() {
        return "input";
    }

    /**
     * Called before onBeforeProcess. Override to flush any accumulated vertex buffers
     * that feed this pipeline's input target (e.g. a dedicated MultiBufferSource).
     */
    public void flushBuffers() {}

    /**
     * Called before the post chain processes. Override to push per-frame uniforms.
     */
    public void onBeforeProcess(PostChain chain) {}

    public BlitMode getBlitMode() {
        return BlitMode.ADDITIVE;
    }
}
