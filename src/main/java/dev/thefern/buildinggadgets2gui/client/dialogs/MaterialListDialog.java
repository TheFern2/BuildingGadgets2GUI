package dev.thefern.buildinggadgets2gui.client.dialogs;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialListDialog extends Screen {
    
    private static final int DIALOG_WIDTH = 320;
    private static final int DIALOG_HEIGHT = 280;
    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 40;
    private static final int ICON_SIZE = 16;
    
    private final Screen parent;
    private final ArrayList<StatePos> blocks;
    private final String title;
    
    private List<MaterialEntry> materials;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int maxVisibleRows = 0;
    
    private int dialogX;
    private int dialogY;
    
    public MaterialListDialog(Screen parent, String title, ArrayList<StatePos> blocks) {
        super(Component.literal(title));
        this.parent = parent;
        this.blocks = blocks;
        this.title = title;
        calculateMaterials();
    }
    
    private void calculateMaterials() {
        materials = new ArrayList<>();
        
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        Map<Block, Integer> blockCounts = new HashMap<>();
        
        for (StatePos statePos : blocks) {
            BlockState state = statePos.state;
            if (state != null && !state.isAir()) {
                blockCounts.merge(state.getBlock(), 1, Integer::sum);
            }
        }
        
        for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
            Block block = entry.getKey();
            int count = entry.getValue();
            String name = block.getName().getString();
            materials.add(new MaterialEntry(name, count, block));
        }
        
        materials.sort(Comparator.comparingInt((MaterialEntry e) -> e.count).reversed()
            .thenComparing(e -> e.name));
    }
    
    @Override
    protected void init() {
        dialogX = (this.width - DIALOG_WIDTH) / 2;
        dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        int listHeight = DIALOG_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT;
        maxVisibleRows = listHeight / ROW_HEIGHT;
        maxScrollOffset = Math.max(0, materials.size() - maxVisibleRows);
        
        Button closeButton = Button.builder(
            Component.literal("Close"),
            button -> onClose()
        )
        .bounds(dialogX + (DIALOG_WIDTH - 80) / 2, dialogY + DIALOG_HEIGHT - 30, 80, 20)
        .build();
        this.addRenderableWidget(closeButton);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0000000);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF2A2A2A);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + 2, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY + DIALOG_HEIGHT - 2, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY, dialogX + 2, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX + DIALOG_WIDTH - 2, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        
        int listX = dialogX + 10;
        int listY = dialogY + HEADER_HEIGHT;
        int listWidth = DIALOG_WIDTH - 20;
        int listHeight = DIALOG_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT;
        
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1A1A);
        
        if (materials.size() > maxVisibleRows) {
            int scrollbarX = listX + listWidth - 6;
            int scrollbarHeight = listHeight - 4;
            int scrollbarY = listY + 2;
            
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF333333);
            
            float scrollRatio = (float) scrollOffset / maxScrollOffset;
            int thumbHeight = Math.max(20, (int) ((float) maxVisibleRows / materials.size() * scrollbarHeight));
            int thumbY = scrollbarY + (int) (scrollRatio * (scrollbarHeight - thumbHeight));
            
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF666666);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
        
        graphics.drawString(
            this.font,
            title,
            dialogX + 10,
            dialogY + 10,
            0xFFFFFF,
            true
        );
        
        int totalBlocks = blocks != null ? blocks.size() : 0;
        int uniqueTypes = materials.size();
        String summary = totalBlocks + " blocks, " + uniqueTypes + " types";
        int summaryWidth = this.font.width(summary);
        graphics.drawString(
            this.font,
            summary,
            dialogX + DIALOG_WIDTH - summaryWidth - 10,
            dialogY + 10,
            0xAAAAAA,
            true
        );
        
        if (materials.isEmpty()) {
            String emptyText = "No materials";
            int textWidth = this.font.width(emptyText);
            graphics.drawString(
                this.font,
                emptyText,
                listX + (listWidth - textWidth) / 2,
                listY + listHeight / 2 - 4,
                0x888888,
                true
            );
        } else {
            graphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);
            
            int visibleRows = Math.min(materials.size() - scrollOffset, maxVisibleRows);
            for (int i = 0; i < visibleRows; i++) {
                int index = scrollOffset + i;
                MaterialEntry entry = materials.get(index);
                int rowY = listY + (i * ROW_HEIGHT) + 1;
                
                boolean hovered = mouseX >= listX && mouseX <= listX + listWidth &&
                                  mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
                
                if (hovered) {
                    graphics.fill(listX + 1, rowY, listX + listWidth - 1, rowY + ROW_HEIGHT, 0xFF333333);
                }
                
                ItemStack itemStack = entry.getItemStack();
                if (!itemStack.isEmpty()) {
                    graphics.renderItem(itemStack, listX + 4, rowY + 1);
                }
                
                String countText = String.format("%dx", entry.count);
                graphics.drawString(
                    this.font,
                    countText,
                    listX + 24,
                    rowY + 5,
                    0x55FF55,
                    true
                );
                
                String displayName = entry.name;
                int maxNameWidth = listWidth - 85;
                if (this.font.width(displayName) > maxNameWidth) {
                    while (this.font.width(displayName + "...") > maxNameWidth && displayName.length() > 1) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }
                    displayName += "...";
                }
                
                graphics.drawString(
                    this.font,
                    displayName,
                    listX + 65,
                    rowY + 5,
                    0xFFFFFF,
                    true
                );
            }
            
            graphics.disableScissor();
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (materials.size() > maxVisibleRows) {
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                scrollOffset = Math.min(maxScrollOffset, scrollOffset + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }
    
    private static class MaterialEntry {
        final String name;
        final int count;
        final Block block;
        private ItemStack cachedItemStack = null;
        
        MaterialEntry(String name, int count, Block block) {
            this.name = name;
            this.count = count;
            this.block = block;
        }
        
        ItemStack getItemStack() {
            if (cachedItemStack == null) {
                try {
                    ItemStack stack = new ItemStack(block.asItem());
                    if (stack.isEmpty()) {
                        cachedItemStack = new ItemStack(Items.BARRIER);
                    } else {
                        cachedItemStack = stack;
                    }
                } catch (Exception e) {
                    cachedItemStack = new ItemStack(Items.BARRIER);
                }
            }
            return cachedItemStack;
        }
    }
}
