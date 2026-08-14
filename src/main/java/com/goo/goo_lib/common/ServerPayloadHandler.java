package com.goo.goo_lib.common;

import com.goo.goo_lib.common.network.serverbound.SetItemStackInSlotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handle(final SetItemStackInSlotPayload payload, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            ServerPlayer player = ((ServerPlayer) context.player());
            player.getInventory().setItem(payload.slot(), payload.stack());
        }
    }
}
