package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.common.GooLib;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GLAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, GooLib.MOD_ID);

    private static String prepend(String name){
        return "attributes." + GooLib.MOD_ID + "." + name;
    }

    public static final Holder<Attribute> LIFESTEAL = ATTRIBUTES.register("lifesteal", () -> new PercentageAttribute(
            prepend("lifesteal"),
            0, 0, 10000
    ));
    public static final Holder<Attribute> DRAW_SPEED = ATTRIBUTES.register("draw_speed", () -> new PercentageAttribute(
            prepend("draw_speed"),
            1, 0.01, 10000
    ).setSyncable(true));

    public static final Holder<Attribute> ARROW_VELOCITY = ATTRIBUTES.register("arrow_velocity", () -> new PercentageAttribute(
            prepend("arrow_velocity"),
            1, 0.01, 10000
    ));
    public static final Holder<Attribute> ARROW_DAMAGE = ATTRIBUTES.register("arrow_damage", () -> new PercentageAttribute(
            prepend("arrow_damage"),
            1, 0.01, 10000
    ));

    public static final Holder<Attribute> ARROW_GRAVITY = ATTRIBUTES.register("arrow_gravity", () -> new PercentageAttribute(
            prepend("arrow_gravity"),
            1, 0.01, 10000
    ).setSyncable(true));

    public static final Holder<Attribute> CRITICAL_DAMAGE = ATTRIBUTES.register("critical_damage", () -> new PercentageAttribute(
            prepend("critical_damage"),
            1.5, 0, 10000
    ));

    public static final Holder<Attribute> XP_GAIN = ATTRIBUTES.register("xp_gain", () -> new PercentageAttribute(
            prepend("xp_gain"),
            1, 0, 10000
    ));

    public static final Holder<Attribute> HEALING_RECEIVED = ATTRIBUTES.register("healing_received", () -> new PercentageAttribute(
            prepend("healing_received"),
            1, 0, 10000
    ));


}
