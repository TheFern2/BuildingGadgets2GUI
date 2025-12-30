# Copy Paste Gadget - Internal Reference

## Overview
This document provides detailed internal implementation details of the Copy Paste Gadget from BuildingGadgets2. Use this as a reference when building GUI features or integrations.

---

## Core Classes

### 1. GadgetCopyPaste
**Package**: `com.direwolf20.buildinggadgets2.common.items`

**Key Methods**:

#### `onAction(ItemActionContext context)` (Line 70-96)
Called when player right-clicks (non-shift).

**Copy Mode**:
```java
if (mode.getId().getPath().equals("copy")) {
    GadgetNBT.setCopyStartPos(gadget, context.pos());
    buildAndStore(context, gadget);
}
```

**Paste Mode**:
```java
if (mode.getId().getPath().equals("paste")) {
    UUID uuid = GadgetNBT.getUUID(gadget);
    BG2Data bg2Data = BG2Data.get(server.overworld());
    ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(uuid, false);
    // ... paste logic
}
```

#### `onShiftAction(ItemActionContext context)` (Line 102-116)
Called when player shift-right-clicks.

**Copy Mode**:
```java
if (mode.getId().getPath().equals("copy")) {
    GadgetNBT.setCopyEndPos(gadget, context.pos());
    buildAndStore(context, gadget);
}
```

#### `buildAndStore(ItemActionContext context, ItemStack gadget)` (Line 118-125)
The core copy operation method.

**Process**:
1. Collects blocks using `Copy().collect()`
2. Gets gadget UUID
3. Generates new copy UUID
4. Stores in BG2Data
5. Displays message to player

```java
ArrayList<StatePos> buildList = new Copy().collect(
    context.hitResult().getDirection(), 
    context.player(), 
    context.pos(), 
    Blocks.AIR.defaultBlockState()
);
UUID uuid = GadgetNBT.getUUID(gadget);
GadgetNBT.setCopyUUID(gadget);
BG2Data bg2Data = BG2Data.get(server.overworld());
bg2Data.addToCopyPaste(uuid, buildList);
context.player().displayClientMessage(
    Component.translatable("buildinggadgets2.messages.copyblocks", buildList.size()), 
    true
);
```

---

### 2. Copy Mode Class
**Package**: `com.direwolf20.buildinggadgets2.util.modes`

#### `collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state)` (Line 36-74)

**Validation Steps**:
1. Checks if item is GadgetCopyPaste
2. Gets copy start and end positions from NBT
3. Validates positions are not null
4. Creates AABB from start/end positions
5. Validates axis sizes (max 500 blocks per axis)
6. Validates total volume (max 100,000 blocks)

**Collection Process**:
```java
BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
    if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos) 
        && !(level.getBlockState(pos).getBlock() instanceof RenderBlock))
        coordinates.add(new StatePos(
            GadgetUtils.cleanBlockState(level.getBlockState(pos)), 
            pos.subtract(copyStart)  // RELATIVE POSITION!
        ));
    else
        coordinates.add(new StatePos(
            Blocks.AIR.defaultBlockState(), 
            pos.subtract(copyStart)
        ));
});
```

**Important**: Positions are stored **relative** to the copy start position!

---

### 3. StatePos Data Class
**Package**: `com.direwolf20.buildinggadgets2.util.datatypes`

**Structure**:
```java
public class StatePos {
    public BlockState state;  // The block state
    public BlockPos pos;      // Position (relative when copied)
}
```

**Key Methods**:

#### `getBlockStateMap(ArrayList<StatePos> list)` (Line 64-71)
Creates a unique list of all BlockStates in the copy.
```java
ArrayList<BlockState> blockStateMap = new ArrayList<>();
for (StatePos statePos : list) {
    if (!blockStateMap.contains(statePos.state))
        blockStateMap.add(statePos.state);
}
return blockStateMap;
```

#### `rotate90Degrees(ArrayList<StatePos> list, ArrayList<TagPos> tagListMutable)` (Line 73-106)
Rotates the entire copy 90 degrees clockwise.
```java
BlockState newState = oldState.rotate(Rotation.CLOCKWISE_90);
BlockPos newPos = new BlockPos(-oldPos.getZ(), oldPos.getY(), oldPos.getX());
```

