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

public record SendClipboardToGadgetPayload(
        UUID gadgetUUID,
        UUID copyUUID,
        CompoundTag tag
) implements CustomPacketPayload {
    public static final Type<SendClipboardToGadgetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2GUI.MODID, "send_clipboard_to_gadget"));

    @Override
    public Type<SendClipboardToGadgetPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendClipboardToGadgetPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendClipboardToGadgetPayload::gadgetUUID,
            UUIDUtil.STREAM_CODEC, SendClipboardToGadgetPayload::copyUUID,
            ByteBufCodecs.COMPOUND_TAG, SendClipboardToGadgetPayload::tag,
            SendClipboardToGadgetPayload::new
    );
}

