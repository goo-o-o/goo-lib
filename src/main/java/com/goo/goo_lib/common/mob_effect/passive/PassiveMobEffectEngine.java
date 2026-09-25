package com.goo.goo_lib.common.mob_effect.passive;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveMobEffectEngine {

    private static final Map<UUID, PassiveMobEffectRegistry> REGISTRIES = new HashMap<>();

    public static boolean setPassiveState(LivingEntity entity, PassiveMobEffect passive, boolean active) {
        if (entity.level().isClientSide()) return false;

        UUID uuid = entity.getUUID();
        PassiveMobEffectRegistry registry = REGISTRIES.computeIfAbsent(uuid, k -> new PassiveMobEffectRegistry());

        boolean stateChanged = registry.setPassiveState(entity, passive, active);

        if (registry.isEmpty()) {
            REGISTRIES.remove(uuid);
        }

        return stateChanged;
    }

    public static void clearEntity(LivingEntity entity) {
        REGISTRIES.remove(entity.getUUID());
    }
}