#### `getItemList(ArrayList<StatePos> list)` (Line 109-121)
Calculates material requirements (client-side only).
```java
Map<ItemStackKey, Integer> itemList = new Object2IntOpenHashMap<>();
for (StatePos statePos : list) {
    ItemStackKey itemStackKey = new ItemStackKey(
        GadgetUtils.getItemForBlock(statePos.state, level, pos, player), 
        true
    );
    // Count items
}
```

---

### 4. BG2Data (Server-Side Storage)
**Package**: `com.direwolf20.buildinggadgers2.common.worlddata`

**Storage Structure**:
```java
private final HashMap<UUID, ArrayList<StatePos>> copyPasteLookup;  // Gadget UUID -> Blocks
private final HashMap<UUID, ArrayList<StatePos>> undoList;         // Build UUID -> Undo data
private final HashMap<UUID, ArrayList<TagPos>> teMap;              // Gadget UUID -> Tile entities
private final BiMap<UUID, String> redprintLookup;                  // UUID <-> Template name
```

**Key Methods**:

#### `addToCopyPaste(UUID uuid, ArrayList<StatePos> list)` (Line 81-84)
Stores copied blocks.
```java
copyPasteLookup.put(uuid, list);
this.setDirty();  // Marks world data as needing save
```

#### `getCopyPasteList(UUID uuid, boolean remove)` (Line 91-98)
Retrieves copied blocks.
```java
ArrayList<StatePos> returnList = copyPasteLookup.get(uuid);
if (remove) {
    returnList = copyPasteLookup.remove(uuid);
    this.setDirty();
}
return returnList;
```

#### `getCopyPasteListAsNBTMap(UUID uuid, boolean remove)` (Line 100-102)
Gets copy data as NBT (for network sync).
```java
return statePosListToNBTMap(getCopyPasteList(uuid, remove));
```

#### `statePosListToNBTMapArray(ArrayList<StatePos> list)` (Line 127-154)
Converts StatePos list to compact NBT format.

**Optimization**: Uses a block state map to avoid duplicating block state data:
```java
ArrayList<BlockState> blockStateMap = StatePos.getBlockStateMap(list);
ListTag blockStateMapList = StatePos.getBlockStateNBT(blockStateMap);
int[] blocklist = new int[list.size()];  // Indices into blockStateMap

// For each position, store index instead of full BlockState
blocklist[counter[0]++] = blockStateMap.indexOf(blockState);
```

**NBT Structure**:
```
{
  "startpos": {X, Y, Z},
  "endpos": {X, Y, Z},
  "blockstatemap": [BlockState1, BlockState2, ...],
  "statelist": [0, 1, 0, 2, 1, ...]  // Indices into blockstatemap
}
```

---

### 5. GadgetNBT Utility Class
**Package**: `com.direwolf20.buildinggadgets2.util`

**Gadget UUID Methods**:

#### `getUUID(ItemStack gadget)` (Line 192-196)
Gets or creates the gadget's unique identifier.
```java
if (!gadget.has(BG2DataComponents.GADGET_UUID))
    return setUUID(gadget);
return gadget.get(BG2DataComponents.GADGET_UUID);
```

**Copy UUID Methods**:

#### `setCopyUUID(ItemStack gadget)` (Line 198-202)
Generates a new copy operation UUID.
```java
UUID uuid = UUID.randomUUID();
gadget.set(BG2DataComponents.COPY_UUID, uuid);
return uuid;
```

#### `getCopyUUID(ItemStack gadget)` (Line 209-213)
Gets the current copy UUID (or creates one).

#### `hasCopyUUID(ItemStack gadget)` (Line 215-217)
Checks if gadget has copy data.

**Copy Position Methods**:

#### `setCopyStartPos(ItemStack gadget, BlockPos blockPos)` (Line 162-164)
Stores the first corner of the copy region.

#### `getCopyStartPos(ItemStack gadget)` (Line 166-168)
Retrieves the copy start position (or `nullPos` if not set).

#### `setCopyEndPos(ItemStack gadget, BlockPos blockPos)` (Line 178-180)
Stores the second corner of the copy region.

