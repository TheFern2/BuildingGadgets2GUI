package dev.thefern.buildinggadgets2gui;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
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

    private static final HashMap<UUID, UUID> lastKnownCopyUUID = new HashMap<>();

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
                    BuildingGadgets2GUI.LOGGER.info("Copy Paste Gadget - COPY operation detected!");
                    BuildingGadgets2GUI.LOGGER.info("Player: {}", player.getName().getString());
                    BuildingGadgets2GUI.LOGGER.info("Position: {}", event.getPos());
                    BuildingGadgets2GUI.LOGGER.info("Gadget UUID: {}", gadgetUUID);
                    BuildingGadgets2GUI.LOGGER.info("==============================================");
                    
                    lastKnownCopyUUID.put(gadgetUUID, GadgetNBT.getCopyUUID(heldItem));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (lastKnownCopyUUID.isEmpty()) return;
        
        ServerLevel overworld = event.getServer().overworld();
        BG2Data bg2Data = BG2Data.get(overworld);
        
        ArrayList<UUID> toRemove = new ArrayList<>();
        
        for (UUID gadgetUUID : lastKnownCopyUUID.keySet()) {
            UUID oldCopyUUID = lastKnownCopyUUID.get(gadgetUUID);
            
            ArrayList<StatePos> copiedBlocks = bg2Data.getCopyPasteList(gadgetUUID, false);
            
            if (copiedBlocks != null && !copiedBlocks.isEmpty()) {
                BuildingGadgets2GUI.LOGGER.info("==============================================");
                BuildingGadgets2GUI.LOGGER.info("Copy Paste Gadget - COPY COMPLETED!");
                BuildingGadgets2GUI.LOGGER.info("Gadget UUID: {}", gadgetUUID);
                BuildingGadgets2GUI.LOGGER.info("Total blocks copied: {}", copiedBlocks.size());
                BuildingGadgets2GUI.LOGGER.info("First 5 blocks:");
                
                int count = 0;
                for (StatePos statePos : copiedBlocks) {
                    if (count >= 5) break;
                    BuildingGadgets2GUI.LOGGER.info("  - {} at {}", 
                        statePos.state.getBlock().getName().getString(), 
                        statePos.pos);
                    count++;
                }
                
                if (copiedBlocks.size() > 5) {
                    BuildingGadgets2GUI.LOGGER.info("  ... and {} more blocks", copiedBlocks.size() - 5);
                }
                
                BuildingGadgets2GUI.LOGGER.info("==============================================");
                
                toRemove.add(gadgetUUID);
            }
        }
        
        for (UUID uuid : toRemove) {
            lastKnownCopyUUID.remove(uuid);
        }
    }
}

