package dev.thefern.buildinggadgets2gui.network;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PacketRequestTEData {
    public static final PacketRequestTEData INSTANCE = new PacketRequestTEData();

    public static PacketRequestTEData get() {
        return INSTANCE;
    }

    public void handle(final RequestTEDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            UUID gadgetUUID = payload.gadgetUUID();
            
            BuildingGadgets2GUI.LOGGER.info("==============================================");
            BuildingGadgets2GUI.LOGGER.info("[SERVER] Received RequestTEData for gadget: {}", gadgetUUID.toString().substring(0, 8) + "...");
            
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
            
            ArrayList<TagPos> teData = bg2Data.peekTEMap(gadgetUUID);
            
            CompoundTag teDataTag = new CompoundTag();
            if (teData != null && !teData.isEmpty()) {
                ListTag teList = new ListTag();
                for (TagPos tagPos : teData) {
                    teList.add(tagPos.getTag());
                }
                teDataTag.put("tedata", teList);
                BuildingGadgets2GUI.LOGGER.info("Found {} TileEntity entries for gadget", teData.size());
            } else {
                BuildingGadgets2GUI.LOGGER.info("No TileEntity data found for gadget");
            }
            
            ((ServerPlayer) player).connection.send(new SendTEDataPayload(gadgetUUID, teDataTag));
            BuildingGadgets2GUI.LOGGER.info("Sent TEData to client");
            BuildingGadgets2GUI.LOGGER.info("==============================================");
        });
    }
}