#### `getCopyEndPos(ItemStack gadget)` (Line 182-184)
Retrieves the copy end position (or `nullPos` if not set).

**Paste Position Methods**:

#### `setRelativePaste(ItemStack gadget, BlockPos blockPos)` (Line 170-172)
Sets the paste offset.

#### `getRelativePaste(ItemStack gadget)` (Line 174-176)
Gets the paste offset (defaults to `BlockPos.ZERO`).

**Mode Methods**:

#### `getMode(ItemStack stack)` (Line 322-359)
Gets the current gadget mode.

**Default modes by gadget**:
- `GadgetCopyPaste` → "copy"
- `GadgetCutPaste` → "cut"
- `GadgetBuilding` → "build_to_me"
- `GadgetExchanger` → "surface"

**Settings Methods**:

#### `getPasteReplace(ItemStack stack)` (Line 278-286)
Checks if paste should replace existing blocks.
```java
if (!stack.has(BG2DataComponents.SETTING_TOGGLES.get(ToggleableSettings.PASTE_REPLACE))) {
    if (stack.getItem() instanceof GadgetCutPaste)
        return true;  // Default true for cut/paste
    else
        return false; // Default false for copy/paste
}
return getSetting(stack, ToggleableSettings.PASTE_REPLACE.getName());
```

**Constants**:

#### `nullPos` (Line 101)
```java
public final static BlockPos nullPos = new BlockPos(-999, -999, -999);
```
Used to indicate "no position set".

---

### 6. BaseGadget Abstract Class
**Package**: `com.direwolf20.buildinggadgets2.common.items`

**Key Methods**:

#### `use(Level level, Player player, InteractionHand hand)` (Line 122-146)
Main entry point for gadget usage.

**Flow**:
1. Returns early on client side
2. Gets block being looked at
3. Creates `ItemActionContext`
4. Checks for shift key → calls `onShiftAction()` or `onAction()`

#### `getGadget(Player player)` (Line 204-213)
Static utility to get gadget from player's hand.
```java
ItemStack heldItem = player.getMainHandItem();
if (!(heldItem.getItem() instanceof BaseGadget)) {
    heldItem = player.getOffhandItem();
    if (!(heldItem.getItem() instanceof BaseGadget)) {
        return ItemStack.EMPTY;
    }
}
return heldItem;
```

#### `undo(Level level, Player player, ItemStack gadget)` (Line 234-249)
Reverses the last build operation.

**Undo System**:
- Each gadget has an undo list (max 10 operations)
- Each operation has a UUID
- Undo data stored in BG2Data
- Validates chunks are loaded before undoing

---

## Copy Operation Flow

### Complete Copy Sequence

**First Click (Start Position)**:
1. Player right-clicks block with Copy Paste Gadget in "copy" mode
2. `PlayerInteractEvent.RightClickBlock` fires
3. `BaseGadget.use()` called
4. `GadgetCopyPaste.onAction()` called
5. `GadgetNBT.setCopyStartPos(gadget, pos)` stores position
6. `buildAndStore()` called (but returns empty list if end pos not set)

**Second Click (End Position)**:
1. Player shift-right-clicks second block
2. `GadgetCopyPaste.onShiftAction()` called
3. `GadgetNBT.setCopyEndPos(gadget, pos)` stores position
4. `buildAndStore()` called again
5. `Copy.collectWorld()` called with both positions set
6. Validates region size (max 500 per axis, 100k blocks total)
7. Iterates through AABB between start and end
8. Creates `StatePos` for each block (with relative positions)
9. Generates new copy UUID via `GadgetNBT.setCopyUUID()`
10. Stores in `BG2Data.addToCopyPaste(gadgetUUID, buildList)`
11. Displays message to player with block count

---

## Paste Operation Flow

### Complete Paste Sequence

**Paste Click**:
1. Player right-clicks with gadget in "paste" mode
2. `GadgetCopyPaste.onAction()` called
3. Gets gadget UUID: `GadgetNBT.getUUID(gadget)`
4. Retrieves blocks: `BG2Data.getCopyPasteList(uuid, false)`
5. Gets paste position: `getHitPos(context).above()`
6. Applies relative offset: `.offset(GadgetNBT.getRelativePaste(gadget))`
7. Checks paste replace setting
8. Calls `BuildingUtils.build()` or `BuildingUtils.exchange()`
9. Adds operation to undo list

