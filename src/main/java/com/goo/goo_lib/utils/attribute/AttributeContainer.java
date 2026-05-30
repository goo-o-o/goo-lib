package com.goo.goo_lib.utils.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeContainer(
        Holder<Attribute> attribute,
        double value,
        AttributeModifier.Operation operation
) {
    public static final Codec<AttributeContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeContainer::attribute),
            Codec.DOUBLE.fieldOf("value").forGetter(AttributeContainer::value),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeContainer::operation)
    ).apply(instance, AttributeContainer::new));
}