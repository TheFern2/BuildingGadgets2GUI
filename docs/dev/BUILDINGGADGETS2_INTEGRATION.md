# BuildingGadgets2 Integration Guide

## Overview
This document describes how to integrate with BuildingGadgets2's Copy Paste Gadget from your mod.

## Copy Paste Gadget Architecture

### Key Classes

1. **GadgetCopyPaste** (`com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste`)
   - Main item class for the Copy/Paste Gadget
   - Extends `BaseGadget`
   - Has two modes: "copy" and "paste"

2. **BG2Data** (`com.direwolf20.buildinggadgers2.common.worlddata.BG2Data`)
   - Server-side SavedData that stores copied blocks
   - Uses UUID-based lookup: `HashMap<UUID, ArrayList<StatePos>>`
   - Methods:
     - `addToCopyPaste(UUID uuid, ArrayList<StatePos> list)` - Stores copied data
     - `getCopyPasteList(UUID uuid, boolean remove)` - Retrieves copied data
     - `getCopyPasteListAsNBTMap(UUID uuid, boolean remove)` - Gets data as NBT

3. **BG2DataClient** (`com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient`)
   - Client-side cache of copy/paste data
   - Synchronized with server via network packets

4. **GadgetNBT** (`com.direwolf20.buildinggadgets2.util.GadgetNBT`)
   - Utility class for reading/writing gadget NBT data
   - Key methods:
     - `getUUID(ItemStack)` - Gets the gadget's unique ID
     - `getMode(ItemStack)` - Gets current mode (copy/paste)
     - `getCopyUUID(ItemStack)` - Gets the copy operation UUID
     - `setCopyStartPos(ItemStack, BlockPos)` - Sets copy start position
     - `setCopyEndPos(ItemStack, BlockPos)` - Sets copy end position

5. **StatePos** (`com.direwolf20.buildinggadgets2.util.datatypes.StatePos`)
   - Data class representing a BlockState at a specific position
   - Used to store the list of copied blocks

### How Copy Operation Works

1. **First Click (Start Position)**:
   - Player right-clicks with Copy Paste Gadget in "copy" mode
   - `onAction()` is called
   - `GadgetNBT.setCopyStartPos(gadget, context.pos())` sets start position
   - `buildAndStore()` is called

2. **Shift-Click (End Position)**:
   - Player shift-right-clicks to set end position
   - `onShiftAction()` is called
   - `GadgetNBT.setCopyEndPos(gadget, context.pos())` sets end position
   - `buildAndStore()` is called again

3. **buildAndStore() Method** (Line 118-125 in GadgetCopyPaste.java):
   ```java
   public void buildAndStore(ItemActionContext context, ItemStack gadget) {
       ArrayList<StatePos> buildList = new Copy().collect(...);
       UUID uuid = GadgetNBT.getUUID(gadget);
       GadgetNBT.setCopyUUID(gadget);
       BG2Data bg2Data = BG2Data.get(...);
       bg2Data.addToCopyPaste(uuid, buildList);
       context.player().displayClientMessage(...);
   }
   ```

### Network Packets

BuildingGadgets2 uses several network packets for copy/paste operations:

1. **SendCopyDataPayload** - Sends copied data from server to client
2. **RequestCopyDataPayload** - Client requests copy data from server
3. **SendCopyDataToServerPayload** - Client sends copy data to server (for templates)
4. **SendPastePayload** - Handles paste operations

### Events System

**Important**: BuildingGadgets2 does NOT expose custom events via an API. It uses standard NeoForge events internally.

To hook into copy operations, you have these options:

#### Option 1: Event Listener (Implemented)
Listen to `PlayerInteractEvent.RightClickBlock` and check if the player is holding a `GadgetCopyPaste` in copy mode.

**Pros**: 
- Simple, no mixins required
- Works with standard NeoForge events

**Cons**:
- Fires before the actual copy happens
- Cannot access the copied block list directly

#### Option 2: Mixin (More Advanced)
Create a Mixin to hook into `GadgetCopyPaste.buildAndStore()` method.

**Pros**:
- Direct access to copied blocks (`buildList`)
- Fires exactly when copy completes
- Can modify behavior if needed

**Cons**:
- Requires mixin setup
- More fragile (breaks if method signature changes)

#### Option 3: Access BG2Data Directly
Query `BG2Data` after detecting a copy operation to get the copied blocks.

```java
BG2Data bg2Data = BG2Data.get(server.overworld());
UUID gadgetUUID = GadgetNBT.getUUID(gadgetStack);
ArrayList<StatePos> copiedBlocks = bg2Data.getCopyPasteList(gadgetUUID, false);
```

## Current Implementation

The `CopyPasteEventListener` class in this mod uses **Option 1** to detect copy operations:

```java
@SubscribeEvent
public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    Player player = event.getEntity();
    ItemStack heldItem = player.getMainHandItem();

    if (heldItem.getItem() instanceof GadgetCopyPaste) {
        if (!event.getLevel().isClientSide()) {
            String mode = GadgetNBT.getMode(heldItem).getId().getPath();
            
            if (mode.equals("copy")) {
                // Copy operation detected!
                // Log or handle the event here
            }
        }
    }
}
```

## Additional Information

### Gadget Modes
The Copy Paste Gadget has two modes accessible via `GadgetNBT.getMode()`:
- `"copy"` - Selects blocks to copy
- `"paste"` - Places copied blocks

### Other Gadgets
BuildingGadgets2 includes several other gadgets:
- **Building Gadget** - Places blocks in patterns
- **Exchanging Gadget** - Replaces blocks
- **Destruction Gadget** - Removes blocks
- **Cut Paste Gadget** - Moves blocks (cut + paste)

### API Package
BuildingGadgets2 has a minimal API in `com.direwolf20.buildinggadgets2.api.gadgets`:
- `GadgetTarget` - Enum of gadget types
- `GadgetModes` - Interface for gadget modes

However, there are no custom events or hooks exposed through this API.

## Testing

To test the integration:
1. Build your mod with the BuildingGadgets2 dependency
2. Run the game with both mods loaded
3. Get a Copy Paste Gadget from creative inventory or craft it
4. Use the gadget to copy blocks
5. Check the logs for the detection messages

## Future Enhancements

Possible improvements to the integration:
1. Access the actual copied block list from BG2Data
2. Create custom events in your mod when copy is detected
3. Add GUI notifications when copy operations occur
4. Store additional metadata about copy operations
5. Integrate with other BuildingGadgets2 gadgets (Building, Exchanging, etc.)

