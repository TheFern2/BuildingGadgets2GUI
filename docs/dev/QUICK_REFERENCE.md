# BuildingGadgets2 Copy Paste Gadget - Quick Reference

## Quick Facts

### Copy Paste Gadget Modes
- **Copy Mode**: Click to set start, Shift+Click to set end and copy
- **Paste Mode**: Click to paste the copied blocks

### Key Information Access

```java
// Get the gadget's unique ID
UUID gadgetUUID = GadgetNBT.getUUID(itemStack);

// Get current mode ("copy" or "paste")
String mode = GadgetNBT.getMode(itemStack).getId().getPath();

// Get the copy operation UUID (changes each copy)
UUID copyUUID = GadgetNBT.getCopyUUID(itemStack);

// Access copied blocks (server-side only)
BG2Data bg2Data = BG2Data.get(server.overworld());
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);

// Get copy start/end positions
BlockPos startPos = GadgetNBT.getCopyStartPos(itemStack);
BlockPos endPos = GadgetNBT.getCopyEndPos(itemStack);
```

### StatePos Class

```java
public class StatePos {
    public BlockPos pos;        // Position relative to copy origin
    public BlockState state;    // The block state
}
```

### Useful Events

```java
// Detect when player uses the gadget
@SubscribeEvent
public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
    if (event.getEntity().getMainHandItem().getItem() instanceof GadgetCopyPaste) {
        // Player is using Copy Paste Gadget
    }
}

// Access data after copy completes
@SubscribeEvent
public static void onServerTick(ServerTickEvent.Post event) {
    BG2Data data = BG2Data.get(event.getServer().overworld());
    // Check for new copy operations
}
```

## All BuildingGadgets2 Gadgets

1. **Building Gadget** - Places blocks in patterns
   - Class: `GadgetBuilding`
   - Target: `GadgetTarget.BUILDING`

2. **Exchanging Gadget** - Replaces blocks
   - Class: `GadgetExchanging`
   - Target: `GadgetTarget.EXCHANGING`

3. **Copy Paste Gadget** - Copies and pastes structures
   - Class: `GadgetCopyPaste`
   - Target: `GadgetTarget.COPYPASTE`

4. **Cut Paste Gadget** - Moves structures (removes + pastes)
   - Class: `GadgetCutPaste`
   - Target: `GadgetTarget.CUTPASTE`

5. **Destruction Gadget** - Removes blocks
   - Class: `GadgetDestruction`
   - Target: `GadgetTarget.DESTRUCTION`

## Common Patterns

### Check if player is holding any gadget
```java
ItemStack held = player.getMainHandItem();
if (held.getItem() instanceof BaseGadget) {
    BaseGadget gadget = (BaseGadget) held.getItem();
    GadgetTarget target = gadget.gadgetTarget();
    // Do something based on gadget type
}
```

### Get copied blocks count without loading all data
```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(uuid, false);
int count = blocks != null ? blocks.size() : 0;
```

### Check if gadget has copied data
```java
UUID copyUUID = GadgetNBT.getCopyUUID(itemStack);
if (copyUUID != null) {
    // Gadget has copied data
}
```

### Iterate through copied blocks
```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(uuid, false);
if (blocks != null) {
    for (StatePos statePos : blocks) {
        BlockState state = statePos.state;
        BlockPos pos = statePos.pos;  // Relative position
        // Process block
    }
}
```

## Important Notes

- **Server-Side Only**: BG2Data is only available on the server
- **Relative Positions**: StatePos.pos is relative to the copy origin
- **UUID Persistence**: Gadget UUIDs persist across game sessions
- **Copy UUID Changes**: A new copy UUID is generated each time you copy
- **No Custom Events**: BuildingGadgets2 doesn't fire custom events
- **World Data**: Copy data is stored in world save data

## Dependency Setup

### build.gradle
```groovy
dependencies {
    compileOnly files("run/mods/buildinggadgets2-1.3.9.jar")
    localRuntime files("run/mods/buildinggadgets2-1.3.9.jar")
}
```

## Imports You'll Need

```java
// Items
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;

// Data
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;

// Utilities
import com.direwolf20.buildinggadgets2.util.GadgetNBT;

// API
import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
```

## Troubleshooting Checklist

- [ ] BuildingGadgets2 jar is in run/mods folder
- [ ] Dependency is added to build.gradle
- [ ] Gradle refresh completed successfully
- [ ] Event listener is registered with @EventBusSubscriber
- [ ] Checking server-side (!level.isClientSide())
- [ ] Gadget is in correct mode (copy vs paste)
- [ ] Logging level is set to INFO or DEBUG

## Performance Tips

1. **Don't query BG2Data every tick** - Only check when needed
2. **Cache gadget UUIDs** - Store them instead of recalculating
3. **Use false parameter** - `getCopyPasteList(uuid, false)` doesn't remove data
4. **Limit block iteration** - Process only first N blocks if just checking
5. **Async processing** - For large copies, process blocks asynchronously

## Example Use Cases

### 1. Block Counter
Count specific block types in a copy:
```java
int stoneCount = 0;
for (StatePos sp : blocks) {
    if (sp.state.is(Blocks.STONE)) {
        stoneCount++;
    }
}
```

### 2. Size Calculator
Calculate the bounding box of copied blocks:
```java
int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

for (StatePos sp : blocks) {
    minX = Math.min(minX, sp.pos.getX());
    maxX = Math.max(maxX, sp.pos.getX());
    // ... same for Y and Z
}

int sizeX = maxX - minX + 1;
int sizeY = maxY - minY + 1;
int sizeZ = maxZ - minZ + 1;
```

### 3. Material List
Create a list of required materials:
```java
HashMap<Block, Integer> materials = new HashMap<>();
for (StatePos sp : blocks) {
    Block block = sp.state.getBlock();
    materials.put(block, materials.getOrDefault(block, 0) + 1);
}
```

