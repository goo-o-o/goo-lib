package com.goo.goo_lib.common.mob_effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Small class that allows a hook for when the effect expires
 */
public class ExpirableEffect extends MobEffect {
    public ExpirableEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public ExpirableEffect(MobEffectCategory category, int color, ParticleOptions particle) {
        super(category, color, particle);
    }

    public void onEffectExpired(LivingEntity livingEntity, MobEffectInstance instance) {

    }

    public void onEffectRemoved(LivingEntity livingEntity, MobEffectInstance instance) {

    }
}
