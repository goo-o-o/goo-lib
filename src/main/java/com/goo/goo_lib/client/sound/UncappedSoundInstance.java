package com.goo.goo_lib.client.sound;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * A sound instance that can have a volume more than 1, you need to capture your original sound instance in {@link net.neoforged.neoforge.client.event.sound.PlaySoundEvent} and return an instance of this in order for it to work. This is because {@link net.minecraft.network.protocol.game.ClientboundSoundPacket} does not serialize the Object itself.
 */
public class UncappedSoundInstance extends SimpleSoundInstance {
    public UncappedSoundInstance(ResourceLocation location, SoundSource source, float volume, float pitch, RandomSource random, boolean looping, int delay, Attenuation attenuation, double x, double y, double z, boolean relative) {
        super(location, source, volume, pitch, random, looping, delay, attenuation, x, y, z, relative);
    }

    public UncappedSoundInstance(SoundEvent soundEvent, SoundSource source, float volume, float pitch, RandomSource random, BlockPos entity) {
        super(soundEvent, source, volume, pitch, random, entity);
    }

    public UncappedSoundInstance(SoundEvent soundEvent, SoundSource source, float volume, float pitch, RandomSource random, double x, double y, double z) {
        super(soundEvent, source, volume, pitch, random, x, y, z);
    }

    public static UncappedSoundInstance fromSimpleSoundInstance(SimpleSoundInstance instance) {
        return new UncappedSoundInstance(
                instance.getLocation(),
                instance.getSource(),
                instance.volume,
                instance.pitch,
                SoundInstance.createUnseededRandom(),
                instance.isLooping(),
                instance.getDelay(),
                instance.getAttenuation(),
                instance.getX(),
                instance.getY(),
                instance.getZ(),
                instance.isRelative());
    }

}
