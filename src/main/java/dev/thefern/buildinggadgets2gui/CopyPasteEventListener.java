package dev.thefern.buildinggadgets2gui;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = BuildingGadgets2GUI.MODID)
public class CopyPasteEventListener {

    private static final HashMap<UUID, CopyDataSnapshot> trackedGadgets = new HashMap<>();
    
    private static class CopyDataSnapshot {
        UUID copyUUID;
        int blockCount;
        long lastCheckTick;
        
        CopyDataSnapshot(UUID copyUUID, int blockCount, long tick) {
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            this.lastCheckTick = tick;
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof GadgetCopyPaste) {
            if (!event.getLevel().isClientSide()) {
                String mode = GadgetNBT.getMode(heldItem).getId().getPath();
                
                if (mode.equals("copy")) {
                    UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
                    
                    BuildingGadgets2GUI.LOGGER.info("==============================================");
                    BuildingGadgets2GUI.LOGGER.info("Copy Paste Gadget - COPY operation initiated");
                    BuildingGadgets2GUI.LOGGER.info("Player: {}", player.getName().getString());
                    BuildingGadgets2GUI.LOGGER.info("Position: {}", event.getPos());
                    BuildingGadgets2GUI.LOGGER.info("Gadget UUID: {}", formatUUID(gadgetUUID));
                    BuildingGadgets2GUI.LOGGER.info("==============================================");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        BG2Data bg2Data = BG2Data.get(overworld);
        long currentTick = overworld.getGameTime();
        
        for (Player player : overworld.players()) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            
            checkGadget(mainHand, bg2Data, player, currentTick);
            checkGadget(offHand, bg2Data, player, currentTick);
        }
    }
    
    private static void checkGadget(ItemStack stack, BG2Data bg2Data, Player player, long currentTick) {
        if (!(stack.getItem() instanceof GadgetCopyPaste)) return;
        
        UUID gadgetUUID = GadgetNBT.getUUID(stack);
        UUID currentCopyUUID = GadgetNBT.hasCopyUUID(stack) ? GadgetNBT.getCopyUUID(stack) : null;
        
        ArrayList<StatePos> copiedBlocks = bg2Data.getCopyPasteList(gadgetUUID, false);
        int currentBlockCount = (copiedBlocks != null) ? copiedBlocks.size() : 0;
        
        CopyDataSnapshot lastSnapshot = trackedGadgets.get(gadgetUUID);
        
        if (lastSnapshot == null) {
            if (currentBlockCount > 0) {
                logEvent("NEW_COPY", player, gadgetUUID, currentCopyUUID, null, 0, currentBlockCount, copiedBlocks);
                trackedGadgets.put(gadgetUUID, new CopyDataSnapshot(currentCopyUUID, currentBlockCount, currentTick));
            }
        } else {
            boolean copyUUIDChanged = (currentCopyUUID != null && !currentCopyUUID.equals(lastSnapshot.copyUUID));
            boolean blockCountChanged = (currentBlockCount != lastSnapshot.blockCount);
            
            if (copyUUIDChanged || blockCountChanged) {
                if (currentBlockCount == 0 && lastSnapshot.blockCount > 0) {
                    logEvent("CLEARED", player, gadgetUUID, currentCopyUUID, lastSnapshot.copyUUID, lastSnapshot.blockCount, 0, null);
                } else if (copyUUIDChanged && currentBlockCount > 0) {
                    logEvent("MODIFIED_COPY", player, gadgetUUID, currentCopyUUID, lastSnapshot.copyUUID, lastSnapshot.blockCount, currentBlockCount, copiedBlocks);
                } else if (blockCountChanged && currentBlockCount > 0) {
                    logEvent("RANGE_ADJUSTED", player, gadgetUUID, currentCopyUUID, lastSnapshot.copyUUID, lastSnapshot.blockCount, currentBlockCount, copiedBlocks);
                }
                
                trackedGadgets.put(gadgetUUID, new CopyDataSnapshot(currentCopyUUID, currentBlockCount, currentTick));
            }
        }
    }
    
    private static void logEvent(String eventType, Player player, UUID gadgetUUID, UUID newCopyUUID, 
                                 UUID oldCopyUUID, int oldCount, int newCount, ArrayList<StatePos> blocks) {
        BuildingGadgets2GUI.LOGGER.info("==============================================");
        BuildingGadgets2GUI.LOGGER.info("Copy Paste Event: {}", eventType);
        BuildingGadgets2GUI.LOGGER.info("Player: {}", player.getName().getString());
        BuildingGadgets2GUI.LOGGER.info("Gadget UUID: {}", formatUUID(gadgetUUID));
        
        if (oldCopyUUID != null) {
            BuildingGadgets2GUI.LOGGER.info("Old Copy UUID: {}", formatUUID(oldCopyUUID));
        }
        if (newCopyUUID != null) {
            BuildingGadgets2GUI.LOGGER.info("New Copy UUID: {}", formatUUID(newCopyUUID));
        }
        
        if (eventType.equals("CLEARED")) {
            BuildingGadgets2GUI.LOGGER.info("Previous blocks: {} -> Now: 0", oldCount);
        } else if (eventType.equals("MODIFIED_COPY") || eventType.equals("RANGE_ADJUSTED")) {
            BuildingGadgets2GUI.LOGGER.info("Block count: {} -> {}", oldCount, newCount);
        } else {
            BuildingGadgets2GUI.LOGGER.info("Total blocks: {}", newCount);
        }
        
        if (blocks != null && !blocks.isEmpty()) {
            BuildingGadgets2GUI.LOGGER.info("First 3 blocks:");
            int count = 0;
            for (StatePos statePos : blocks) {
                if (count >= 3) break;
                BuildingGadgets2GUI.LOGGER.info("  - {} at {}", 
                    statePos.state.getBlock().getName().getString(), 
                    statePos.pos);
                count++;
            }
            if (blocks.size() > 3) {
                BuildingGadgets2GUI.LOGGER.info("  ... and {} more blocks", blocks.size() - 3);
            }
        }
        
        BuildingGadgets2GUI.LOGGER.info("==============================================");
    }
    
    private static String formatUUID(UUID uuid) {
        if (uuid == null) return "null";
        return uuid.toString().substring(0, 8) + "...";
    }
}

