package dev.thefern.buildinggadgets2gui.network;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PacketSendClipboardToGadget {
    public static final PacketSendClipboardToGadget INSTANCE = new PacketSendClipboardToGadget();

    public static PacketSendClipboardToGadget get() {
        return INSTANCE;
    }

    public void handle(final SendClipboardToGadgetPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            BuildingGadgets2GUI.LOGGER.info("==============================================");
            BuildingGadgets2GUI.LOGGER.info("[SERVER] Received SendClipboardToGadget packet");
            
            Player player = context.player();
            ItemStack heldItem = player.getMainHandItem();
            
            BuildingGadgets2GUI.LOGGER.info("Player: {}", player.getName().getString());
            BuildingGadgets2GUI.LOGGER.info("Held item: {}", heldItem.getItem().toString());
            
            if (!(heldItem.getItem() instanceof GadgetCopyPaste)) {
                BuildingGadgets2GUI.LOGGER.warn("Player {} tried to send clipboard data but is not holding a Copy/Paste gadget", player.getName().getString());
                BuildingGadgets2GUI.LOGGER.info("==============================================");
                return;
            }
            
            UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
            
            BuildingGadgets2GUI.LOGGER.info("Gadget UUID from item: {}", gadgetUUID.toString().substring(0, 8) + "...");
            BuildingGadgets2GUI.LOGGER.info("Gadget UUID from payload: {}", payload.gadgetUUID().toString().substring(0, 8) + "...");
            
            if (!gadgetUUID.equals(payload.gadgetUUID())) {
                BuildingGadgets2GUI.LOGGER.warn("Gadget UUID mismatch: expected {}, got {}", gadgetUUID, payload.gadgetUUID());
                BuildingGadgets2GUI.LOGGER.info("==============================================");
                return;
            }
            
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
            
            BuildingGadgets2GUI.LOGGER.info("Converting NBT to StatePos list...");
            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(payload.tag());
            BuildingGadgets2GUI.LOGGER.info("Converted {} blocks from NBT", buildList.size());
            
            if (buildList.isEmpty()) {
                BuildingGadgets2GUI.LOGGER.warn("Build list is empty after conversion!");
                BuildingGadgets2GUI.LOGGER.info("==============================================");
                return;
            }
            
            BuildingGadgets2GUI.LOGGER.info("Clearing copy start/end positions (no green box needed for clipboard paste)");
            net.minecraft.core.BlockPos nullPos = new net.minecraft.core.BlockPos(-999, -999, -999);
            GadgetNBT.setCopyStartPos(heldItem, nullPos);
            GadgetNBT.setCopyEndPos(heldItem, nullPos);
            
            BuildingGadgets2GUI.LOGGER.info("Adding to BG2Data...");
            bg2Data.addToCopyPaste(gadgetUUID, buildList);
            
            BuildingGadgets2GUI.LOGGER.info("Setting copy UUID on gadget...");
            GadgetNBT.setCopyUUID(heldItem, payload.copyUUID());
            
            UUID verifyUUID = GadgetNBT.getCopyUUID(heldItem);
            BuildingGadgets2GUI.LOGGER.info("Copy UUID set to: {}", verifyUUID.toString().substring(0, 8) + "...");
            
            BuildingGadgets2GUI.LOGGER.info("Sending sync packet to client...");
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(gadgetUUID, false);
            BuildingGadgets2GUI.LOGGER.info("Sync packet NBT size: {}", tag.size());
            
            ((ServerPlayer) player).connection.send(
                new com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload(
                    gadgetUUID,
                    GadgetNBT.getCopyUUID(heldItem),
                    tag
                )
            );
            
            BuildingGadgets2GUI.LOGGER.info("Clipboard data successfully sent to gadget!");
            BuildingGadgets2GUI.LOGGER.info("==============================================");
        });
    }
}