---

## Data Storage Details

### UUID System

**Two Types of UUIDs**:

1. **Gadget UUID** (`GADGET_UUID`)
   - Unique identifier for each gadget item
   - Persists across game sessions
   - Used as key in BG2Data maps
   - Generated once per gadget

2. **Copy UUID** (`COPY_UUID`)
   - Identifies a specific copy operation
   - Changes every time you copy
   - Used to detect if copy data is stale
   - Used for cache invalidation

### World Data Persistence

**BG2Data** extends `SavedData`:
- Stored in world save folder
- Automatically serialized to NBT
- Loaded when world loads
- Shared across all players in the world

**File Location**: `<world>/data/buildinggadgets2.dat`

---

## Coordinate System

### Relative Positions

**Copy Storage**:
Blocks are stored with positions **relative to the copy start position**.

```java
// During copy
BlockPos absolutePos = /* actual world position */;
BlockPos copyStart = GadgetNBT.getCopyStartPos(gadget);
BlockPos relativePos = absolutePos.subtract(copyStart);
statePos = new StatePos(blockState, relativePos);
```

**Paste Application**:
```java
// During paste
BlockPos pasteOrigin = /* where player clicked */;
BlockPos relativeOffset = GadgetNBT.getRelativePaste(gadget);
BlockPos finalPos = pasteOrigin.offset(relativeOffset).offset(statePos.pos);
```

### AABB Construction

```java
AABB area = VecHelpers.aabbFromBlockPos(copyStart, copyEnd);
```

Creates axis-aligned bounding box that includes both corner positions.

---

## Size Limits and Validation

### Copy Limits

**Per-Axis Limit**: 500 blocks
```java
int maxAxis = 500;
if (area.getXsize() > maxAxis) {
    player.displayClientMessage(
        Component.translatable("buildinggadgets2.messages.axistoolarge", 
            "x", maxAxis, area.getXsize()), 
        false
    );
    return coordinates;
}
```

**Total Volume Limit**: 100,000 blocks
```java
int maxSize = 100000;
if (size > maxSize) {
    player.displayClientMessage(
        Component.translatable("buildinggadgets2.messages.areatoolarge", 
            maxSize, size), 
        false
    );
    return coordinates;
}
```

### Block Validation

**Valid Blocks**:
- Must pass `GadgetUtils.isValidBlockState()`
- Cannot be `RenderBlock` (BuildingGadgets2 internal block)
- Air blocks are stored as `Blocks.AIR.defaultBlockState()`

**Invalid Blocks**:
- Replaced with air in the copy
- Still occupy space in the structure

---

## Network Synchronization

### Relevant Packets

**SendCopyDataPayload**:
- Sends copy data from server to client
- Used for rendering preview

**RequestCopyDataPayload**:
- Client requests copy data from server
- Triggered when needed for rendering

**SendCopyDataToServerPayload**:
- Client sends copy data to server
- Used for template operations

**SendPastePayload**:
- Handles paste operations
- Coordinates server-side placement

### Client-Side Cache

**BG2DataClient**:
- Mirrors server data on client
- Used for rendering block previews
- Synchronized via network packets
- Invalidated when copy UUID changes

---

## Rendering System

### Render Types

```java
public enum RenderTypes {
    GROW("buildinggadgets2.grow"),
    FADE("buildinggadgets2.fade"),
    SQUISH("buildinggadgets2.squish"),
    GROWUP("buildinggadgets2.growup"),
    RISEUP("buildinggadgets2.riseup"),
    SNAP("buildinggadgets2.snap");
}
```

**Stored in NBT**:
```java
byte renderType = GadgetNBT.getRenderTypeByte(stack);
RenderTypes type = GadgetNBT.getRenderType(stack);
```

---

## Template System (Redprints)

### Template Storage

**Redprint Lookup**:
```java
private final BiMap<UUID, String> redprintLookup;  // UUID <-> Template name
```

**Key Methods**:

#### `addToRedprints(UUID uuid, String name)` (Line 39-44)
Saves a copy as a named template.

#### `removeFromRedprints(String name)` (Line 46-55)
Deletes a template.

