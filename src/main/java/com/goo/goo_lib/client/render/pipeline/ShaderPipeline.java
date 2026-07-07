package com.goo.goo_lib.client.render.pipeline;

import net.minecraft.resources.ResourceLocation;

public abstract class ShaderPipeline {

    public abstract ResourceLocation getLocation();

    public abstract PipelineStage getStage();

    public boolean isEnabled() {
        return true;
    }

    public enum PipelineStage {
        GUI, ENTITY, SCREEN
    }

    public enum BlitMode {
        ADDITIVE, OPAQUE
    }
}
