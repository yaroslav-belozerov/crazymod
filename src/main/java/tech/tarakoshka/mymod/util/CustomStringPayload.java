package tech.tarakoshka.mymod.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record CustomStringPayload(String message) implements CustomPacketPayload {

    public static final Type<CustomStringPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("yourmodid", "custom_packet"));

    public static final StreamCodec<FriendlyByteBuf, CustomStringPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    CustomStringPayload::message,
                    CustomStringPayload::new
            );

    @Override
    public @NotNull  Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
