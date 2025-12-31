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
            Player player = context.player();
            ItemStack heldItem = player.getMainHandItem();
            
            if (!(heldItem.getItem() instanceof GadgetCopyPaste)) {
                BuildingGadgets2GUI.LOGGER.warn("Player {} tried to send clipboard data but is not holding a Copy/Paste gadget", player.getName().getString());
                return;
            }
            
            UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
            
            if (!gadgetUUID.equals(payload.gadgetUUID())) {
                BuildingGadgets2GUI.LOGGER.warn("Gadget UUID mismatch: expected {}, got {}", gadgetUUID, payload.gadgetUUID());
                return;
            }
            
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
            
            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(payload.tag());
            bg2Data.addToCopyPaste(gadgetUUID, buildList);
            GadgetNBT.setCopyUUID(heldItem, payload.copyUUID());
            
            BuildingGadgets2GUI.LOGGER.info("==============================================");
            BuildingGadgets2GUI.LOGGER.info("Clipboard data sent to gadget");
            BuildingGadgets2GUI.LOGGER.info("Player: {}", player.getName().getString());
            BuildingGadgets2GUI.LOGGER.info("Gadget UUID: {}", gadgetUUID.toString().substring(0, 8) + "...");
            BuildingGadgets2GUI.LOGGER.info("Copy UUID: {}", payload.copyUUID().toString().substring(0, 8) + "...");
            BuildingGadgets2GUI.LOGGER.info("Blocks: {}", buildList.size());
            BuildingGadgets2GUI.LOGGER.info("==============================================");
            
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(gadgetUUID, false);
            ((ServerPlayer) player).connection.send(
                new com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload(
                    gadgetUUID,
                    GadgetNBT.getCopyUUID(heldItem),
                    tag
                )
            );
        });
    }
}

