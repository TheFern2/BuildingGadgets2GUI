# Copy Paste Gadget - Code Examples

## Overview
This document provides ready-to-use code examples for common Copy Paste Gadget integration scenarios.

---

## Basic Detection

### Detect Copy Operation Start

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
                BlockPos clickedPos = event.getPos();
                
                System.out.println("Player " + player.getName().getString() + 
                    " started copy at " + clickedPos);
            }
        }
    }
}
```

### Detect Copy Completion

```java
@EventBusSubscriber(modid = YourMod.MODID)
public class CopyDetector {
    private static final HashMap<UUID, UUID> pendingCopies = new HashMap<>();
    
    @SubscribeEvent
    public static void onCopyStart(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        
        if (heldItem.getItem() instanceof GadgetCopyPaste && !event.getLevel().isClientSide()) {
            if (GadgetNBT.getMode(heldItem).getId().getPath().equals("copy")) {
                UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
                pendingCopies.put(gadgetUUID, GadgetNBT.getCopyUUID(heldItem));
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (pendingCopies.isEmpty()) return;
        
        ServerLevel overworld = event.getServer().overworld();
        BG2Data bg2Data = BG2Data.get(overworld);
        
        Iterator<UUID> iterator = pendingCopies.keySet().iterator();
        while (iterator.hasNext()) {
            UUID gadgetUUID = iterator.next();
            ArrayList<StatePos> copiedBlocks = bg2Data.getCopyPasteList(gadgetUUID, false);
            
            if (copiedBlocks != null && !copiedBlocks.isEmpty()) {
                onCopyCompleted(gadgetUUID, copiedBlocks);
                iterator.remove();
            }
        }
    }
    
    private static void onCopyCompleted(UUID gadgetUUID, ArrayList<StatePos> blocks) {
        System.out.println("Copy completed! " + blocks.size() + " blocks copied.");
    }
}
```

---

## Data Analysis

### Get Copy Statistics

```java
public class CopyStats {
    public final int totalBlocks;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final int uniqueBlockTypes;
    public final Map<Block, Integer> materialCounts;
    
    public CopyStats(ArrayList<StatePos> blocks) {
        this.totalBlocks = blocks.size();
        
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        Map<Block, Integer> materials = new HashMap<>();
        
        for (StatePos sp : blocks) {
            minX = Math.min(minX, sp.pos.getX());
            maxX = Math.max(maxX, sp.pos.getX());
            minY = Math.min(minY, sp.pos.getY());
            maxY = Math.max(maxY, sp.pos.getY());
            minZ = Math.min(minZ, sp.pos.getZ());
            maxZ = Math.max(maxZ, sp.pos.getZ());
            
            Block block = sp.state.getBlock();
            materials.put(block, materials.getOrDefault(block, 0) + 1);
        }
        
        this.sizeX = maxX - minX + 1;
        this.sizeY = maxY - minY + 1;
        this.sizeZ = maxZ - minZ + 1;
        this.uniqueBlockTypes = materials.size();
        this.materialCounts = materials;
    }
    
    public int getVolume() {
        return sizeX * sizeY * sizeZ;
    }
    
    public float getDensity() {
        return (float) totalBlocks / getVolume();
    }
    
    public String getDimensionsString() {
        return sizeX + " × " + sizeY + " × " + sizeZ;
    }
}
```

### Usage:

```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
if (blocks != null && !blocks.isEmpty()) {
    CopyStats stats = new CopyStats(blocks);
    
    System.out.println("Dimensions: " + stats.getDimensionsString());
    System.out.println("Total blocks: " + stats.totalBlocks);
    System.out.println("Unique types: " + stats.uniqueBlockTypes);
    System.out.println("Density: " + String.format("%.1f%%", stats.getDensity() * 100));
}
```

---

## Material List Generation

### Generate Material Requirements

```java
public class MaterialList {
    
    public static Map<ItemStack, Integer> getMaterialsNeeded(ArrayList<StatePos> blocks, Level level) {
        Map<ItemStackKey, Integer> itemMap = new HashMap<>();
        
        for (StatePos statePos : blocks) {
            BlockState state = statePos.state;
            
            if (state.isAir()) continue;
            
            ItemStack itemStack = new ItemStack(state.getBlock().asItem());
            
            if (itemStack.isEmpty()) continue;
            
            ItemStackKey key = new ItemStackKey(itemStack, true);
            itemMap.put(key, itemMap.getOrDefault(key, 0) + 1);
        }
        
        Map<ItemStack, Integer> result = new HashMap<>();
        for (Map.Entry<ItemStackKey, Integer> entry : itemMap.entrySet()) {
            result.put(entry.getKey().getItemStack(), entry.getValue());
        }
        
        return result;
    }
    
    public static List<Component> formatMaterialList(Map<ItemStack, Integer> materials) {
        List<Component> lines = new ArrayList<>();
        
        lines.add(Component.literal("Materials Required:").withStyle(ChatFormatting.GOLD));
        
        List<Map.Entry<ItemStack, Integer>> sorted = new ArrayList<>(materials.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        for (Map.Entry<ItemStack, Integer> entry : sorted) {
            ItemStack item = entry.getKey();
            int count = entry.getValue();
            
            Component line = Component.literal("  • ")
                .append(item.getHoverName())
                .append(Component.literal(" × " + count).withStyle(ChatFormatting.GRAY));
            
            lines.add(line);
        }
        
        return lines;
    }
}
```

### Usage:

```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
Map<ItemStack, Integer> materials = MaterialList.getMaterialsNeeded(blocks, level);
List<Component> formatted = MaterialList.formatMaterialList(materials);

for (Component line : formatted) {
    player.sendSystemMessage(line);
}
```

---

## GUI Integration

### Create Copy Info Screen

```java
public class CopyInfoScreen extends Screen {
    private final ItemStack gadget;
    private final ArrayList<StatePos> blocks;
    private final CopyStats stats;
    
    public CopyInfoScreen(ItemStack gadget, ArrayList<StatePos> blocks) {
        super(Component.literal("Copy Information"));
        this.gadget = gadget;
        this.blocks = blocks;
        this.stats = new CopyStats(blocks);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 40;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            button -> this.onClose()
        ).bounds(centerX - 50, this.height - 30, 100, 20).build());
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int y = 20;
        
        graphics.drawCenteredString(this.font, this.title, centerX, y, 0xFFFFFF);
        y += 30;
        
        graphics.drawString(this.font, "Dimensions: " + stats.getDimensionsString(), 
            centerX - 100, y, 0xFFFFFF);
        y += 15;
        
        graphics.drawString(this.font, "Total Blocks: " + stats.totalBlocks, 
            centerX - 100, y, 0xFFFFFF);
        y += 15;
        
        graphics.drawString(this.font, "Unique Types: " + stats.uniqueBlockTypes, 
            centerX - 100, y, 0xFFFFFF);
        y += 15;
        
        graphics.drawString(this.font, 
            String.format("Density: %.1f%%", stats.getDensity() * 100), 
            centerX - 100, y, 0xFFFFFF);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
```

### Open Screen from Event

```java
@OnlyIn(Dist.CLIENT)
public static void openCopyInfoScreen(ItemStack gadget) {
    Minecraft mc = Minecraft.getInstance();
    
    UUID gadgetUUID = GadgetNBT.getUUID(gadget);
    BG2DataClient clientData = BG2DataClient.INSTANCE;
    ArrayList<StatePos> blocks = clientData.getCopyPasteList(gadgetUUID, false);
    
    if (blocks != null && !blocks.isEmpty()) {
        mc.setScreen(new CopyInfoScreen(gadget, blocks));
    }
}
```

---

## Copy History Tracking

### Track Player Copy History

```java
public class CopyHistory {
    private static final Map<UUID, LinkedList<CopyRecord>> playerHistory = new HashMap<>();
    private static final int MAX_HISTORY = 10;
    
    public static class CopyRecord {
        public final UUID copyUUID;
        public final long timestamp;
        public final int blockCount;
        public final String dimensions;
        
        public CopyRecord(UUID copyUUID, ArrayList<StatePos> blocks) {
            this.copyUUID = copyUUID;
            this.timestamp = System.currentTimeMillis();
            this.blockCount = blocks.size();
            
            CopyStats stats = new CopyStats(blocks);
            this.dimensions = stats.getDimensionsString();
        }
        
        public String getTimeAgo() {
            long seconds = (System.currentTimeMillis() - timestamp) / 1000;
            if (seconds < 60) return seconds + "s ago";
            if (seconds < 3600) return (seconds / 60) + "m ago";
            return (seconds / 3600) + "h ago";
        }
    }
    
    public static void addCopy(UUID playerUUID, UUID copyUUID, ArrayList<StatePos> blocks) {
        LinkedList<CopyRecord> history = playerHistory.computeIfAbsent(
            playerUUID, 
            k -> new LinkedList<>()
        );
        
        history.addFirst(new CopyRecord(copyUUID, blocks));
        
        if (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
    }
    
    public static List<CopyRecord> getHistory(UUID playerUUID) {
        return playerHistory.getOrDefault(playerUUID, new LinkedList<>());
    }
    
    public static void clearHistory(UUID playerUUID) {
        playerHistory.remove(playerUUID);
    }
}
```

### Usage:

```java
@SubscribeEvent
public static void onCopyComplete(UUID gadgetUUID, ArrayList<StatePos> blocks, Player player) {
    UUID copyUUID = UUID.randomUUID();
    CopyHistory.addCopy(player.getUUID(), copyUUID, blocks);
    
    List<CopyHistory.CopyRecord> history = CopyHistory.getHistory(player.getUUID());
    player.sendSystemMessage(Component.literal("Copy saved! History: " + history.size()));
}
```

---

## Block Filtering

### Filter Blocks by Type

```java
public class BlockFilter {
    
    public static ArrayList<StatePos> filterByBlock(ArrayList<StatePos> blocks, Block targetBlock) {
        ArrayList<StatePos> filtered = new ArrayList<>();
        for (StatePos sp : blocks) {
            if (sp.state.is(targetBlock)) {
                filtered.add(sp);
            }
        }
        return filtered;
    }
    
    public static ArrayList<StatePos> filterByTag(ArrayList<StatePos> blocks, TagKey<Block> tag) {
        ArrayList<StatePos> filtered = new ArrayList<>();
        for (StatePos sp : blocks) {
            if (sp.state.is(tag)) {
                filtered.add(sp);
            }
        }
        return filtered;
    }
    
    public static ArrayList<StatePos> excludeAir(ArrayList<StatePos> blocks) {
        ArrayList<StatePos> filtered = new ArrayList<>();
        for (StatePos sp : blocks) {
            if (!sp.state.isAir()) {
                filtered.add(sp);
            }
        }
        return filtered;
    }
    
    public static Map<Block, ArrayList<StatePos>> groupByBlock(ArrayList<StatePos> blocks) {
        Map<Block, ArrayList<StatePos>> groups = new HashMap<>();
        for (StatePos sp : blocks) {
            Block block = sp.state.getBlock();
            groups.computeIfAbsent(block, k -> new ArrayList<>()).add(sp);
        }
        return groups;
    }
}
```

### Usage:

```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);

ArrayList<StatePos> stoneBlocks = BlockFilter.filterByBlock(blocks, Blocks.STONE);
System.out.println("Stone blocks: " + stoneBlocks.size());

ArrayList<StatePos> solidBlocks = BlockFilter.excludeAir(blocks);
System.out.println("Solid blocks: " + solidBlocks.size());

Map<Block, ArrayList<StatePos>> grouped = BlockFilter.groupByBlock(blocks);
System.out.println("Block types: " + grouped.size());
```

---

## Copy Comparison

### Compare Two Copies

```java
public class CopyComparator {
    
    public static class ComparisonResult {
        public final int blocksInCommon;
        public final int blocksOnlyInFirst;
        public final int blocksOnlyInSecond;
        public final Map<Block, Integer> commonBlocks;
        
        public ComparisonResult(
            int common, 
            int onlyFirst, 
            int onlySecond, 
            Map<Block, Integer> commonBlocks
        ) {
            this.blocksInCommon = common;
            this.blocksOnlyInFirst = onlyFirst;
            this.blocksOnlyInSecond = onlySecond;
            this.commonBlocks = commonBlocks;
        }
        
        public float getSimilarity() {
            int total = blocksInCommon + blocksOnlyInFirst + blocksOnlyInSecond;
            return total > 0 ? (float) blocksInCommon / total : 0f;
        }
    }
    
    public static ComparisonResult compare(
        ArrayList<StatePos> first, 
        ArrayList<StatePos> second
    ) {
        Map<Block, Integer> firstBlocks = countBlocks(first);
        Map<Block, Integer> secondBlocks = countBlocks(second);
        
        Map<Block, Integer> common = new HashMap<>();
        int commonCount = 0;
        
        for (Map.Entry<Block, Integer> entry : firstBlocks.entrySet()) {
            Block block = entry.getKey();
            if (secondBlocks.containsKey(block)) {
                int count = Math.min(entry.getValue(), secondBlocks.get(block));
                common.put(block, count);
                commonCount += count;
            }
        }
        
        int onlyFirst = first.size() - commonCount;
        int onlySecond = second.size() - commonCount;
        
        return new ComparisonResult(commonCount, onlyFirst, onlySecond, common);
    }
    
    private static Map<Block, Integer> countBlocks(ArrayList<StatePos> blocks) {
        Map<Block, Integer> counts = new HashMap<>();
        for (StatePos sp : blocks) {
            Block block = sp.state.getBlock();
            counts.put(block, counts.getOrDefault(block, 0) + 1);
        }
        return counts;
    }
}
```

### Usage:

```java
ArrayList<StatePos> copy1 = bg2Data.getCopyPasteList(uuid1, false);
ArrayList<StatePos> copy2 = bg2Data.getCopyPasteList(uuid2, false);

CopyComparator.ComparisonResult result = CopyComparator.compare(copy1, copy2);

System.out.println("Similarity: " + String.format("%.1f%%", result.getSimilarity() * 100));
System.out.println("Blocks in common: " + result.blocksInCommon);
System.out.println("Unique to first: " + result.blocksOnlyInFirst);
System.out.println("Unique to second: " + result.blocksOnlyInSecond);
```

---

## Network Synchronization

### Send Copy Data to Client

```java
public class CopyDataSyncPayload implements CustomPacketPayload {
    public static final Type<CopyDataSyncPayload> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(YourMod.MODID, "copy_sync"));
    
    private final UUID gadgetUUID;
    private final CompoundTag copyData;
    
    public CopyDataSyncPayload(UUID gadgetUUID, ArrayList<StatePos> blocks) {
        this.gadgetUUID = gadgetUUID;
        this.copyData = BG2Data.statePosListToNBTMapArray(blocks);
    }
    
    public CopyDataSyncPayload(FriendlyByteBuf buf) {
        this.gadgetUUID = buf.readUUID();
        this.copyData = buf.readNbt();
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(gadgetUUID);
        buf.writeNbt(copyData);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(CopyDataSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ArrayList<StatePos> blocks = BG2Data.statePosListFromNBTMapArray(payload.copyData);
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendSystemMessage(
                    Component.literal("Received copy data: " + blocks.size() + " blocks")
                );
            }
        });
    }
}
```

### Register Packet

```java
@SubscribeEvent
public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
    IPayloadRegistrar registrar = event.registrar(YourMod.MODID);
    
    registrar.playToClient(
        CopyDataSyncPayload.TYPE,
        CopyDataSyncPayload::new,
        handler -> handler.client(CopyDataSyncPayload::handle)
    );
}
```

---

## Command Integration

### Create Copy Info Command

```java
public class CopyCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("copyinfo")
                .executes(CopyCommands::showCopyInfo)
        );
    }
    
    private static int showCopyInfo(CommandContext<CommandSourceStack> context) {
        try {
            Player player = context.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();
            
            if (!(heldItem.getItem() instanceof GadgetCopyPaste)) {
                context.getSource().sendFailure(
                    Component.literal("You must be holding a Copy Paste Gadget!")
                );
                return 0;
            }
            
            UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
            BG2Data bg2Data = BG2Data.get(context.getSource().getLevel().getServer().overworld());
            ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
            
            if (blocks == null || blocks.isEmpty()) {
                context.getSource().sendFailure(
                    Component.literal("No copy data found!")
                );
                return 0;
            }
            
            CopyStats stats = new CopyStats(blocks);
            
            context.getSource().sendSuccess(() -> 
                Component.literal("=== Copy Information ===").withStyle(ChatFormatting.GOLD), 
                false
            );
            context.getSource().sendSuccess(() -> 
                Component.literal("Dimensions: " + stats.getDimensionsString()), 
                false
            );
            context.getSource().sendSuccess(() -> 
                Component.literal("Total Blocks: " + stats.totalBlocks), 
                false
            );
            context.getSource().sendSuccess(() -> 
                Component.literal("Unique Types: " + stats.uniqueBlockTypes), 
                false
            );
            context.getSource().sendSuccess(() -> 
                Component.literal(String.format("Density: %.1f%%", stats.getDensity() * 100)), 
                false
            );
            
            return 1;
            
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }
}
```

### Register Command

```java
@SubscribeEvent
public static void onRegisterCommands(RegisterCommandsEvent event) {
    CopyCommands.register(event.getDispatcher());
}
```

---

## Utility Methods

### Safe Copy Data Access

```java
public class CopyUtils {
    
