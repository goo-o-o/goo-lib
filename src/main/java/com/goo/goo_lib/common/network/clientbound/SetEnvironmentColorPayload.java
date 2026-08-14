package com.goo.goo_lib.common.network.clientbound;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.color.EnvironmentColorType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SetEnvironmentColorPayload(
        String identifier,
        EnvironmentColorType environmentColorType,
        int priority,
        int color

) implements CustomPacketPayload {

    public static final Type<SetEnvironmentColorPayload> TYPE = new Type<>(GooLib.loc("set_environment_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetEnvironmentColorPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.identifier);
                buf.writeEnum(payload.environmentColorType);
                buf.writeInt(payload.priority);
                buf.writeInt(payload.color);
            },
            buf -> new SetEnvironmentColorPayload(
                    buf.readUtf(),
                    buf.readEnum(EnvironmentColorType.class),
                    buf.readInt(),
                    buf.readInt()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}