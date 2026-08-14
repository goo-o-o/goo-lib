package com.goo.goo_lib.client;

import com.goo.goo_lib.common.network.clientbound.DisplayItemActivationPayload;
import com.goo.goo_lib.common.network.clientbound.ResetEnvironmentColorPayload;
import com.goo.goo_lib.common.network.clientbound.ScreenShakePayload;
import com.goo.goo_lib.common.network.clientbound.SetEnvironmentColorPayload;
import com.goo.goo_lib.util.color.EnvironmentColorType;
import com.goo.goo_lib.util.color.EnvironmentColorUtil;
import com.goo.goo_lib.util.screenshake.ScreenShakeUtil;
import com.goo.goo_lib.util.screenshake.ShakeInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handle(final DisplayItemActivationPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameRenderer.displayItemActivation(payload.stack());
        });
    }

    public static void handle(final ResetEnvironmentColorPayload packet, final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                for (EnvironmentColorType type : packet.environmentColorTypes()) {
                    EnvironmentColorUtil.resetColor(packet.identifier(), type);
                }
            });
        }
    }

    public static void handle(final ScreenShakePayload packet, final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            ShakeInstance instance = ShakeInstance.builder()
                    .identifier(packet.identifier())
                    .speed(packet.speed())
                    .durationTicks(packet.durationTicks())
                    .fadeInTicks(packet.fadeInTicks())
                    .fadeOutTicks(packet.fadeOutTicks())
                    .fadeInCurve(packet.fadeInCurve())
                    .fadeOutCurve(packet.fadeOutCurve())
                    .motionBlur(packet.motionBlur())
                    .maxX(packet.maxX())
                    .maxY(packet.maxY())
                    .maxPitch(packet.maxPitch())
                    .maxYaw(packet.maxYaw())
                    .maxRoll(packet.maxRoll())
                    .sourcePos(packet.sourcePos() != null ? new Vec3(packet.sourcePos()) : null)
                    .radius(packet.radius())
                    .build();
            ScreenShakeUtil.addShake(instance);
        }
    }

    public static void handle(final SetEnvironmentColorPayload packet, final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> EnvironmentColorUtil.setColorOverride(packet.identifier(), packet.environmentColorType(), packet.priority(), packet.color()));
        }
    }
}
