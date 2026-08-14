package com.goo.goo_lib.util.screenshake;

import com.goo.goo_lib.util.Easing;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@Getter
@Builder(builderClassName = "ShakeInstanceBuilder", toBuilder = true)
@OnlyIn(Dist.CLIENT)
public class ShakeInstance {
    @Builder.Default public final String identifier = "main";
    @Setter
    private int ticksElapsed;
    @Builder.Default public float speed = 1.0F;
    @Builder.Default public int durationTicks = 20;
    @Builder.Default public int fadeInTicks = 10; // lombok requires literal constants or explicit values here
    @Builder.Default public int fadeOutTicks = 10;
    @Builder.Default public Easing fadeInCurve = Easing.EASE_LINEAR;
    @Builder.Default public Easing fadeOutCurve = Easing.EASE_LINEAR;
    @Builder.Default public boolean motionBlur = true;

    @Builder.Default public float maxX = 0.3F;
    @Builder.Default public float maxY = 0.3F;
    @Builder.Default public float maxPitch = 2.0F;
    @Builder.Default public float maxYaw = 2.0F;
    @Builder.Default public float maxRoll = 1.5F;

    @Builder.Default public Vec3 sourcePos = null;
    @Builder.Default public double radius = 0.0;

    @Builder.Default public long startTime = System.currentTimeMillis();

    public boolean tick() {
        this.ticksElapsed++;
        return isOver();
    }



    private boolean isOver() {
        return ticksElapsed >= durationTicks;
    }

    public CalculatedOffsets getOffsets(float partialTicks) {
        if (isOver()) return CalculatedOffsets.ZERO;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return CalculatedOffsets.ZERO;

        float traumaFactor = 1.0F;

        if (ticksElapsed < fadeInTicks) {
            float progress = (float) ticksElapsed / fadeInTicks;
            traumaFactor = fadeInCurve.ease(progress);
        } else if (ticksElapsed > (durationTicks - fadeOutTicks)) {
            int ticksIntoFadeOut = ticksElapsed - (durationTicks - fadeOutTicks);
            float progress = (float) ticksIntoFadeOut / fadeOutTicks;
            traumaFactor = 1 - fadeOutCurve.ease(progress);
        }

        float shakeFactor = traumaFactor * traumaFactor;

        if (this.sourcePos != null && this.radius > 0) {
            double distance = mc.player.position().distanceTo(this.sourcePos);
            if (distance >= this.radius) return CalculatedOffsets.ZERO;
            shakeFactor *= (float) (1.0 - (distance / this.radius));
        }

        if (shakeFactor <= 0.0F) return CalculatedOffsets.ZERO;

        float time = ((System.currentTimeMillis() - this.startTime) * 0.05F * this.speed) + partialTicks;

        float noiseX = Mth.sin(time * 0.7F) * 0.6F + Mth.sin(time * 1.5F) * 0.4F;
        float noiseY = Mth.cos(time * 0.8F) * 0.6F + Mth.sin(time * 1.3F) * 0.4F;
        float noisePitch = Mth.cos(time * 0.6F) * 0.6F + Mth.sin(time * 1.4F) * 0.4F;
        float noiseYaw = Mth.sin(time * 0.9F) * 0.6F + Mth.cos(time * 1.6F) * 0.4F;
        float noiseRoll = Mth.cos(time * 1.1F) * 0.6F + Mth.sin(time * 1.8F) * 0.4F;

        return new CalculatedOffsets(
                noiseX * this.maxX * shakeFactor,
                noiseY * this.maxY * shakeFactor,
                noisePitch * this.maxPitch * shakeFactor,
                noiseYaw * this.maxYaw * shakeFactor,
                noiseRoll * this.maxRoll * shakeFactor
        );
    }

    // ─── CUSTOM BUILDER EXTENSIONS ──────────────────────────────────────────
    public static class ShakeInstanceBuilder {
        // shortcut method for modifying bounds at once
        public ShakeInstanceBuilder bounds(float x, float y) {
            this.maxX(x);
            this.maxY(y);
            return this;
        }

        // shortcut method for setting all rotation vectors at once
        public ShakeInstanceBuilder rotation(float pitch, float yaw, float roll) {
            this.maxPitch(pitch);
            this.maxYaw(yaw);
            this.maxRoll(roll);
            return this;
        }

        // shortcut method for assigning spatial context fields at once
        public ShakeInstanceBuilder position(Vec3 pos, double radius) {
            this.sourcePos(pos);
            this.radius(radius);
            return this;
        }
    }

    public record CalculatedOffsets(float x, float y, float pitch, float yaw, float roll) {
        public static final CalculatedOffsets ZERO = new CalculatedOffsets(0, 0, 0, 0, 0);
    }
}