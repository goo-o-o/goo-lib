package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.client.ClientPayloadHandler;
import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.common.ServerPayloadHandler;
import com.goo.goo_lib.common.network.clientbound.DisplayItemActivationPayload;
import com.goo.goo_lib.common.network.clientbound.ResetEnvironmentColorPayload;
import com.goo.goo_lib.common.network.clientbound.ScreenShakePayload;
import com.goo.goo_lib.common.network.clientbound.SetEnvironmentColorPayload;
import com.goo.goo_lib.common.network.serverbound.SetItemStackInSlotPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = GooLib.MOD_ID)
public class GLPayloads {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ScreenShakePayload.TYPE,
                ScreenShakePayload.STREAM_CODEC,
                ClientPayloadHandler::handle
        );
        registrar.playToClient(
                SetEnvironmentColorPayload.TYPE,
                SetEnvironmentColorPayload.STREAM_CODEC,
                ClientPayloadHandler::handle
        );
        registrar.playToClient(
                ResetEnvironmentColorPayload.TYPE,
                ResetEnvironmentColorPayload.STREAM_CODEC,
                ClientPayloadHandler::handle
        );
        registrar.playToClient(
                DisplayItemActivationPayload.TYPE,
                DisplayItemActivationPayload.STREAM_CODEC,
                ClientPayloadHandler::handle
        );
        registrar.playToServer(
                SetItemStackInSlotPayload.TYPE,
                SetItemStackInSlotPayload.STREAM_CODEC,
                ServerPayloadHandler::handle
        );
    }

}