#### `getRedprintUUIDfromName(String name)` (Line 57-61)
Looks up template UUID by name.

**Template Data**:
- Templates use the same `copyPasteLookup` storage
- UUID is associated with a name in `redprintLookup`
- Templates persist across world sessions
- Can be shared between gadgets

---

## Energy System

### Energy Storage

**Forge Energy Integration**:
```java
public abstract int getEnergyMax();
public abstract int getEnergyCost();
```

**Copy Paste Gadget Values** (from Config):
- `COPYPASTEGADGET_MAXPOWER` - Maximum energy storage
- `COPYPASTEGADGET_COST` - Energy per block

**Energy Bar Display**:
```java
@Override
public int getBarWidth(ItemStack stack) {
    var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
    return Math.min(13 * energy.getEnergyStored() / energy.getMaxEnergyStored(), 13);
}

@Override
public int getBarColor(ItemStack stack) {
    var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
    return Mth.hsvToRgb(
        Math.max(0.0F, (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 
        1.0F, 
        1.0F
    );
}
```

---

## Undo System

### Undo List Structure

**Per-Gadget Undo List**:
```java
LinkedList<UUID> undoList = GadgetNBT.getUndoList(gadget);
```

**Maximum Size**: 10 operations

**Adding to Undo**:
```java
public static void addToUndoList(ItemStack gadget, UUID uuid, BG2Data bg2Data) {
    LinkedList<UUID> undoList = getUndoList(gadget);
    if (undoList.size() >= undoListSize) {
        UUID removal = undoList.removeFirst();  // Remove oldest
        bg2Data.removeFromUndoList(removal);
    }
    undoList.add(uuid);
    setUndoList(gadget, undoList);
}
```

**Undo Data Storage**:
```java
private final HashMap<UUID, ArrayList<StatePos>> undoList;  // Build UUID -> Original blocks
```

---

## Tile Entity Handling

### TE Map

**Storage**:
```java
private final HashMap<UUID, ArrayList<TagPos>> teMap;  // Gadget UUID -> Tile entity data
```

**TagPos Class**:
```java
public class TagPos {
    public CompoundTag tag;  // NBT data
    public BlockPos pos;     // Position
}
```

**Methods**:
```java
public void addToTEMap(UUID uuid, ArrayList<TagPos> list);
public ArrayList<TagPos> peekTEMap(UUID uuid);
public ArrayList<TagPos> getTEMap(UUID uuid);  // Removes from map
```

---

## Integration Points for GUI

### Detecting Copy Operations

**Event Listener Approach**:
```java
@SubscribeEvent
public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    Player player = event.getEntity();
    ItemStack heldItem = player.getMainHandItem();
    
    if (heldItem.getItem() instanceof GadgetCopyPaste) {
        if (!event.getLevel().isClientSide()) {
            String mode = GadgetNBT.getMode(heldItem).getId().getPath();
            
            if (mode.equals("copy")) {
                UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
                // Store gadgetUUID to check later
            }
        }
    }
}
```

**Checking for Completion**:
```java
@SubscribeEvent
public static void onServerTick(ServerTickEvent.Post event) {
    ServerLevel overworld = event.getServer().overworld();
    BG2Data bg2Data = BG2Data.get(overworld);
    
    ArrayList<StatePos> copiedBlocks = bg2Data.getCopyPasteList(gadgetUUID, false);
    if (copiedBlocks != null && !copiedBlocks.isEmpty()) {
        // Copy completed!
    }
}
```

### Accessing Copy Data

**Getting Block List**:
```java
BG2Data bg2Data = BG2Data.get(server.overworld());
UUID gadgetUUID = GadgetNBT.getUUID(gadgetStack);
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
```

**Analyzing Blocks**:
```java
// Count blocks
int totalBlocks = blocks.size();

// Get unique block types
ArrayList<BlockState> uniqueBlocks = StatePos.getBlockStateMap(blocks);

// Calculate bounding box
int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

for (StatePos sp : blocks) {
    minX = Math.min(minX, sp.pos.getX());
    maxX = Math.max(maxX, sp.pos.getX());
    minY = Math.min(minY, sp.pos.getY());
    maxY = Math.max(maxY, sp.pos.getY());
    minZ = Math.min(minZ, sp.pos.getZ());
    maxZ = Math.max(maxZ, sp.pos.getZ());
}

int sizeX = maxX - minX + 1;
int sizeY = maxY - minY + 1;
int sizeZ = maxZ - minZ + 1;
```

