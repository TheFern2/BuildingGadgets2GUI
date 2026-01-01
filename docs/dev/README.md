# BuildingGadgets2GUI Developer Documentation

## Overview

This directory contains comprehensive documentation for integrating with BuildingGadgets2's Copy Paste Gadget.

---

## Documentation Files

### 1. [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
**Quick lookup reference for common operations**

Use this when you need:
- Quick code snippets
- Method signatures
- Import statements
- Common patterns
- Troubleshooting checklist

**Best for**: Quick lookups during development

---

### 2. [BUILDINGGADGETS2_INTEGRATION.md](BUILDINGGADGETS2_INTEGRATION.md)
**High-level integration guide**

Covers:
- Architecture overview
- Key classes and their roles
- How copy operations work
- Event system options
- Network packets
- Integration approaches

**Best for**: Understanding the system architecture

---

### 3. [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md)
**Detailed internal implementation reference**

Comprehensive coverage of:
- Complete class documentation with line numbers
- Method-by-method breakdowns
- Data structures and storage
- Copy/paste operation flows
- UUID system
- Coordinate system (relative positions)
- Size limits and validation
- Network synchronization
- Template system
- Energy system
- Undo system
- Tile entity handling

**Best for**: Deep dives into specific features

---

### 4. [CODE_EXAMPLES.md](CODE_EXAMPLES.md)
**Ready-to-use code examples**

Includes:
- Copy detection patterns
- Data analysis utilities
- Material list generation
- GUI integration examples
- Copy history tracking
- Block filtering
- Copy comparison
- Network synchronization
- Command integration
- Error handling
- Testing utilities
- Complete working examples

**Best for**: Copy-paste solutions for common tasks

---

### 5. [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
**Current implementation status**

Documents:
- What has been implemented
- Files created/modified
- How the current system works
- Example output
- Testing instructions
- Next steps

**Best for**: Understanding what's already done

---

## Quick Start Guide

### For New Developers

