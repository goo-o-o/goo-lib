package com.goo.goo_lib.common.network.clientbound;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.Easing;
import com.goo.goo_lib.util.screenshake.ShakeInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;

public record ScreenShakePayload(
        String identifier,
        float speed,
        int durationTicks,
        int fadeInTicks,
        int fadeOutTicks,
        Easing fadeInCurve,
        Easing fadeOutCurve,
        boolean motionBlur,
        float maxX,
        float maxY,
        float maxPitch,
        float maxYaw,
        float maxRoll,
        @Nullable
        Vector3f sourcePos, // nullable
        double radius
) implements CustomPacketPayload {

    public ScreenShakePayload(ShakeInstance instance) {
        this(
                instance.identifier,
                instance.speed, instance.durationTicks,
                instance.fadeInTicks, instance.fadeOutTicks,
                instance.fadeInCurve, instance.fadeOutCurve,
                instance.motionBlur,
                instance.maxX, instance.maxY,
                instance.maxPitch, instance.maxYaw, instance.maxRoll,
                instance.sourcePos.toVector3f(), instance.radius
        );
    }

    public static final Type<ScreenShakePayload> TYPE = new Type<>(GooLib.loc("screen_shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShakePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.identifier);
                buf.writeFloat(payload.speed);
                buf.writeVarInt(payload.durationTicks);
                buf.writeVarInt(payload.fadeInTicks);
                buf.writeVarInt(payload.fadeOutTicks);
                buf.writeEnum(payload.fadeInCurve);
                buf.writeEnum(payload.fadeOutCurve);
                buf.writeBoolean(payload.motionBlur);
                buf.writeFloat(payload.maxX);
                buf.writeFloat(payload.maxY);
                buf.writeFloat(payload.maxPitch);
                buf.writeFloat(payload.maxYaw);
                buf.writeFloat(payload.maxRoll);
                buf.writeNullable(payload.sourcePos(), (buffer, pos) -> buffer.writeVector3f(Objects.requireNonNull(pos)));
                buf.writeDouble(payload.radius);
            },
            buf -> new ScreenShakePayload(
                    buf.readUtf(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readEnum(Easing.class),
                    buf.readEnum(Easing.class),
                    buf.readBoolean(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readNullable((buffer) -> buffer.readVector3f()),
                    buf.readDouble()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}