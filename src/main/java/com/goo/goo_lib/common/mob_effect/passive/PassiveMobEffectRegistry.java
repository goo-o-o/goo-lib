package com.goo.goo_lib.common.mob_effect.passive;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PassiveMobEffectRegistry {

    // MobEffect -> Map<PassiveMobEffect, ActiveStateData>
    private final Map<Holder<MobEffect>, Map<PassiveMobEffect, Integer>> activePassives = new HashMap<>();

    /**
     * Updates state for a specific passive instance.
     * Returns true ONLY if this specific passive actually flipped its active state.
     */
    public boolean setPassiveState(LivingEntity entity, PassiveMobEffect passive, boolean active) {
        Holder<MobEffect> effect = passive.effect();
        Map<PassiveMobEffect, Integer> sources = activePassives.computeIfAbsent(effect, k -> new HashMap<>());

        boolean wasActive = sources.containsKey(passive);

        if (wasActive == active) {
            return false; // state didn't change for this specific passive
        }

        if (active) {
            sources.put(passive, passive.amplifier());
        } else {
            sources.remove(passive);
        }

        recalculate(entity, effect);

        if (sources.isEmpty()) {
            activePassives.remove(effect);
        }

        return true; // state flipped
    }

    private void recalculate(LivingEntity entity, Holder<MobEffect> effect) {
        Map<PassiveMobEffect, Integer> sources = activePassives.get(effect);

        if (sources == null || sources.isEmpty()) {
            MobEffectInstance current = entity.getEffect(effect);
            if (current != null && current.isInfiniteDuration()) {
                entity.removeEffect(effect);
            }
            return;
        }

        int maxAmplifier = Collections.max(sources.values());
        MobEffectInstance current = entity.getEffect(effect);

        if (current == null || current.getAmplifier() != maxAmplifier) {
            // remove the higher amplifier so that when we apply a lower amplifier it actually works
            if (current != null) {
                entity.removeEffect(effect);
            }

            entity.addEffect(new MobEffectInstance(
                    effect,
                    MobEffectInstance.INFINITE_DURATION,
                    maxAmplifier,
                    true,  // ambient
                    false, // particles
                    true   // icon
            ));
        }
    }

    public boolean isEmpty() {
        return activePassives.isEmpty();
    }
}