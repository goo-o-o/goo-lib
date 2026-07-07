package com.goo.goo_lib.client.render;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.render.pipeline.ScreenPostEffectPipeline;
import com.goo.goo_lib.util.MotionBlurUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MotionBlurPipeline extends ScreenPostEffectPipeline {

    private final Matrix4f currentViewProj = new Matrix4f();
    private final Matrix4f invViewProj = new Matrix4f();
    private final Matrix4f prevViewProj = new Matrix4f();
    private final Vector3f camDelta = new Vector3f();

    private Vec3 prevCamPos = Vec3.ZERO;
    private boolean initialized = false;

    @Override
    public ResourceLocation getLocation() {
        return GLRenderTypes.MOTION_BLUR_SHADER_LOCATION;
    }

    @Override
    public boolean isEnabled() {
        return MotionBlurUtil.isEnabled();
    }

    @Override
    public BlitMode getBlitMode() {
        return BlitMode.OPAQUE;
    }

    @Override
    public void onBeforeProcess(PostChain chain, RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotate((float) Math.toRadians(camera.getXRot()), 1.0f, 0.0f, 0.0f);
        viewMatrix.rotate((float) Math.toRadians(camera.getYRot() + 180.0f), 0.0f, 1.0f, 0.0f);
        viewMatrix.translate(-(float) camera.getPosition().x, -(float) camera.getPosition().y, -(float) camera.getPosition().z);

        Matrix4f projMatrix = new Matrix4f(event.getProjectionMatrix());
        currentViewProj.set(projMatrix).mul(viewMatrix);
        currentViewProj.invert(invViewProj);

        Vec3 currentCamPos = camera.getPosition();
        if (!initialized) {
            prevViewProj.set(currentViewProj);
            prevCamPos = currentCamPos;
            initialized = true;
        }

        camDelta.set(
                (float) (currentCamPos.x - prevCamPos.x),
                (float) (currentCamPos.y - prevCamPos.y),
                (float) (currentCamPos.z - prevCamPos.z)
        );

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        chain.passes.forEach(pass -> {
            var effect = pass.getEffect();
            if (effect.getUniform("InvViewProjMat") != null) effect.getUniform("InvViewProjMat").set(invViewProj);
            if (effect.getUniform("PrevViewProjMat") != null) effect.getUniform("PrevViewProjMat").set(prevViewProj);
            if (effect.getUniform("CameraPosDelta") != null) effect.getUniform("CameraPosDelta").set(camDelta);
            if (effect.getUniform("InSize") != null) effect.getUniform("InSize").set((float) width, (float) height);
        });

        prevViewProj.set(currentViewProj);
        prevCamPos = currentCamPos;
    }

}
