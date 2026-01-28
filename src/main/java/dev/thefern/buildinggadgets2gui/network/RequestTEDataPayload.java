package dev.thefern.buildinggadgets2gui.network;

import dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RequestTEDataPayload(
        UUID gadgetUUID
) implements CustomPacketPayload {
    public static final Type<RequestTEDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2GUI.MODID, "request_te_data"));

    @Override
    public Type<RequestTEDataPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, RequestTEDataPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestTEDataPayload::gadgetUUID,
            RequestTEDataPayload::new
    );
}
