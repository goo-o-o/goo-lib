package com.goo.goo_lib.common.network.clientbound;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.color.EnvironmentColorType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record ResetEnvironmentColorPayload(
        String identifier,
        Set<EnvironmentColorType> environmentColorTypes
) implements CustomPacketPayload {

    public static final Type<ResetEnvironmentColorPayload> TYPE = new Type<>(GooLib.loc("reset_environment_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResetEnvironmentColorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ResetEnvironmentColorPayload::identifier,
            ByteBufCodecs.collection(HashSet::new, NeoForgeStreamCodecs.enumCodec(EnvironmentColorType.class)), ResetEnvironmentColorPayload::environmentColorTypes,
            ResetEnvironmentColorPayload::new
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}