**Material Counting**:
```java
HashMap<Block, Integer> materials = new HashMap<>();
for (StatePos sp : blocks) {
    Block block = sp.state.getBlock();
    materials.put(block, materials.getOrDefault(block, 0) + 1);
}
```

### GUI Display Ideas

**Copy Statistics**:
- Total block count
- Dimensions (X × Y × Z)
- Unique block types
- Material list with counts
- Volume percentage (used vs. total)

**Copy History**:
- Track recent copies per player
- Show thumbnails/previews
- Allow re-loading old copies

**Template Browser**:
- List all redprints
- Search by name
- Preview template contents
- Load template into gadget

---

## Common Patterns

### Check if Gadget Has Copy Data

```java
UUID copyUUID = GadgetNBT.getCopyUUID(itemStack);
if (copyUUID != null && GadgetNBT.hasCopyUUID(itemStack)) {
    // Gadget has copied data
}
```

### Get Copy Without Removing

```java
// false = don't remove from storage
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(uuid, false);
```

### Iterate Safely

```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(uuid, false);
if (blocks != null && !blocks.isEmpty()) {
    for (StatePos statePos : blocks) {
        BlockState state = statePos.state;
        BlockPos pos = statePos.pos;  // Remember: relative position!
        // Process block
    }
}
```

### Check Gadget Mode

```java
BaseMode mode = GadgetNBT.getMode(itemStack);
String modePath = mode.getId().getPath();

if (modePath.equals("copy")) {
    // Copy mode
} else if (modePath.equals("paste")) {
    // Paste mode
}
```

---

## Performance Considerations

### Large Copies

**100k Block Limit**:
- Maximum copy size is 100,000 blocks
- Iteration can be expensive
- Consider async processing for analysis

**Optimization Tips**:
1. Cache results when possible
2. Use `getBlockStateMap()` to reduce duplicates
3. Limit GUI updates (don't update every tick)
4. Process in chunks for large copies
5. Use `false` parameter in `getCopyPasteList()` to avoid removal

### Network Traffic

**Copy Data Size**:
- Large copies = large NBT packets
- Client-side rendering needs copy data
- Consider pagination for GUI displays

---

## Troubleshooting

### Common Issues

**Copy Returns Null**:
- Gadget hasn't copied anything yet
- Copy was removed from storage
- Wrong UUID being used

**Copy Returns Empty List**:
- Region contains no valid blocks
- All blocks were filtered out
- Start/end positions not set

**Positions Are Wrong**:
- Remember: StatePos positions are **relative**
- Need to add paste origin to get absolute position

**Mode Detection Fails**:
- Check server-side (`!level.isClientSide()`)
- Verify gadget is actually GadgetCopyPaste
- Mode might be "paste" instead of "copy"

---

## Version Compatibility

This reference is based on **BuildingGadgets2 version 1.3.9** for Minecraft 1.21.1 with NeoForge.

**Key Dependencies**:
- NeoForge 21.1.x
- Minecraft 1.21.1

**Breaking Changes to Watch**:
- Data component system (changed from NBT in 1.20.5+)
- Package names (may change in future versions)
- Method signatures (especially in util classes)

---

## Additional Resources

**Related Documentation**:
- `BUILDINGGADGETS2_INTEGRATION.md` - High-level integration guide
- `IMPLEMENTATION_SUMMARY.md` - Current implementation status
- `QUICK_REFERENCE.md` - Quick lookup reference

**Source Files**:
- `GadgetCopyPaste.java` - Main gadget implementation
- `Copy.java` - Copy mode logic
- `BG2Data.java` - Server-side data storage
- `GadgetNBT.java` - NBT utility methods
- `StatePos.java` - Block data structure

---

## Notes

- All copy/paste operations are **server-side only**
- Client receives data via network packets for rendering
- UUIDs are critical for data lookup
- Positions in StatePos are **relative to copy origin**
- BG2Data is world-specific (not global)
- Copy data persists in world save files

