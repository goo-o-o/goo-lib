package com.goo.goo_lib.common.event;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.common.mob_effect.ClientSyncableEffect;
import com.goo.goo_lib.common.mob_effect.ExpirableEffect;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = GooLib.MOD_ID)
public class GLEffectEvents {
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = event.getEffectInstance();

        if (entity.level() instanceof ServerLevel serverLevel) {
            if (instance.getEffect().value() instanceof ClientSyncableEffect) {
                serverLevel.getChunkSource().broadcastAndSend(
                        entity,
                        new ClientboundUpdateMobEffectPacket(entity.getId(), instance, true)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;


        if (effectInstance.getEffect().value() instanceof ExpirableEffect expirableEffect) {
            expirableEffect.onEffectExpired(event.getEntity(), effectInstance);
        }

    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;


        if (effectInstance.getEffect().value() instanceof ExpirableEffect expirableEffect) {
            expirableEffect.onEffectRemoved(event.getEntity(), effectInstance);
        }
    }
}