1. **Start here**: Read [BUILDINGGADGETS2_INTEGRATION.md](BUILDINGGADGETS2_INTEGRATION.md) to understand the architecture
2. **Reference**: Use [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for quick lookups
3. **Examples**: Copy code from [CODE_EXAMPLES.md](CODE_EXAMPLES.md) for your features
4. **Deep dive**: Consult [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) when you need details

### For Specific Tasks

#### Detecting Copy Operations
1. Check [CODE_EXAMPLES.md](CODE_EXAMPLES.md) → "Basic Detection"
2. Reference [BUILDINGGADGETS2_INTEGRATION.md](BUILDINGGADGETS2_INTEGRATION.md) → "Events System"
3. See current implementation in [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)

#### Analyzing Copy Data
1. Use examples from [CODE_EXAMPLES.md](CODE_EXAMPLES.md) → "Data Analysis"
2. Understand data structure in [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "StatePos Data Class"
3. Check [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for method signatures

#### Creating GUI Features
1. Start with [CODE_EXAMPLES.md](CODE_EXAMPLES.md) → "GUI Integration"
2. Understand data access in [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "Integration Points"
3. Reference [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for imports and patterns

#### Working with Templates
1. Read [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "Template System"
2. Check [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for method signatures
3. Adapt examples from [CODE_EXAMPLES.md](CODE_EXAMPLES.md)

---

## Key Concepts

### UUID System
BuildingGadgets2 uses two types of UUIDs:
- **Gadget UUID**: Unique identifier for each gadget item (persistent)
- **Copy UUID**: Identifies a specific copy operation (changes each copy)

See: [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "UUID System"

### Relative Positions
Copied blocks are stored with positions **relative to the copy start position**, not absolute world coordinates.

See: [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "Coordinate System"

### Server-Side Only
All copy/paste data operations must be done server-side. Client receives data via network packets.

See: [BUILDINGGADGETS2_INTEGRATION.md](BUILDINGGADGETS2_INTEGRATION.md) → "Network Packets"

### BG2Data Storage
Copy data is stored in world save data via the `BG2Data` class, persisting across game sessions.

See: [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "BG2Data"

---

## Common Imports

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

See: [QUICK_REFERENCE.md](QUICK_REFERENCE.md) → "Imports You'll Need"

---

## Essential Code Patterns

### Get Copy Data

```java
BG2Data bg2Data = BG2Data.get(server.overworld());
UUID gadgetUUID = GadgetNBT.getUUID(gadgetStack);
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
```

### Check Gadget Mode

```java
String mode = GadgetNBT.getMode(itemStack).getId().getPath();
if (mode.equals("copy")) {
    // Copy mode
}
```

### Detect Copy Completion

```java
@SubscribeEvent
public static void onServerTick(ServerTickEvent.Post event) {
    BG2Data bg2Data = BG2Data.get(event.getServer().overworld());
    ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
    if (blocks != null && !blocks.isEmpty()) {
        // Copy completed!
    }
}
```

See: [CODE_EXAMPLES.md](CODE_EXAMPLES.md) for complete working examples

---

## Troubleshooting

### Copy Returns Null
- Gadget hasn't copied anything yet
- Copy was removed from storage
- Wrong UUID being used

### Copy Returns Empty List
- Region contains no valid blocks
- All blocks were filtered out
- Start/end positions not set

### Mode Detection Fails
- Check server-side (`!level.isClientSide()`)
- Verify gadget is actually GadgetCopyPaste
- Mode might be "paste" instead of "copy"

See: [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) → "Troubleshooting"

---

## Performance Tips

1. **Don't query BG2Data every tick** - Only check when needed
2. **Cache gadget UUIDs** - Store them instead of recalculating
3. **Use false parameter** - `getCopyPasteList(uuid, false)` doesn't remove data
4. **Limit block iteration** - Process only first N blocks if just checking
5. **Async processing** - For large copies, process blocks asynchronously

See: [CODE_EXAMPLES.md](CODE_EXAMPLES.md) → "Performance Optimization"

---

## Version Information

**BuildingGadgets2**: 1.3.9  
**Minecraft**: 1.21.1  
**NeoForge**: 21.1.x

**Note**: This documentation is based on BuildingGadgets2 version 1.3.9. Future versions may have breaking changes.

---

## Additional Resources

### Source Files (BuildingGadgets2)
- `GadgetCopyPaste.java` - Main gadget implementation
- `Copy.java` - Copy mode logic
- `BG2Data.java` - Server-side data storage
- `GadgetNBT.java` - NBT utility methods
- `StatePos.java` - Block data structure
- `BaseGadget.java` - Base gadget class

### Current Implementation (BuildingGadgets2GUI)
- `CopyPasteEventListener.java` - Event listener for copy detection
- `build.gradle` - Dependency configuration

---

## Contributing

When adding new documentation:
1. Keep examples practical and tested
2. Include code comments for clarity
3. Reference line numbers for source code
4. Update this README with new sections
5. Cross-reference related documentation

---

## Documentation Structure

```
docs/dev/
├── README.md (this file)
├── QUICK_REFERENCE.md
├── BUILDINGGADGETS2_INTEGRATION.md
├── COPYPASTE_INTERNAL_REFERENCE.md
├── CODE_EXAMPLES.md
└── IMPLEMENTATION_SUMMARY.md
```

---

## Getting Help

If you can't find what you need:
1. Check the [QUICK_REFERENCE.md](QUICK_REFERENCE.md) troubleshooting section
2. Search [CODE_EXAMPLES.md](CODE_EXAMPLES.md) for similar use cases
3. Review [COPYPASTE_INTERNAL_REFERENCE.md](COPYPASTE_INTERNAL_REFERENCE.md) for implementation details
4. Examine the BuildingGadgets2 source code directly

---

## License

This documentation is part of the BuildingGadgets2GUI project.  
BuildingGadgets2 is a separate project with its own license.