    public static Optional<ArrayList<StatePos>> getCopyData(ItemStack gadget, Level level) {
        if (!(gadget.getItem() instanceof GadgetCopyPaste)) {
            return Optional.empty();
        }
        
        if (level.isClientSide()) {
            return Optional.empty();
        }
        
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        BG2Data bg2Data = BG2Data.get(level.getServer().overworld());
        ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
        
        if (blocks == null || blocks.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(blocks);
    }
    
    public static boolean hasCopyData(ItemStack gadget) {
        if (!(gadget.getItem() instanceof GadgetCopyPaste)) {
            return false;
        }
        
        return GadgetNBT.hasCopyUUID(gadget);
    }
    
    public static int getCopySize(ItemStack gadget, Level level) {
        return getCopyData(gadget, level)
            .map(ArrayList::size)
            .orElse(0);
    }
}
```

### Usage:

```java
Optional<ArrayList<StatePos>> copyData = CopyUtils.getCopyData(gadget, level);
copyData.ifPresent(blocks -> {
    System.out.println("Found " + blocks.size() + " blocks");
});

if (CopyUtils.hasCopyData(gadget)) {
    int size = CopyUtils.getCopySize(gadget, level);
    player.sendSystemMessage(Component.literal("Copy size: " + size));
}
```

---

## Performance Optimization

### Async Copy Analysis

```java
public class AsyncCopyAnalyzer {
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public static CompletableFuture<CopyStats> analyzeAsync(ArrayList<StatePos> blocks) {
        return CompletableFuture.supplyAsync(() -> new CopyStats(blocks), executor);
    }
    
