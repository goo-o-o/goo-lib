package com.goo.goo_lib.client.registry;

import com.goo.goo_lib.common.GooLib;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class GLShaders {
    private static ShaderInstance sparksOverlayShader;
    private static ShaderInstance textBloomShader;
    private static ShaderInstance textFireShader;
    private static ShaderInstance textAbyssalShader;
    private static AbstractUniform gameTimeUniform;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_text_bloom"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> textBloomShader = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_text_fire"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> {
                    textFireShader = shader;
                    gameTimeUniform = shader.safeGetUniform("GameTime");
                }
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), GooLib.loc("rendertype_text_foggy"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> {
                    textAbyssalShader = shader;
                    gameTimeUniform = shader.safeGetUniform("GameTime");
                });
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), GooLib.loc("sparks_overlay"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shader -> {
                    sparksOverlayShader = shader;
                    sparksOverlayShader.setSampler("NoiseSampler", 1);
                    gameTimeUniform = shader.safeGetUniform("GameTime");
                });
    }

    public static void updateGameTime(float time) {
        if (gameTimeUniform != null) {
            gameTimeUniform.set(time);
        }
    }

    public static ShaderInstance getSparksOverlayShader() {
        return sparksOverlayShader;
    }

    public static ShaderInstance getTextBloomShader() { return textBloomShader; }

    public static ShaderInstance getTextFireShader() {
        return textFireShader;
    }

    public static ShaderInstance getTextAbyssalShader() {
        return textAbyssalShader;
    }
}
