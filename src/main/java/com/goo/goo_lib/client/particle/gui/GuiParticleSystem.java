package com.goo.goo_lib.client.particle.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiParticleSystem {

    private static final GuiParticleSystem INSTANCE = new GuiParticleSystem();

    public static GuiParticleSystem getInstance() {
        return INSTANCE;
    }

    private static final int DEFAULT_MAX = 2048;
    private final List<GuiParticle> particles = new ArrayList<>();
    private final List<GuiParticle> pendingAdditions = new ArrayList<>();
    private final List<GuiParticle> pendingRemovals = new ArrayList<>();


    private final int maxParticles;

    public GuiParticleSystem() {
        this.maxParticles = DEFAULT_MAX;
    }

    public GuiParticleSystem(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    // ── Control ───────────────────────────────────────────────────────────────
    public GuiParticle add(GuiParticle p) {
        if (particles.size() >= maxParticles) return null;
        pendingAdditions.add(p);
        return p;
    }

    public void tick() {
        particles.removeIf(p -> !p.tick());

        if (!pendingAdditions.isEmpty()) {
            particles.addAll(pendingAdditions);
            pendingAdditions.clear();
        }
        if (!pendingRemovals.isEmpty()) {
            particles.removeAll(pendingRemovals);
            pendingRemovals.clear();
        }
    }

    public void render(GuiGraphics graphics, float partialTick) {
        particles.forEach(p -> p.render(graphics, partialTick));
    }

    public boolean remove(GuiParticle particle) {
        return pendingRemovals.add(particle);
    }

    public void clear() {
        particles.clear();
    }

    public int size() {
        return particles.size();
    }

    // ── Emit: particle type (animated atlas) ──────────────────────────────────
    public GuiParticle emit(ParticleType<?> type,
                            float x, float y, int z, float vx, float vy, int lifetime, float scale,
                            float r, float g, float b, float alpha) {
        return add(new GuiParticle(type, x, y, z, vx, vy, lifetime, scale, r, g, b, alpha));
    }

    public GuiParticle emit(ParticleType<?> type,
                            float x, float y, float vx, float vy, int lifetime, float scale) {
        return emit(type, x, y, 401, vx, vy, lifetime, scale, 1F, 1F, 1F, 1F);
    }

    // ── Emit: static texture ──────────────────────────────────────────────────
    public GuiParticle emit(ResourceLocation texture,
                            float x, float y, int z, float vx, float vy, int lifetime, float scale,
                            float r, float g, float b, float alpha) {
        return add(new GuiParticle(texture, x, y, z, vx, vy, lifetime, scale, r, g, b, alpha));
    }

    public GuiParticle emit(ResourceLocation texture,
                            float x, float y, float vx, float vy, int lifetime, float scale) {
        return emit(texture, x, y, 401, vx, vy, lifetime, scale, 1F, 1F, 1F, 1F);
    }

    // ── Emit: solid color ─────────────────────────────────────────────────────
    public GuiParticle emit(int color,
                            float x, float y, int z, float vx, float vy, int lifetime, float scale) {
        return add(new GuiParticle(color, x, y, z, vx, vy, lifetime, scale, 1f, 1f, 1f, 1f));
    }

    public GuiParticle emit(int color,
                            float x, float y, float vx, float vy, int lifetime, float scale) {
        return emit(color, x, y, 401, vx, vy, lifetime, scale);
    }

    // ── Burst: particle type ──────────────────────────────────────────────────
    public void burst(ParticleType<?> type,
                      float x, float y, int z, int count, int lifetime, float scale,
                      float r, float g, float b, float alpha,
                      float speedMin, float speedMax) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = speedMin + (float) Math.random() * (speedMax - speedMin);
            int lt = lifetime + (int) (Math.random() * (lifetime / 4f));
            emit(type, x, y, z, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed, lt, scale, r, g, b, alpha);
        }
    }

    public void burst(ParticleType<?> type,
                      float x, float y, int count, int lifetime, float scale,
                      float r, float g, float b, float alpha,
                      float speedMin, float speedMax) {
        burst(type, x, y, 401, count, lifetime, scale, r, g, b, alpha, speedMin, speedMax);
    }

    // ── Burst: solid color ────────────────────────────────────────────────────
    public void burst(int color,
                      float x, float y, int z, int count, int lifetime, float scale,
                      float speedMin, float speedMax) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = speedMin + (float) Math.random() * (speedMax - speedMin);
            int lt = lifetime + (int) (Math.random() * (lifetime / 4f));
            emit(color, x, y, z, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed, lt, scale);
        }
    }

    public void burst(int color,
                      float x, float y, int count, int lifetime, float scale,
                      float speedMin, float speedMax) {
        burst(color, x, y, 401, count, lifetime, scale, speedMin, speedMax);
    }

    // ── Burst: static texture ─────────────────────────────────────────────────
    public void burst(ResourceLocation texture,
                      float x, float y, int z, int count, int lifetime, float scale,
                      float r, float g, float b, float alpha,
                      float speedMin, float speedMax) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = speedMin + (float) Math.random() * (speedMax - speedMin);
            int lt = lifetime + (int) (Math.random() * (lifetime / 4f));
            emit(texture, x, y, z, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed, lt, scale, r, g, b, alpha);
        }
    }

    public void burst(ResourceLocation texture,
                      float x, float y, int count, int lifetime, float scale,
                      float r, float g, float b, float alpha,
                      float speedMin, float speedMax) {
        burst(texture, x, y, 401, count, lifetime, scale, r, g, b, alpha, speedMin, speedMax);
    }

    public static @Nullable SpriteSet getSprites(ParticleType<?> type) {
        ResourceLocation key = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
        if (key == null) return null;
        return Minecraft.getInstance().particleEngine.spriteSets.get(key);
    }
}