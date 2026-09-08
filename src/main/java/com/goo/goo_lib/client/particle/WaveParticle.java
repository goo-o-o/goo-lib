package com.goo.goo_lib.client.particle;

import com.goo.goo_lib.util.Easing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaveParticle extends FlatParticle {
    private final Easing easing;
    private final float finalSize;

    public WaveParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float radius, float pitch, float yaw, float roll, int growthDuration, Easing easing) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, radius, pitch, yaw, roll);
        setLifetime(growthDuration);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.easing = easing;
        this.finalSize = radius;
        this.quadSize = 0;
        this.oQuadSize = 0;
    }


    @Override
    public void tick() {
        super.tick();

        float progress = (float) age / lifetime;
        progress = easing.ease(progress);
        quadSize = finalSize * progress;

    }

    public static class Provider implements ParticleProvider<WaveParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(WaveParticleOption data, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            WaveParticle waveParticle = new WaveParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, data.radius(), data.rotX(), data.rotY(), data.rotZ(), data.growthDuration(), data.easing());
            waveParticle.pickSprite(this.sprites);
            return waveParticle;
        }
    }
}
