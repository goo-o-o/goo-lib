package com.goo.goo_lib.common.event.custom;

import lombok.Getter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import lombok.Setter;

public abstract class LivingDodgeEvent extends LivingEvent {
    @Getter
    private final DamageSource source;
    @Getter
    private final DamageContainer container;

    public LivingDodgeEvent(LivingEntity entity, DamageSource source, DamageContainer container) {
        super(entity);
        this.source = source;
        this.container = container;
    }

    /**
     * Fired BEFORE checking dodge probability. Cancellable & allows roll/dodge chance modification.
     */
    public static class Pre extends LivingDodgeEvent implements ICancellableEvent {
        @Getter
        @Setter
        private double roll;
        @Getter
        @Setter
        private double dodgeChance;

        public Pre(LivingEntity entity, double roll, double dodgeChance, DamageSource source, DamageContainer container) {
            super(entity, source, container);
            this.roll = roll;
            this.dodgeChance = dodgeChance;
        }
    }

    /**
     * Fired AFTER the entity successfully dodges damage. Non-cancellable.
     */
    public static class Post extends LivingDodgeEvent {
        @Getter
        private final double finalRoll;
        @Getter
        private final double finalDodgeChance;

        public Post(LivingEntity entity, double finalRoll, double finalDodgeChance, DamageSource source, DamageContainer container) {
            super(entity, source, container);
            this.finalRoll = finalRoll;
            this.finalDodgeChance = finalDodgeChance;
        }
    }
}
