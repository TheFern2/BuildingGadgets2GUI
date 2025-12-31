package dev.thefern.buildinggadgets2gui.client;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import dev.thefern.buildinggadgets2gui.client.tabs.HistoryTab;
import dev.thefern.buildinggadgets2gui.network.SendClipboardToGadgetPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.UUID;

public class ClipboardUtils {
    
    public static class CopyData {
        public boolean hasCopyData = false;
        public int blockCount = 0;
        public UUID gadgetUUID = null;
        public UUID copyUUID = null;
        public ArrayList<StatePos> copiedBlocks = null;
    }
    
    public static CopyData checkCopyData() {
        CopyData data = new CopyData();
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player == null || mc.level == null) {
            return data;
        }
        
        ItemStack heldItem = mc.player.getMainHandItem();
        
        if (heldItem.getItem() instanceof GadgetCopyPaste) {
            data.gadgetUUID = GadgetNBT.getUUID(heldItem);
            data.copyUUID = GadgetNBT.hasCopyUUID(heldItem) ? GadgetNBT.getCopyUUID(heldItem) : null;
            
            BG2DataClient.isClientUpToDate(heldItem);
            
            data.copiedBlocks = BG2DataClient.getLookupFromUUID(data.gadgetUUID);
            
            if (data.copiedBlocks != null && !data.copiedBlocks.isEmpty()) {
                data.hasCopyData = true;
                data.blockCount = data.copiedBlocks.size();
            } else {
                data.copiedBlocks = null;
            }
        }
        
        return data;
    }
    
    public static void copyFromTool() {
        CopyData data = checkCopyData();
        
        if (!data.hasCopyData || data.copiedBlocks == null || data.copiedBlocks.isEmpty()) {
            System.out.println("No copy data available on tool to copy!");
            return;
        }
        
        ArrayList<StatePos> clipboardBlocks = new ArrayList<>();
        for (StatePos statePos : data.copiedBlocks) {
            clipboardBlocks.add(new StatePos(statePos.state, statePos.pos.immutable()));
        }
        UUID clipboardCopyUUID = data.copyUUID;
        int clipboardBlockCount = clipboardBlocks.size();
        
        HistoryTab.setClipboard(clipboardBlocks, clipboardCopyUUID, clipboardBlockCount);
        HistoryTab.addToHistory(clipboardBlocks, clipboardCopyUUID, clipboardBlockCount);
        
        System.out.println("==============================================");
        System.out.println("Copied data from tool to clipboard!");
        System.out.println("Clipboard blocks: " + clipboardBlockCount);
        System.out.println("Clipboard Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("==============================================");
    }
    
    public static void sendToTool() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        ItemStack heldItem = mc.player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) {
            System.out.println("Not holding a Copy/Paste gadget!");
            return;
        }
        
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is empty! Use 'Copy from Tool' first.");
            return;
        }
        
        UUID targetGadgetUUID = GadgetNBT.getUUID(heldItem);
        UUID newCopyUUID = UUID.randomUUID();
        
        System.out.println("==============================================");
        System.out.println("[CLIENT] Sending clipboard data to server...");
        System.out.println("Target Gadget UUID: " + targetGadgetUUID.toString().substring(0, 8) + "...");
        System.out.println("New Copy UUID: " + newCopyUUID.toString().substring(0, 8) + "...");
        System.out.println("Blocks to send: " + clipboardBlocks.size());
        
        CompoundTag tag = BG2Data.statePosListToNBTMapArray(clipboardBlocks);
        System.out.println("NBT Tag size: " + tag.size());
        
        PacketDistributor.sendToServer(new SendClipboardToGadgetPayload(targetGadgetUUID, newCopyUUID, tag));
        
        System.out.println("Packet sent to server!");
        System.out.println("==============================================");
    }
    
    public static void printClipboard() {
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        UUID clipboardCopyUUID = HistoryTab.getClipboardCopyUUID();
        int clipboardBlockCount = HistoryTab.getClipboardBlockCount();
        
        System.out.println("==============================================");
        System.out.println("Clipboard Data:");
        
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is EMPTY");
        } else {
            System.out.println("Block count: " + clipboardBlockCount);
            System.out.println("Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
            System.out.println("First 5 blocks:");
            int count = 0;
            for (StatePos statePos : clipboardBlocks) {
                if (count >= 5) break;
                System.out.println("  - " + statePos.state.getBlock().getName().getString() + " at " + statePos.pos);
                count++;
            }
            if (clipboardBlocks.size() > 5) {
                System.out.println("  ... and " + (clipboardBlocks.size() - 5) + " more blocks");
            }
        }
        
        System.out.println("==============================================");
    }
    
    public static void clearClipboard() {
        HistoryTab.setClipboard(null, null, 0);
        
        System.out.println("==============================================");
        System.out.println("Clipboard cleared!");
        System.out.println("==============================================");
    }
    
    public static String formatUUID(UUID uuid) {
        if (uuid == null) return "null";
        return uuid.toString().substring(0, 8) + "...";
    }
}

