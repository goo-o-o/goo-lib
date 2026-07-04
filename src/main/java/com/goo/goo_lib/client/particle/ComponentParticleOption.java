package com.goo.goo_lib.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class ComponentParticleOption implements ParticleOptions {
    private final ParticleType<ComponentParticleOption> type;
    private final Component component;
    private final int backgroundColor;
    private final boolean dropShadow;

    // Network synchronization
    public static MapCodec<ComponentParticleOption> codec(ParticleType<ComponentParticleOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("component").forGetter(ComponentParticleOption::getComponent),
                ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("background_color", 0x00000000).forGetter(ComponentParticleOption::getBackgroundColor),
                Codec.BOOL.optionalFieldOf("drop_shadow", true).forGetter(ComponentParticleOption::hasDropShadow)
        ).apply(instance, (component, backgroundColor, dropShadow) ->
                new ComponentParticleOption(particleType, component, backgroundColor, dropShadow)));
    }

    // Packet serialization
    public static StreamCodec<RegistryFriendlyByteBuf, ComponentParticleOption> streamCodec(ParticleType<ComponentParticleOption> type) {
        return StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, ComponentParticleOption::getComponent,
                ByteBufCodecs.INT, ComponentParticleOption::getBackgroundColor,
                ByteBufCodecs.BOOL, ComponentParticleOption::hasDropShadow,
                (component, backgroundColor, dropShadow) ->
                        new ComponentParticleOption(type, component, backgroundColor, dropShadow)
        );
    }

    private ComponentParticleOption(ParticleType<ComponentParticleOption> type, Component component, int backgroundColor, boolean dropShadow) {
        this.type = type;
        this.component = component;
        this.backgroundColor = backgroundColor;
        this.dropShadow = dropShadow;
    }

    @Override
    public @NotNull ParticleType<ComponentParticleOption> getType() {
        return this.type;
    }

    public Component getComponent() {
        return this.component;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public boolean hasDropShadow() {
        return this.dropShadow;
    }

    // Color helper methods for the background
    public float getBackgroundRed() {
        return (float) FastColor.ARGB32.red(this.backgroundColor) / 255.0F;
    }

    public float getBackgroundGreen() {
        return (float) FastColor.ARGB32.green(this.backgroundColor) / 255.0F;
    }

    public float getBackgroundBlue() {
        return (float) FastColor.ARGB32.blue(this.backgroundColor) / 255.0F;
    }

    public float getBackgroundAlpha() {
        return (float) FastColor.ARGB32.alpha(this.backgroundColor) / 255.0F;
    }

    // Factory methods
    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, Component component) {
        return new ComponentParticleOption(type, component, 0x00000000, true);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, Component component, int backgroundColor) {
        return new ComponentParticleOption(type, component, backgroundColor, true);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, Component component, int backgroundColor, boolean dropShadow) {
        return new ComponentParticleOption(type, component, backgroundColor, dropShadow);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, Component component, float bgRed, float bgGreen, float bgBlue, float bgAlpha) {
        int backgroundColor = FastColor.ARGB32.colorFromFloat(bgAlpha, bgRed, bgGreen, bgBlue);
        return new ComponentParticleOption(type, component, backgroundColor, true);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, Component component, float bgRed, float bgGreen, float bgBlue, float bgAlpha, boolean dropShadow) {
        int backgroundColor = FastColor.ARGB32.colorFromFloat(bgAlpha, bgRed, bgGreen, bgBlue);
        return new ComponentParticleOption(type, component, backgroundColor, dropShadow);
    }

    // Convenience methods for plain text
    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, String text) {
        return new ComponentParticleOption(type, Component.literal(text), 0x00000000, true);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, String text, int backgroundColor) {
        return new ComponentParticleOption(type, Component.literal(text), backgroundColor, true);
    }

    public static ComponentParticleOption create(ParticleType<ComponentParticleOption> type, String text, int backgroundColor, boolean dropShadow) {
        return new ComponentParticleOption(type, Component.literal(text), backgroundColor, dropShadow);
    }
}