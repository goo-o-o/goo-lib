package com.goo.goo_lib.common.mob_effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Automatically syncs with clients for non-player entities
 */
public class ClientSyncableEffect extends ExpirableEffect {
    public ClientSyncableEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public ClientSyncableEffect(MobEffectCategory category, int color, ParticleOptions particle) {
        super(category, color, particle);
    }

    @Override
    public void onEffectRemoved(LivingEntity livingEntity, MobEffectInstance instance) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(
                    livingEntity,
                    new ClientboundRemoveMobEffectPacket(livingEntity.getId(), instance.getEffect())
            );
        }
    }

    @Override
    public void onEffectExpired(LivingEntity livingEntity, MobEffectInstance instance) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(
                    livingEntity,
                    new ClientboundRemoveMobEffectPacket(livingEntity.getId(), instance.getEffect())
            );
        }
    }
}
