package com.goo.goo_lib.client.render.pipeline;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public abstract class ShaderPipeline {

    public abstract ResourceLocation getLocation();

    public abstract PipelineStage getStage();

    public boolean isEnabled() {
        return true;
    }

    public enum PipelineStage {
        GUI, ENTITY, SCREEN;

        public String suffix() {
            return "_" + this.name().toLowerCase(Locale.ROOT);
        }

    }

    public enum BlitMode {
        ADDITIVE, OPAQUE
    }
}
