package dev.thefern.buildinggadgets2gui.network;

import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI;
import dev.thefern.buildinggadgets2gui.client.TEDataClientCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.UUID;

public class PacketSendTEData {
    public static final PacketSendTEData INSTANCE = new PacketSendTEData();

    public static PacketSendTEData get() {
        return INSTANCE;
    }

    public void handle(final SendTEDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID gadgetUUID = payload.gadgetUUID();
            CompoundTag teDataTag = payload.teDataTag();
            
            BuildingGadgets2GUI.LOGGER.info("==============================================");
            BuildingGadgets2GUI.LOGGER.info("[CLIENT] Received SendTEData for gadget: {}", gadgetUUID.toString().substring(0, 8) + "...");
            
            ArrayList<TagPos> teData = new ArrayList<>();
            
            if (teDataTag.contains("tedata")) {
                ListTag teList = teDataTag.getList("tedata", Tag.TAG_COMPOUND);
                for (int i = 0; i < teList.size(); i++) {
                    TagPos tagPos = new TagPos(teList.getCompound(i));
                    teData.add(tagPos);
                }
                BuildingGadgets2GUI.LOGGER.info("Parsed {} TileEntity entries", teData.size());
            } else {
                BuildingGadgets2GUI.LOGGER.info("No TileEntity data in payload");
            }
            
            TEDataClientCache.storeTEData(gadgetUUID, teData);
            BuildingGadgets2GUI.LOGGER.info("Stored TEData in client cache");
            BuildingGadgets2GUI.LOGGER.info("==============================================");
        });
    }
}
