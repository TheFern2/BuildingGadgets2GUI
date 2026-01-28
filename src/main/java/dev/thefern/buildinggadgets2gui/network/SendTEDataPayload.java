package dev.thefern.buildinggadgets2gui.network;

import dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SendTEDataPayload(
        UUID gadgetUUID,
        CompoundTag teDataTag
) implements CustomPacketPayload {
    public static final Type<SendTEDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2GUI.MODID, "send_te_data"));

    @Override
    public Type<SendTEDataPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendTEDataPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendTEDataPayload::gadgetUUID,
            ByteBufCodecs.COMPOUND_TAG, SendTEDataPayload::teDataTag,
            SendTEDataPayload::new
    );
}
