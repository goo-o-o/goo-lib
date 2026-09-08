package com.goo.goo_lib.common.network.serverbound;

import com.goo.goo_lib.common.GooLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record DamageEntityPayload(
        int targetEntityId,
        float amount,
        ResourceKey<DamageType> damageTypeKey,
        Optional<Integer> directEntityId,
        Optional<Integer> causingEntityId
) implements CustomPacketPayload {

    public static final Type<DamageEntityPayload> TYPE =
            new Type<>(GooLib.loc("damage_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageEntityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            DamageEntityPayload::targetEntityId,
            ByteBufCodecs.FLOAT,
            DamageEntityPayload::amount,
            ResourceKey.streamCodec(Registries.DAMAGE_TYPE),
            DamageEntityPayload::damageTypeKey,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            DamageEntityPayload::directEntityId,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            DamageEntityPayload::causingEntityId,
            DamageEntityPayload::new
    );

    // convenience constructor for passing nullable integers directly
    public DamageEntityPayload(
            int targetEntityId,
            float amount,
            ResourceKey<DamageType> damageTypeKey,
            @Nullable Integer directEntityId,
            @Nullable Integer causingEntityId
    ) {
        this(
                targetEntityId,
                amount,
                damageTypeKey,
                Optional.ofNullable(directEntityId),
                Optional.ofNullable(causingEntityId)
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}