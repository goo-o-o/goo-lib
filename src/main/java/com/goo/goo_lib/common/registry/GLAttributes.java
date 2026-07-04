package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.common.GooLib;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GLAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, GooLib.MOD_ID);

    private static String prepend(String name){
        return "attributes." + GooLib.MOD_ID + "." + name;
    }

    public static final Holder<Attribute> VILLAGER_REPUTATION = ATTRIBUTES.register("villager_reputation", () -> new RangedAttribute(
            prepend("villager_reputation"),
            0, -10000, 10000
    ));

    public static final Holder<Attribute> LAVA_MOVEMENT_EFFICIENCY = ATTRIBUTES.register("lava_movement_efficiency", () -> new PercentageAttribute(
            prepend("lava_movement_efficiency"),
            0, 0.0, 1.0
    ).setSyncable(true));

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

    public static final Holder<Attribute> FRICTION_MODIFIER = ATTRIBUTES.register("friction_modifier", () -> new PercentageAttribute(
            prepend("friction_modifier"),
            1, 0, 10000
    ).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));


    public static final Holder<Attribute> AIR_DRAG_MODIFIER = ATTRIBUTES.register("air_drag_modifier", () -> new PercentageAttribute(
            prepend("air_drag_modifier"),
            1, 0, 10000
    ).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));

  

    public static final Holder<Attribute> BOAT_SPEED_MODIFIER = ATTRIBUTES.register("boat_speed_modifier", () -> new PercentageAttribute(
            prepend("boat_speed_modifier"),
            1, 0, 1000
    ).setSyncable(true));

    public static final Holder<Attribute> WALL_CLIMBING = ATTRIBUTES.register("wall_climbing", () -> new BooleanAttribute(
            prepend("wall_climbing"),
            false
    ).setSyncable(true));

    public static final Holder<Attribute> CLIMBING_SPEED_MODIFIER = ATTRIBUTES.register("climbing_speed_modifier", () -> new PercentageAttribute(
            prepend("climbing_speed_modifier"),
            1, 0, 1000
    ).setSyncable(true));

}