    public static CompletableFuture<Map<ItemStack, Integer>> getMaterialsAsync(
        ArrayList<StatePos> blocks, 
        Level level
    ) {
        return CompletableFuture.supplyAsync(
            () -> MaterialList.getMaterialsNeeded(blocks, level), 
            executor
        );
    }
    
    public static void shutdown() {
        executor.shutdown();
    }
}
```

### Usage:

```java
ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);

AsyncCopyAnalyzer.analyzeAsync(blocks).thenAccept(stats -> {
    Minecraft.getInstance().execute(() -> {
        player.sendSystemMessage(
            Component.literal("Analysis complete: " + stats.getDimensionsString())
        );
    });
});
```

---

## Error Handling

### Robust Copy Access

```java
public class SafeCopyAccess {
    
    public static Result<ArrayList<StatePos>> getCopy(ItemStack gadget, Level level) {
        if (gadget.isEmpty()) {
            return Result.error("No item in hand");
        }
        
        if (!(gadget.getItem() instanceof GadgetCopyPaste)) {
            return Result.error("Not a Copy Paste Gadget");
        }
        
        if (level.isClientSide()) {
            return Result.error("Cannot access copy data on client");
        }
        
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        BG2Data bg2Data = BG2Data.get(level.getServer().overworld());
        ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(gadgetUUID, false);
        
        if (blocks == null || blocks.isEmpty()) {
            return Result.error("No copy data found");
        }
        
        return Result.success(blocks);
    }
    
