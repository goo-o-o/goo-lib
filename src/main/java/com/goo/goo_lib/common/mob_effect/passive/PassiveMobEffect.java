package com.goo.goo_lib.common.mob_effect.passive;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Record to hold a "Passive Effect" instance for giving Curio's infinite mob effects
 */
public record PassiveMobEffect(
        Holder<MobEffect> effect,
        int amplifier,
        Predicate<LivingEntity> condition,
        int checkInterval // in ticks
) {
    // in the future maybe add a curve function for the amplifier, will have to change up registry and engine logic to detect changes though
    public static PassiveMobEffect unconditional(Holder<MobEffect> effect, int amplifier) {
        return new PassiveMobEffect(effect, amplifier, e -> true, 1);
    }
    public static PassiveMobEffect conditional(Holder<MobEffect> effect, int amplifier, Predicate<LivingEntity> predicate) {
        return new PassiveMobEffect(effect, amplifier, predicate, 1);
    }
    public static PassiveMobEffect unconditional(Holder<MobEffect> effect, int amplifier, int checkInterval) {
        return new PassiveMobEffect(effect, amplifier, e -> true, checkInterval);
    }
    public static PassiveMobEffect conditional(Holder<MobEffect> effect, int amplifier, Predicate<LivingEntity> predicate, int checkInterval) {
        return new PassiveMobEffect(effect, amplifier, predicate, checkInterval);
    }

}
