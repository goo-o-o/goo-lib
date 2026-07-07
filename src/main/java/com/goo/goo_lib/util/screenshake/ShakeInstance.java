package com.goo.goo_lib.util.screenshake;

import com.goo.goo_lib.util.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ShakeInstance {
    private int ticksElapsed = 0;
    private final float speed;
    private final int durationTicks;
    private final int fadeInTicks;
    private final int fadeOutTicks;
    private final Easing fadeInCurve;
    private final Easing fadeOutCurve;
    public final boolean motionBlur;

    // customizable bounding limits
    private final float maxX;
    private final float maxY;
    private final float maxPitch;
    private final float maxYaw;
    private final float maxRoll;

    // positional properties
    private final Vec3 sourcePos;
    private final double radius;

    private final long startTime;

    public ShakeInstance(Builder builder) {
        this.durationTicks = builder.durationTicks;
        this.fadeInTicks = builder.fadeInTicks;
        this.fadeOutTicks = builder.fadeOutTicks;
        this.fadeInCurve = builder.fadeInCurve;
        this.fadeOutCurve = builder.fadeOutCurve;
        this.speed = builder.speed;
        this.maxX = builder.maxX;
        this.maxY = builder.maxY;
        this.maxPitch = builder.maxPitch;
        this.maxYaw = builder.maxYaw;
        this.maxRoll = builder.maxRoll;
        this.sourcePos = builder.sourcePos;
        this.radius = builder.radius;
        this.motionBlur = builder.motionBlur;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Ticks the shake instance.
     *
     * @return true if the shake lifecycle has expired.
     */
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


        // middle factor
        float traumaFactor = 1.0F;

        if (ticksElapsed < fadeInTicks) {
            float progress = (float) ticksElapsed / fadeInTicks;
            traumaFactor = fadeInCurve.ease(progress);
        } else if (ticksElapsed > (durationTicks - fadeOutTicks)) {
            int ticksIntoFadeOut = ticksElapsed - (durationTicks - fadeOutTicks);
            float progress = (float) ticksIntoFadeOut / fadeOutTicks;
            traumaFactor = 1 - fadeOutCurve.ease(progress);
        }

        // non-linear intensity curve applied to remaining trauma and dynamic distance
        float shakeFactor = traumaFactor * traumaFactor;

        if (this.sourcePos != null && this.radius > 0) {
            double distance = mc.player.position().distanceTo(this.sourcePos);
            if (distance >= this.radius) return CalculatedOffsets.ZERO;
            shakeFactor *= (float) (1.0 - (distance / this.radius));
        }

        if (shakeFactor <= 0.0F) return CalculatedOffsets.ZERO;

        float time = ((System.currentTimeMillis() - this.startTime) * 0.05F * this.speed) + partialTicks;

        // generate multi-frequency harmonic tracks
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

    // ─── API BUILDER PATTERN ────────────────────────────────────────────────
    public static class Builder {
        private boolean motionBlur = true;
        private int durationTicks = 20;
        private Easing fadeInCurve = Easing.EASE_LINEAR;
        private Easing fadeOutCurve = Easing.EASE_LINEAR;
        private int fadeInTicks = durationTicks / 2;
        private int fadeOutTicks = durationTicks / 2;
        private float speed = 1.0F;
        private float maxX = 0.3F;
        private float maxY = 0.3F;
        private float maxPitch = 2.0F;
        private float maxYaw = 2.0F;
        private float maxRoll = 1.5F;
        private Vec3 sourcePos = null;
        private double radius = 0.0;

        public Builder duration(int ticks) {
            this.durationTicks = ticks;
            return this;
        }
        public Builder motionBlur(boolean blur) { this.motionBlur = blur; return this; }
        public Builder easeIn(Easing type) { this.fadeInCurve = type; return this; }
        public Builder easeIn(Easing type, int fadeInTicks) {
            this.fadeInCurve = type;
            this.fadeInTicks = fadeInTicks;
            return this;
        }

        public Builder easeOut(Easing type) { this.fadeOutCurve = type; return this; }
        public Builder easeOut(Easing type, int fadeOutTicks) {
            this.fadeOutCurve = type;
            this.fadeOutTicks = fadeOutTicks;
            return this;
        }

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder bounds(float x, float y) {
            this.maxX = x;
            this.maxY = y;
            return this;
        }

        public Builder rotation(float pitch, float yaw, float roll) {
            this.maxPitch = pitch;
            this.maxYaw = yaw;
            this.maxRoll = roll;
            return this;
        }

        public Builder position(Vec3 pos, double radius) {
            this.sourcePos = pos;
            this.radius = radius;
            return this;
        }

        public ShakeInstance build() {
            return new ShakeInstance(this);
        }
    }

    public record CalculatedOffsets(float x, float y, float pitch, float yaw, float roll) {
        public static final CalculatedOffsets ZERO = new CalculatedOffsets(0, 0, 0, 0, 0);
    }

}