package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.StyleEffectUtils;
import com.goo.goo_lib.common.registry.TextEffects;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Function;

@Mixin(Style.Serializer.class)
public abstract class StyleSerializerMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;",
                    remap = false // this is a DFU/Mojang-serialization library method, not an MC method
            )
    )
    private static MapCodec<Style> gl$wrapStyleCodec(
            Function<RecordCodecBuilder.Instance<Style>, ? extends App<RecordCodecBuilder.Mu<Style>, Style>> original) {

        MapCodec<Style> vanilla = RecordCodecBuilder.mapCodec(original);

        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                vanilla.forGetter(style -> style),
                TextEffects.CONFIGURED_EFFECT_LIST_CODEC
                        .optionalFieldOf("text_effects", List.of())
                        .forGetter(style -> ((StyleEffectContainer) style).gl$getEffects())
        ).apply(instance, (style, effects) -> {
            if (effects.isEmpty()) {
                return style; // nothing to attach, safe to return shared instance as-is
            }
            Style fresh = StyleEffectUtils.copyOf(style);
            ((StyleEffectContainer) (Object) fresh).gl$setEffects(effects);
            return fresh;
        }));
    }
}