    public static class Result<T> {
        private final T value;
        private final String error;
        private final boolean success;
        
        private Result(T value, String error, boolean success) {
            this.value = value;
            this.error = error;
            this.success = success;
        }
        
        public static <T> Result<T> success(T value) {
            return new Result<>(value, null, true);
        }
        
        public static <T> Result<T> error(String error) {
            return new Result<>(null, error, false);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public T getValue() {
            return value;
        }
        
        public String getError() {
            return error;
        }
        
        public void ifSuccess(Consumer<T> consumer) {
            if (success) {
                consumer.accept(value);
            }
        }
        
        public void ifError(Consumer<String> consumer) {
            if (!success) {
                consumer.accept(error);
            }
        }
    }
}
```

### Usage:

```java
SafeCopyAccess.Result<ArrayList<StatePos>> result = SafeCopyAccess.getCopy(gadget, level);

result.ifSuccess(blocks -> {
    System.out.println("Got " + blocks.size() + " blocks");
});

result.ifError(error -> {
    player.sendSystemMessage(Component.literal("Error: " + error).withStyle(ChatFormatting.RED));
});
```

---

## Testing Utilities

### Mock Copy Data

```java
public class CopyTestUtils {
    
    public static ArrayList<StatePos> createTestCopy(int sizeX, int sizeY, int sizeZ) {
        ArrayList<StatePos> blocks = new ArrayList<>();
        
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockState state = (x + y + z) % 2 == 0 
                        ? Blocks.STONE.defaultBlockState() 
                        : Blocks.DIRT.defaultBlockState();
                    
                    blocks.add(new StatePos(state, new BlockPos(x, y, z)));
                }
            }
        }
        
        return blocks;
    }
    
    public static void printCopyInfo(ArrayList<StatePos> blocks) {
        System.out.println("=== Copy Info ===");
        System.out.println("Total blocks: " + blocks.size());
        
        CopyStats stats = new CopyStats(blocks);
        System.out.println("Dimensions: " + stats.getDimensionsString());
        System.out.println("Unique types: " + stats.uniqueBlockTypes);
        System.out.println("Density: " + String.format("%.1f%%", stats.getDensity() * 100));
        
        System.out.println("\nMaterial breakdown:");
        for (Map.Entry<Block, Integer> entry : stats.materialCounts.entrySet()) {
            System.out.println("  " + entry.getKey().getName().getString() + ": " + entry.getValue());
        }
    }
}
```

### Usage:

```java
ArrayList<StatePos> testCopy = CopyTestUtils.createTestCopy(10, 10, 10);
CopyTestUtils.printCopyInfo(testCopy);
```

---

## Complete Example: Copy Monitor

```java
@EventBusSubscriber(modid = YourMod.MODID)
public class CopyMonitor {
    private static final HashMap<UUID, MonitorData> activeMonitors = new HashMap<>();
    
