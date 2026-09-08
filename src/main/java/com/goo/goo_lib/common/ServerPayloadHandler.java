package com.goo.goo_lib.common;

import com.goo.goo_lib.common.network.serverbound.DamageEntityPayload;
import com.goo.goo_lib.common.network.serverbound.SetItemStackInSlotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handle(final SetItemStackInSlotPayload payload, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            ServerPlayer player = ((ServerPlayer) context.player());
            player.getInventory().setItem(payload.slot(), payload.stack());
        }
    }

    public static void handle(final DamageEntityPayload payload, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            Player player = context.player();
            Level level = player.level();
            Entity target = level.getEntity(payload.targetEntityId());

            if (target != null && target.isAlive()) {
                Entity directEntity = payload.directEntityId()
                        .map(level::getEntity)
                        .orElse(null);

                Entity causingEntity = payload.causingEntityId()
                        .map(level::getEntity)
                        .orElse(null);

                DamageSource source = player.damageSources().source(
                        payload.damageTypeKey(),
                        directEntity,
                        causingEntity
                );

                target.hurt(source, payload.amount());
            }
        }
    }
}
