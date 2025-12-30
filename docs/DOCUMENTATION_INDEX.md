# BuildingGadgets2GUI Documentation Index

## Overview

This is the main documentation hub for the BuildingGadgets2GUI mod. All documentation is organized in the `dev/` directory.

---

## 📚 Documentation Library

### Developer Documentation (`dev/`)

The `dev/` directory contains comprehensive technical documentation for integrating with BuildingGadgets2's Copy Paste Gadget.

**Start here**: [dev/README.md](dev/README.md)

---

## 📖 Documentation Files

### Quick Reference
**File**: [dev/QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md)  
**Purpose**: Fast lookups and common patterns  
**Use when**: You need a quick code snippet or method signature

**Contains**:
- Quick facts about Copy Paste Gadget
- Key information access patterns
- All BuildingGadgets2 gadgets list
- Common code patterns
- Import statements
- Troubleshooting checklist
- Performance tips
- Example use cases

---

### Integration Guide
**File**: [dev/BUILDINGGADGETS2_INTEGRATION.md](dev/BUILDINGGADGERS2_INTEGRATION.md)  
**Purpose**: High-level architecture and integration approaches  
**Use when**: Starting a new integration or understanding the system

**Contains**:
- Copy Paste Gadget architecture
- Key classes overview
- How copy operations work
- Network packets
- Events system options
- Current implementation approach
- Future enhancement ideas

---

### Internal Reference
**File**: [dev/COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md)  
**Purpose**: Detailed implementation documentation  
**Use when**: You need deep technical details about specific features

**Contains**:
- Complete class documentation
- Method-by-method breakdowns with line numbers
- Copy/paste operation flows
- UUID system details
- Coordinate system (relative positions)
- Data storage and persistence
- Size limits and validation
- Network synchronization
- Template system (Redprints)
- Energy system
- Undo system
- Tile entity handling
- Integration points
- Performance considerations

---

### Code Examples
**File**: [dev/CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md)  
**Purpose**: Ready-to-use code snippets and complete examples  
**Use when**: You want to copy-paste working code for common tasks

**Contains**:
- Basic copy detection
- Copy completion detection
- Data analysis utilities
- Material list generation
- GUI integration examples
- Copy history tracking
- Block filtering
- Copy comparison
- Network synchronization
- Command integration
- Error handling patterns
- Testing utilities
- Complete working examples

---

### Implementation Summary
**File**: [dev/IMPLEMENTATION_SUMMARY.md](dev/IMPLEMENTATION_SUMMARY.md)  
**Purpose**: Current implementation status and testing guide  
**Use when**: You want to know what's already implemented

**Contains**:
- What was implemented
- Files created/modified
- How the current system works
- Example output
- Key classes used
- What you can do with this
- Next steps
- Testing instructions
- Troubleshooting

---

## 🚀 Quick Start

### For New Developers

1. **Read**: [dev/README.md](dev/README.md) - Start here for navigation
2. **Understand**: [dev/BUILDINGGADGETS2_INTEGRATION.md](dev/BUILDINGGADGERS2_INTEGRATION.md) - Learn the architecture
3. **Reference**: [dev/QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) - Bookmark for quick lookups
4. **Code**: [dev/CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md) - Copy working examples
5. **Deep Dive**: [dev/COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) - When you need details

### For Specific Tasks

| Task | Primary Document | Supporting Documents |
|------|-----------------|---------------------|
| Detect copy operations | [CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md) → Basic Detection | [BUILDINGGADGERS2_INTEGRATION.md](dev/BUILDINGGADGERS2_INTEGRATION.md) → Events |
| Analyze copy data | [CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md) → Data Analysis | [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → StatePos |
| Create GUI features | [CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md) → GUI Integration | [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → Integration Points |
| Work with templates | [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → Template System | [QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) |
| Debug issues | [QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) → Troubleshooting | [IMPLEMENTATION_SUMMARY.md](dev/IMPLEMENTATION_SUMMARY.md) |

---

## 🔑 Key Concepts

### UUID System
Two types of UUIDs are used:
- **Gadget UUID**: Persistent identifier for each gadget item
- **Copy UUID**: Changes with each copy operation

📖 Details: [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → UUID System

### Relative Positions
Copied blocks use positions **relative to the copy start**, not absolute world coordinates.

📖 Details: [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → Coordinate System

### Server-Side Operations
All copy/paste data must be accessed server-side. Clients receive data via network packets.

📖 Details: [BUILDINGGADGERS2_INTEGRATION.md](dev/BUILDINGGADGERS2_INTEGRATION.md) → Network Packets

### BG2Data Storage
Copy data persists in world save files via the `BG2Data` class.

📖 Details: [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → BG2Data

---

## 📋 Common Code Patterns

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

📖 More examples: [CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md)

---

## 🔧 Essential Imports

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

📖 Full list: [QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) → Imports

---

## 🐛 Troubleshooting

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
- Verify gadget is GadgetCopyPaste
- Mode might be "paste" instead of "copy"

📖 Full troubleshooting: [QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) → Troubleshooting Checklist

---

## ⚡ Performance Tips

1. Don't query BG2Data every tick - only when needed
2. Cache gadget UUIDs instead of recalculating
3. Use `getCopyPasteList(uuid, false)` to avoid removing data
4. Limit block iteration for large copies
5. Use async processing for 100k+ block copies

📖 More tips: [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) → Performance Considerations

---

## 📦 Version Information

**BuildingGadgets2**: 1.3.9  
**Minecraft**: 1.21.1  
**NeoForge**: 21.1.x

**Note**: Documentation is based on BuildingGadgets2 1.3.9. Future versions may have breaking changes.

---

## 📂 Documentation Structure

```
docs/
├── DOCUMENTATION_INDEX.md (this file)
└── dev/
    ├── README.md
    ├── QUICK_REFERENCE.md
    ├── BUILDINGGADGERS2_INTEGRATION.md
    ├── COPYPASTE_INTERNAL_REFERENCE.md
    ├── CODE_EXAMPLES.md
    └── IMPLEMENTATION_SUMMARY.md
```

---

## 🎯 Documentation Goals

This documentation aims to:
- ✅ Provide comprehensive coverage of Copy Paste Gadget integration
- ✅ Offer ready-to-use code examples
- ✅ Explain internal implementation details
- ✅ Enable quick lookups and references
- ✅ Support both beginners and advanced developers

---

## 🤝 Contributing

When adding new documentation:
1. Keep examples practical and tested
2. Include code comments for clarity
3. Reference line numbers for source code
4. Update indexes and cross-references
5. Follow the existing structure

---

## 📞 Getting Help

Can't find what you need?

1. Check [QUICK_REFERENCE.md](dev/QUICK_REFERENCE.md) troubleshooting
2. Search [CODE_EXAMPLES.md](dev/CODE_EXAMPLES.md) for similar use cases
3. Review [COPYPASTE_INTERNAL_REFERENCE.md](dev/COPYPASTE_INTERNAL_REFERENCE.md) for details
4. Examine BuildingGadgets2 source code directly

---

## 📝 License

This documentation is part of the BuildingGadgets2GUI project.  
BuildingGadgets2 is a separate project with its own license.

---

**Last Updated**: December 30, 2025  
**Documentation Version**: 1.0  
**BuildingGadgets2 Version**: 1.3.9

