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
    public static void modifyEffect(LivingEntity livingEntity, @org.jetbrains.annotations.Nullable LivingEntity source, Holder<MobEffect> mobEffect,
                                    @org.jetbrains.annotations.Nullable MobEffectUtils.ModValue durationMod, @org.jetbrains.annotations.Nullable MobEffectUtils.ModValue amplifierMod,
                                    Integer limit, @org.jetbrains.annotations.Nullable Consumer<LivingEntity> ifAbsent,
                                    @org.jetbrains.annotations.Nullable Consumer<LivingEntity> ifLimit) {
        if (livingEntity.hasEffect(mobEffect)) {
            MobEffectInstance original = livingEntity.getEffect(mobEffect);
            if (original != null) {

                int newDuration = original.getDuration();
                if (durationMod != null) {
                    newDuration = durationMod.overwrite() ? durationMod.value() : newDuration + durationMod.value();
                }

                int newAmplifier = original.getAmplifier();
                if (amplifierMod != null) {
                    int value = Math.min(255, amplifierMod.value());
                    newAmplifier = amplifierMod.overwrite() ? value : newAmplifier + value;
                }

                boolean hitLimit = limit != null && newAmplifier > limit;

                if (hitLimit) {
                    if (ifLimit != null) {
                        ifLimit.accept(livingEntity);
                    }

                    if (!livingEntity.hasEffect(mobEffect)) {
                        return;
                    }

                    newAmplifier = limit;
                }

                livingEntity.addEffect(new MobEffectInstance(mobEffect, newDuration, newAmplifier,
                        original.isAmbient(), original.isVisible(), original.showIcon()), source);
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
