package com.goo.goo_lib.mixin;

import com.goo.goo_lib.utils.MixinInterfaces;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injected into {@link MobEffectInstance} to track the source of an effect.
 */
@Mixin(MobEffectInstance.class)
public class MobEffectInstanceMixin implements MixinInterfaces.MobEffectInstanceSourceAccessor {

    @Unique
    private Integer goo_lib$sourceId;

    /**
     * Target the primary master constructor in 1.21.1.
     * All secondary constructors (and data component/NBT loaders) route through this one.
     */
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;IIZZZLnet/minecraft/world/effect/MobEffectInstance;)V",
            at = @At("RETURN"))
    private void init(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, MobEffectInstance hiddenEffect, CallbackInfo ci) {
        this.goo_lib$sourceId = null;
    }

    /**
     * Saves the SourceID to NBT when the effect instance is exported.
     */
    @Inject(method = "save", at = @At("RETURN"))
    private void brutality$saveSource(CallbackInfoReturnable<Tag> cir) {
        if (this.goo_lib$sourceId != null) {

            Tag vanillaTag = cir.getReturnValue();

            if (vanillaTag instanceof CompoundTag compoundTag) {

                compoundTag.putInt("goo_lib:source_id", this.goo_lib$sourceId);
            }
        }
    }

    /**
     * Replaces the old 'loadSpecifiedEffect' logic.
     * 1.21.1 uses a static 'load' method that returns a nullable MobEffectInstance.
     */
    @Inject(method = "load", at = @At("RETURN"))
    private static void brutality$loadSource(CompoundTag tag, CallbackInfoReturnable<MobEffectInstance> cir) {
        MobEffectInstance instance = cir.getReturnValue();
        if (instance != null && tag.contains("source_id")) {
            if (instance instanceof MixinInterfaces.MobEffectInstanceSourceAccessor accessor) {
                accessor.goo_lib$setSourceId(tag.getInt("goo_lib:source_id"));
            }
        }
    }

    @Override
    public Integer goo_lib$getSourceId() {
        return this.goo_lib$sourceId;
    }

    @Override
    public void goo_lib$setSourceId(int id) {
        this.goo_lib$sourceId = id;
    }
}