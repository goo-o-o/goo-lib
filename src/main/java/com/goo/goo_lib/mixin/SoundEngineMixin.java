package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.sound.UncappedSoundInstance;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    private native float getVolume(SoundSource category);

    @ModifyExpressionValue(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"
            )
    )
    private float uncapTaggedSounds(float vanillaClampedVolume, SoundInstance soundInstance) {
        if (soundInstance instanceof UncappedSoundInstance) {
            float rawCalculated = soundInstance.getVolume() * this.getVolume(soundInstance.getSource());
            return Math.max(rawCalculated, 0.0F);
        }
        return vanillaClampedVolume;
    }
}