    private static class MonitorData {
        UUID gadgetUUID;
        UUID oldCopyUUID;
        long startTime;
    }
    
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        
        if (heldItem.getItem() instanceof GadgetCopyPaste && !event.getLevel().isClientSide()) {
            if (GadgetNBT.getMode(heldItem).getId().getPath().equals("copy")) {
                UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
                
                MonitorData data = new MonitorData();
                data.gadgetUUID = gadgetUUID;
                data.oldCopyUUID = GadgetNBT.getCopyUUID(heldItem);
                data.startTime = System.currentTimeMillis();
                
                activeMonitors.put(player.getUUID(), data);
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (activeMonitors.isEmpty()) return;
        
        ServerLevel overworld = event.getServer().overworld();
        BG2Data bg2Data = BG2Data.get(overworld);
        
        Iterator<Map.Entry<UUID, MonitorData>> iterator = activeMonitors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MonitorData> entry = iterator.next();
            UUID playerUUID = entry.getKey();
            MonitorData data = entry.getValue();
            
            ArrayList<StatePos> blocks = bg2Data.getCopyPasteList(data.gadgetUUID, false);
            
            if (blocks != null && !blocks.isEmpty()) {
                long duration = System.currentTimeMillis() - data.startTime;
                onCopyCompleted(event.getServer(), playerUUID, blocks, duration);
                iterator.remove();
            }
            
            if (System.currentTimeMillis() - data.startTime > 30000) {
                iterator.remove();
            }
        }
    }
    
    private static void onCopyCompleted(
        MinecraftServer server, 
        UUID playerUUID, 
        ArrayList<StatePos> blocks,
        long durationMs
    ) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player == null) return;
        
        CopyStats stats = new CopyStats(blocks);
        
        player.sendSystemMessage(Component.literal("=== Copy Complete ===").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Time: " + durationMs + "ms"));
        player.sendSystemMessage(Component.literal("Blocks: " + stats.totalBlocks));
        player.sendSystemMessage(Component.literal("Dimensions: " + stats.getDimensionsString()));
        player.sendSystemMessage(Component.literal("Types: " + stats.uniqueBlockTypes));
        
        CopyHistory.addCopy(playerUUID, UUID.randomUUID(), blocks);
    }
}
```

---

## Notes

- All server-side operations should check `!level.isClientSide()`
- Always handle null/empty copy data gracefully
- Use `false` parameter in `getCopyPasteList()` to avoid removing data
- Remember that StatePos positions are relative to copy origin
- Consider async processing for large copies (100k+ blocks)
- Cache analysis results when possible to avoid recomputation

