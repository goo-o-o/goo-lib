package com.goo.goo_lib.utils;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MobEffectUtils {


    public record ModValue(Integer value, boolean overwrite) {
    }

    /**
     * @param livingEntity The {@link LivingEntity} to add effects to
     * @param mobEffect    The {@link MobEffect} to add
     * @param durationMod  A small {@link ModValue} record to pass the duration modifier as well as whether it should overwrite the original instance, the duration will be incremented if overwrite = false
     * @param amplifierMod A small {@link ModValue} record to pass the amplifier modifier as well as whether it should overwrite the original instance, the amplifier will be incremented if overwrite = false
     * @param ifAbsent     The code to run on the {@link LivingEntity} if the effect is absent
     * @param limit        The maximum amplifier of the {@link MobEffect}, inclusive
     * @param ifLimit      The code to run on the {@link LivingEntity} if the limit is reached
     */
    public static void modifyEffect(LivingEntity livingEntity, Holder<MobEffect> mobEffect, @Nullable ModValue durationMod, @Nullable ModValue amplifierMod, Integer limit, @Nullable Consumer<LivingEntity> ifAbsent, @Nullable Consumer<LivingEntity> ifLimit) {
        if (livingEntity.hasEffect(mobEffect)) {
            MobEffectInstance original = livingEntity.getEffect(mobEffect);
            if (original != null) {
                if (limit != null && ifLimit != null) {
                    if (original.getAmplifier() >= limit) {
                        ifLimit.accept(livingEntity);
                    }
                    return;
                }

                int newDuration = original.getDuration();
                int newAmplifier = original.getAmplifier();

                if (durationMod != null)
                    if (durationMod.overwrite()) {
                        newDuration = durationMod.value;
                    } else {
                        newDuration += durationMod.value;
                    }

                if (amplifierMod != null) {
                    int value = Math.min(255, amplifierMod.value()); // just in case
                    if (amplifierMod.overwrite()) {
                        newAmplifier = value;
                    } else {
                        newAmplifier += value;
                    }
                }

                livingEntity.addEffect(new MobEffectInstance(mobEffect, newDuration, newAmplifier, original.isAmbient(), original.isVisible(), original.showIcon()));
            }
        } else if (ifAbsent != null) {
            ifAbsent.accept(livingEntity);
        }
    }

    @Nullable
    public static Entity getEffectSource(Level level, MobEffectInstance effectInstance) {
        Integer sourceID = ((MixinInterfaces.MobEffectInstanceSourceAccessor) effectInstance).gl$getSourceId();
        if (sourceID != null) {
            return level.getEntity(sourceID);
        }
        return null;
    }

    public static void setEffectSource(MobEffectInstance effectInstance, Entity source) {
        ((MixinInterfaces.MobEffectInstanceSourceAccessor) effectInstance).gl$setSourceId(source.getId());
    }
}
