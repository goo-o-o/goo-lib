package com.goo.goo_lib.common.network;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.Easing;
import com.goo.goo_lib.util.screenshake.ShakeInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

public record ScreenShakePayload(
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
        Optional<Vec3> sourcePos, // nullable
        double radius
) implements CustomPacketPayload {

    public ScreenShakePayload(ShakeInstance instance) {
        this(instance.speed, instance.durationTicks,
                instance.fadeInTicks, instance.fadeOutTicks,
                instance.fadeInCurve, instance.fadeOutCurve,
                instance.motionBlur,
                instance.maxX, instance.maxY,
                instance.maxPitch, instance.maxYaw, instance.maxRoll,
                Optional.ofNullable(instance.sourcePos), instance.radius
        );
    }

    public static final Type<ScreenShakePayload> TYPE = new Type<>(GooLib.loc("screen_shake"));
    private static final StreamCodec<ByteBuf, Easing> EASING_CODEC =
            ByteBufCodecs.idMapper(id -> Easing.values()[id], Easing::ordinal);
    private static final StreamCodec<ByteBuf, Vec3> VEC3_CODEC =
            ByteBufCodecs.VECTOR3F.map(vec -> new Vec3(vec.x, vec.y, vec.z), vec -> new Vector3f((float) vec.x, (float) vec.y, (float) vec.z));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShakePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeFloat(payload.speed);
                buf.writeVarInt(payload.durationTicks);
                buf.writeVarInt(payload.fadeInTicks);
                buf.writeVarInt(payload.fadeOutTicks);
                EASING_CODEC.encode(buf, payload.fadeInCurve);
                EASING_CODEC.encode(buf, payload.fadeOutCurve);
                buf.writeBoolean(payload.motionBlur);
                buf.writeFloat(payload.maxX);
                buf.writeFloat(payload.maxY);
                buf.writeFloat(payload.maxPitch);
                buf.writeFloat(payload.maxYaw);
                buf.writeFloat(payload.maxRoll);
                ByteBufCodecs.optional(VEC3_CODEC).encode(buf, payload.sourcePos);
                buf.writeDouble(payload.radius);
            },
            buf -> new ScreenShakePayload(
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    EASING_CODEC.decode(buf),
                    EASING_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    ByteBufCodecs.optional(VEC3_CODEC).decode(buf),
                    buf.readDouble()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}