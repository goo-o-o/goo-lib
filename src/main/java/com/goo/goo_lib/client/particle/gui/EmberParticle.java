package com.goo.goo_lib.client.particle.gui;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public class EmberParticle extends GuiParticle {
    private final float factor;
    private final boolean useCos;
    private static final float START_R  = 1.0F,     START_G = 0.85F,    START_B = 0.2F;  // Bright Yellow
    private static final float MID_R    = 0.95F,    MID_G   = 0.3F,     MID_B   = 0.05F; // Deep Orange-Red
    private static final float END_R    = 0.4F,     END_G   = 0.05F,    END_B   = 0.0F;  // Dark Crimson

    public EmberParticle(float x, float y, int z, float vx, float vy, int lifetime, float scale) {
        super(
                FastColor.ARGB32.color(255, (int)(START_R * 255), (int)(START_G * 255), (int)(START_B * 255)),
                x, y, z, vx, vy, lifetime, scale,
                START_R, START_G, START_B, 1.0F
        );

        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.factor = random.nextFloat(0.25F, 1.25F);
        this.useCos = random.nextBoolean();
        this.withRotation(
                random.nextFloat(0.0F, 360.0F),
                random.nextFloat(0.0F, 360.0F),
                random.nextFloat(0.0F, 360.0F)
        );

        this.withAngularVelocity(
                random.nextFloat(-4.0F, 4.0F),
                random.nextFloat(-4.0F, 4.0F),
                random.nextFloat(-6.0F, 6.0F)
        );
    }

    @Override
    public boolean tick() {
        if (super.tick()) {
            this.vx = useCos ? Mth.cos(this.age * 0.2F) : Mth.sin(this.age * 0.2F) * 0.5F * this.factor;

            float progress = (float) this.age / (float) this.lifetime;

            if (progress < 0.5F) {
                float t = progress * 2.0F;
                this.r = Mth.lerp(t, START_R, MID_R);
                this.g = Mth.lerp(t, START_G, MID_G);
                this.b = Mth.lerp(t, START_B, MID_B);
                this.alpha = 1.0F;
            } else {
                float t = (progress - 0.5F) * 2.0F;
                this.r = Mth.lerp(t, MID_R, END_R);
                this.g = Mth.lerp(t, MID_G, END_G);
                this.b = Mth.lerp(t, MID_B, END_B);
            }

            if (progress > 0.7F) {
                float fadeProgress = (progress - 0.7F) / 0.3F;
                this.alpha = Mth.lerp(fadeProgress, 1.0F, 0.0F);
            }

            this.solidColor = FastColor.ARGB32.color(
                    (int) (this.alpha * 255.0F),
                    (int) (this.r * 255.0F),
                    (int) (this.g * 255.0F),
                    (int) (this.b * 255.0F)
            );

            return true;
        } else {
            return false;
        }
    }
}