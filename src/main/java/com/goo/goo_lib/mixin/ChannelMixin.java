package com.goo.goo_lib.mixin;

import com.mojang.blaze3d.audio.Channel;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public class ChannelMixin {

    @Shadow @Final private int source;

    /**
     * Sets the maximum gain of the OpenAL engine to match the maximum volume of the sound coming in automatically.
     */
    @Inject(method = "setVolume(F)V", at = @At("HEAD"))
    private void uncapOpenAlMaxGain(float volume, CallbackInfo ci) {
        AL10.alSourcef(this.source, AL10.AL_MAX_GAIN, Math.max(volume, 1.0F));
    }
}