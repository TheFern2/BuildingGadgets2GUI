package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import dev.thefern.buildinggadgets2gui.client.RowActionButtons;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.EditTagsDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.MaterialListDialog;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFile;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicManager;
import dev.thefern.buildinggadgets2gui.client.schematics.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchTab extends TabPanel {
    
    private static final int PADDING = 10;
    private static final int SEARCH_BOX_WIDTH = 200;
    private static final int TAG_BUTTON_HEIGHT = 16;
    private static final int TAG_SPACING = 4;
    private static final int TAG_PADDING = 6;
    private static final int TAGS_SECTION_HEIGHT = 40;
    private static final int RESULT_ROW_HEIGHT = 24;
    private static final int RESULTS_HEIGHT = 120;
    
    private EditBox searchBox;
    private Set<String> selectedTags = new HashSet<>();
    private List<SchematicFile> searchResults = new ArrayList<>();
    private int scrollOffset = 0;
    private int tagScrollOffset = 0;
    private int maxVisibleResults;
    
    private static String lastSearchText = "";
    private static Set<String> lastSelectedTags = new HashSet<>();
    private static List<SchematicFile> lastSearchResults = new ArrayList<>();
    private static int lastScrollOffset = 0;
    private static int lastTagScrollOffset = 0;
    
    private Component pendingTooltip = null;
    private int tooltipX = 0;
    private int tooltipY = 0;
    
    private int tagsPerRow;
    private int totalTagRows;
    private int visibleTagRows = 2;
    
    public SearchTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
        maxVisibleResults = RESULTS_HEIGHT / RESULT_ROW_HEIGHT;
    }
    
    @Override
    public void init() {
        int searchY = y + 5;
        
        searchBox = new EditBox(
            Minecraft.getInstance().font,
            x + PADDING,
            searchY,
            SEARCH_BOX_WIDTH,
            18,
            Component.literal("Search...")
        );
        searchBox.setHint(Component.literal("Search schematics..."));
        searchBox.setResponder(this::onSearchTextChanged);
        searchBox.setValue(lastSearchText);
        widgets.add(searchBox);
        
        Button clearButton = Button.builder(
            Component.literal("Clear"),
            button -> clearSearch()
        )
        .bounds(x + PADDING + SEARCH_BOX_WIDTH + 5, searchY, 50, 18)
        .build();
        widgets.add(clearButton);
        
        selectedTags = new HashSet<>(lastSelectedTags);
        searchResults = new ArrayList<>(lastSearchResults);
        scrollOffset = lastScrollOffset;
        tagScrollOffset = lastTagScrollOffset;
        
        calculateTagLayout();
    }
    
    private void calculateTagLayout() {
        int availableWidth = width - PADDING * 2 - 20;
        List<String> allTags = TagManager.getAllTags();
        
        int currentX = 0;
        int rows = 1;
        for (String tag : allTags) {
            int tagWidth = getTagButtonWidth(tag, selectedTags.contains(tag));
            if (currentX + tagWidth > availableWidth && currentX > 0) {
                currentX = 0;
                rows++;
            }
            currentX += tagWidth + TAG_SPACING;
        }
        totalTagRows = rows;
        tagsPerRow = allTags.isEmpty() ? 1 : (int) Math.ceil((double) allTags.size() / rows);
    }
    
    private void onSearchTextChanged(String text) {
        lastSearchText = text;
        performSearch();
    }
    
    private void clearSearch() {
        searchBox.setValue("");
        selectedTags.clear();
        searchResults.clear();
        scrollOffset = 0;
        tagScrollOffset = 0;
        
        lastSearchText = "";
        lastSelectedTags.clear();
        lastSearchResults.clear();
        lastScrollOffset = 0;
        lastTagScrollOffset = 0;
    }
    
    private void performSearch() {
        searchResults.clear();
        scrollOffset = 0;
        
        String searchText = searchBox.getValue().toLowerCase().trim();
        
        if (searchText.isEmpty() && selectedTags.isEmpty()) {
            lastSearchResults.clear();
            lastScrollOffset = 0;
            return;
        }
        
        File schematicsRoot = SchematicManager.getSchematicsRoot();
        if (schematicsRoot != null && schematicsRoot.exists()) {
            searchInFolder(schematicsRoot, searchText);
        }
        
        searchResults.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        
        lastSearchResults = new ArrayList<>(searchResults);
        lastScrollOffset = scrollOffset;
    }
    
    private void searchInFolder(File folder, String searchText) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                searchInFolder(file, searchText);
            } else if (file.getName().endsWith(".bg2schem")) {
                SchematicFile schematic = new SchematicFile(file);
                if (matchesSearch(schematic, searchText)) {
                    searchResults.add(schematic);
                }
            }
        }
    }
    
    private boolean matchesSearch(SchematicFile schematic, String searchText) {
        boolean matchesText = searchText.isEmpty() || 
                              schematic.getName().toLowerCase().contains(searchText);
        
        if (!matchesText) {
            SchematicFile.SchematicMetadata metadata = schematic.getMetadata();
            if (metadata != null && metadata.description != null) {
                matchesText = metadata.description.toLowerCase().contains(searchText);
            }
        }
        
        boolean matchesTags = selectedTags.isEmpty();
        if (!selectedTags.isEmpty()) {
            List<String> schematicTags = schematic.getTags();
            for (String selectedTag : selectedTags) {
                if (schematicTags.contains(selectedTag)) {
                    matchesTags = true;
                    break;
                }
            }
        }
        
        return matchesText && matchesTags;
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        calculateTagLayout();
        if (searchBox != null) {
            searchBox.setValue(lastSearchText);
        }
        selectedTags = new HashSet<>(lastSelectedTags);
        searchResults = new ArrayList<>(lastSearchResults);
        scrollOffset = lastScrollOffset;
        tagScrollOffset = lastTagScrollOffset;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        pendingTooltip = null;
        
        int tagsSectionY = y + 28;
        renderTagsSection(guiGraphics, mouseX, mouseY, tagsSectionY);
        
        int tagSectionEnd = tagsSectionY + TAGS_SECTION_HEIGHT + 5;
        guiGraphics.fill(x + PADDING, tagSectionEnd, x + width - PADDING, tagSectionEnd + 1, 0xFF444444);
        
        int resultsY = tagSectionEnd + 5;
        int resultsX = x + PADDING;
        int resultsWidth = width - PADDING * 2;
        
        guiGraphics.fill(resultsX, resultsY, resultsX + resultsWidth, resultsY + RESULTS_HEIGHT, 0xFF1A1A1A);
        
        if (searchResults.isEmpty()) {
            String message;
            if (searchBox.getValue().isEmpty() && selectedTags.isEmpty()) {
                message = "Enter search text or select tags to search";
            } else {
                message = "No results found";
            }
            int textWidth = Minecraft.getInstance().font.width(message);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                message,
                resultsX + (resultsWidth - textWidth) / 2,
                resultsY + RESULTS_HEIGHT / 2 - 4,
                0x888888,
                false
            );
        } else {
            renderSearchResults(guiGraphics, mouseX, mouseY, resultsX, resultsY, resultsWidth);
        }
        
        int statusY = resultsY + RESULTS_HEIGHT + 5;
        String statusText = searchResults.size() + " result" + (searchResults.size() != 1 ? "s" : "");
        if (!selectedTags.isEmpty()) {
            statusText += " | Tags: " + String.join(", ", selectedTags);
        }
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            statusText,
            resultsX,
            statusY,
            0xAAAAAA,
            false
        );
        
        if (pendingTooltip != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, pendingTooltip, tooltipX, tooltipY);
        }
    }
    
    private int getTagButtonWidth(String tag, boolean isSelected) {
        String displayTag = isSelected ? "✓ " + tag : tag;
        return Minecraft.getInstance().font.width(displayTag) + TAG_PADDING * 2;
    }
    
    private void renderTagsSection(GuiGraphics guiGraphics, int mouseX, int mouseY, int tagsSectionY) {
        int tagsX = x + PADDING;
        int tagsWidth = width - PADDING * 2 - 10;
        
        guiGraphics.fill(tagsX, tagsSectionY, tagsX + tagsWidth, tagsSectionY + TAGS_SECTION_HEIGHT, 0xFF1A1A1A);
        
        guiGraphics.enableScissor(tagsX, tagsSectionY, tagsX + tagsWidth, tagsSectionY + TAGS_SECTION_HEIGHT);
        
        List<String> allTags = TagManager.getAllTags();
        int currentX = tagsX + 2;
        int currentY = tagsSectionY + 2 - (tagScrollOffset * (TAG_BUTTON_HEIGHT + TAG_SPACING));
        
        for (int i = 0; i < allTags.size(); i++) {
            String tag = allTags.get(i);
            boolean isSelected = selectedTags.contains(tag);
            int tagWidth = getTagButtonWidth(tag, isSelected);
            
            if (currentX + tagWidth > tagsX + tagsWidth - 2) {
                currentX = tagsX + 2;
                currentY += TAG_BUTTON_HEIGHT + TAG_SPACING;
            }
            
            if (currentY + TAG_BUTTON_HEIGHT > tagsSectionY && currentY < tagsSectionY + TAGS_SECTION_HEIGHT) {
                boolean isHovered = mouseX >= currentX && mouseX <= currentX + tagWidth &&
                                   mouseY >= currentY && mouseY <= currentY + TAG_BUTTON_HEIGHT &&
                                   mouseY >= tagsSectionY && mouseY <= tagsSectionY + TAGS_SECTION_HEIGHT;
                
                int bgColor = isSelected ? 0xFF446688 : (isHovered ? 0xFF3A3A3A : 0xFF2A2A2A);
                guiGraphics.fill(currentX, currentY, currentX + tagWidth, currentY + TAG_BUTTON_HEIGHT, bgColor);
                
                String displayTag = isSelected ? "✓ " + tag : tag;
                
                int textColor = isSelected ? 0xFFFFFF : (isHovered ? 0xFFFFFF : 0xCCCCCC);
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    displayTag,
                    currentX + TAG_PADDING,
                    currentY + 4,
                    textColor,
                    false
                );
            }
            
            currentX += tagWidth + TAG_SPACING;
        }
        
        guiGraphics.disableScissor();
        
        if (totalTagRows > visibleTagRows) {
            int scrollbarX = tagsX + tagsWidth + 2;
            int scrollbarHeight = Math.max(10, (int) ((float) visibleTagRows / totalTagRows * TAGS_SECTION_HEIGHT));
            int maxScroll = totalTagRows - visibleTagRows;
            int scrollbarY = tagsSectionY + (int) ((float) tagScrollOffset / maxScroll * (TAGS_SECTION_HEIGHT - scrollbarHeight));
            
            guiGraphics.fill(scrollbarX, tagsSectionY, scrollbarX + 6, tagsSectionY + TAGS_SECTION_HEIGHT, 0xFF333333);
            guiGraphics.fill(scrollbarX + 1, scrollbarY, scrollbarX + 5, scrollbarY + scrollbarHeight, 0xFF666666);
        }
    }
    
    private void renderSearchResults(GuiGraphics guiGraphics, int mouseX, int mouseY, int resultsX, int resultsY, int resultsWidth) {
        guiGraphics.enableScissor(resultsX, resultsY, resultsX + resultsWidth, resultsY + RESULTS_HEIGHT);
        
        int currentY = resultsY;
        for (int i = scrollOffset; i < searchResults.size() && i < scrollOffset + maxVisibleResults; i++) {
            SchematicFile file = searchResults.get(i);
            renderResultRow(guiGraphics, mouseX, mouseY, file, resultsX, currentY, resultsWidth, i);
            currentY += RESULT_ROW_HEIGHT;
        }
        
        guiGraphics.disableScissor();
        
        if (searchResults.size() > maxVisibleResults) {
            int scrollbarX = resultsX + resultsWidth - 6;
            int scrollbarHeight = Math.max(10, (int) ((float) maxVisibleResults / searchResults.size() * RESULTS_HEIGHT));
            int scrollbarY = resultsY + (int) ((float) scrollOffset / (searchResults.size() - maxVisibleResults) * (RESULTS_HEIGHT - scrollbarHeight));
            
            guiGraphics.fill(scrollbarX, resultsY, scrollbarX + 4, resultsY + RESULTS_HEIGHT, 0xFF333333);
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF666666);
        }
    }
    
    private void renderResultRow(GuiGraphics guiGraphics, int mouseX, int mouseY, SchematicFile file, 
                                  int rowX, int rowY, int rowWidth, int index) {
        boolean isHovered = mouseX >= rowX && mouseX <= rowX + rowWidth &&
                           mouseY >= rowY && mouseY <= rowY + RESULT_ROW_HEIGHT;
        
        int bgColor = isHovered ? 0xFF2A2A2A : 0xFF1A1A1A;
        guiGraphics.fill(rowX, rowY, rowX + rowWidth, rowY + RESULT_ROW_HEIGHT, bgColor);
        
        guiGraphics.fill(rowX, rowY + RESULT_ROW_HEIGHT - 1, rowX + rowWidth, rowY + RESULT_ROW_HEIGHT, 0xFF333333);
        
        List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(rowX, rowY, rowWidth);
        RowActionButtons.renderButtons(guiGraphics, actionButtons, mouseX, mouseY);
        
        for (RowActionButtons.ButtonBounds bounds : actionButtons) {
            if (bounds.contains(mouseX, mouseY)) {
                pendingTooltip = Component.literal(bounds.type.tooltip);
                tooltipX = mouseX;
                tooltipY = mouseY;
                break;
            }
        }
        
        String fileIcon = "📄";
        String fileName = file.getName();
        if (fileName.length() > 25) {
            fileName = fileName.substring(0, 22) + "...";
        }
        
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            fileIcon + " " + fileName,
            rowX + 5,
            rowY + 2,
            isHovered ? 0xFFFFFF : 0xCCCCCC,
            false
        );
        
        SchematicFile.SchematicMetadata metadata = file.getMetadata();
        String info = "";
        if (metadata != null && metadata.tags != null && !metadata.tags.isEmpty()) {
            info = String.join(", ", metadata.tags);
            if (info.length() > 40) {
                info = info.substring(0, 37) + "...";
            }
        }
        
        if (!info.isEmpty()) {
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                info,
                rowX + 5,
                rowY + 13,
                0x8888FF,
                false
            );
        }
    }
    
    private List<RowActionButtons.ButtonBounds> getActionBounds(int rowX, int rowY, int rowWidth) {
        int buttonY = rowY + 6;
        int rightX = rowX + rowWidth - 14;
        return RowActionButtons.calculateButtonBounds(
            rightX, 
            buttonY, 
            RowActionButtons.BUTTON_SIZE_SMALL,
            RowActionButtons.ButtonType.TAGS,
            RowActionButtons.ButtonType.MATERIAL,
            RowActionButtons.ButtonType.TOOL
        );
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive || button != 0) return false;
        
        int tagsSectionY = y + 28;
        int tagsX = x + PADDING;
        int tagsWidth = width - PADDING * 2 - 10;
        
        if (mouseX >= tagsX && mouseX <= tagsX + tagsWidth &&
            mouseY >= tagsSectionY && mouseY <= tagsSectionY + TAGS_SECTION_HEIGHT) {
            
            List<String> allTags = TagManager.getAllTags();
            int currentX = tagsX + 2;
            int currentY = tagsSectionY + 2 - (tagScrollOffset * (TAG_BUTTON_HEIGHT + TAG_SPACING));
            
            for (String tag : allTags) {
                boolean isSelected = selectedTags.contains(tag);
                int tagWidth = getTagButtonWidth(tag, isSelected);
                
                if (currentX + tagWidth > tagsX + tagsWidth - 2) {
                    currentX = tagsX + 2;
                    currentY += TAG_BUTTON_HEIGHT + TAG_SPACING;
                }
                
                if (mouseX >= currentX && mouseX <= currentX + tagWidth &&
                    mouseY >= currentY && mouseY <= currentY + TAG_BUTTON_HEIGHT &&
                    currentY + TAG_BUTTON_HEIGHT > tagsSectionY && currentY < tagsSectionY + TAGS_SECTION_HEIGHT) {
                    
                    if (isSelected) {
                        selectedTags.remove(tag);
                    } else {
                        selectedTags.add(tag);
                    }
                    lastSelectedTags = new HashSet<>(selectedTags);
                    performSearch();
                    return true;
                }
                
                currentX += tagWidth + TAG_SPACING;
            }
            return true;
        }
        
        int tagSectionEnd = tagsSectionY + TAGS_SECTION_HEIGHT + 5;
        int resultsY = tagSectionEnd + 5;
        int resultsX = x + PADDING;
        int resultsWidth = width - PADDING * 2;
        
        if (mouseX >= resultsX && mouseX <= resultsX + resultsWidth &&
            mouseY >= resultsY && mouseY <= resultsY + RESULTS_HEIGHT) {
            
            int clickedIndex = scrollOffset + (int) ((mouseY - resultsY) / RESULT_ROW_HEIGHT);
            if (clickedIndex >= 0 && clickedIndex < searchResults.size()) {
                SchematicFile file = searchResults.get(clickedIndex);
                
                int rowY = resultsY + (clickedIndex - scrollOffset) * RESULT_ROW_HEIGHT;
                List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(resultsX, rowY, resultsWidth);
                RowActionButtons.ButtonType clickedType = RowActionButtons.getClickedButton(actionButtons, mouseX, mouseY);
                
                if (clickedType != null) {
                    handleActionButtonClick(clickedType, file);
                    return true;
                }
            }
            return true;
        }
        
        return false;
    }
    
    private void handleActionButtonClick(RowActionButtons.ButtonType buttonType, SchematicFile file) {
        switch (buttonType) {
            case MATERIAL:
                onMaterialButtonClicked(file);
                break;
            case TOOL:
                onToolButtonClicked(file);
                break;
            case TAGS:
                onTagsButtonClicked(file);
                break;
            default:
                break;
        }
    }
    
    private void onMaterialButtonClicked(SchematicFile file) {
        SchematicFile.SchematicData data = file.loadData();
        if (data != null && data.blocks != null) {
            MaterialListDialog dialog = new MaterialListDialog(
                Minecraft.getInstance().screen,
                "Materials: " + file.getName(),
                data.blocks
            );
            Minecraft.getInstance().setScreen(dialog);
        }
    }
    
    private void onToolButtonClicked(SchematicFile file) {
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Send to Tool",
            "This will override current tool copy data. Continue?",
            confirmed -> {
                if (confirmed) {
                    SchematicFile.SchematicData data = file.loadData();
                    if (data != null && data.blocks != null) {
                        HistoryTab.setClipboard(
                            data.blocks,
                            data.teData,
                            data.copyUUID != null ? java.util.UUID.fromString(data.copyUUID) : null,
                            data.blockCount
                        );
                        ClipboardUtils.sendToTool();
                        System.out.println("Sent schematic '" + file.getName() + "' to tool");
                    }
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void onTagsButtonClicked(SchematicFile file) {
        EditTagsDialog dialog = new EditTagsDialog(
            parentScreen,
            file,
            success -> {
                if (success) {
                    performSearch();
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isActive) return false;
        
        int tagsSectionY = y + 28;
        int tagsX = x + PADDING;
        int tagsWidth = width - PADDING * 2;
        
        if (mouseX >= tagsX && mouseX <= tagsX + tagsWidth &&
            mouseY >= tagsSectionY && mouseY <= tagsSectionY + TAGS_SECTION_HEIGHT) {
            
            int maxTagScroll = Math.max(0, totalTagRows - visibleTagRows);
            if (scrollY > 0 && tagScrollOffset > 0) {
                tagScrollOffset--;
                lastTagScrollOffset = tagScrollOffset;
                return true;
            } else if (scrollY < 0 && tagScrollOffset < maxTagScroll) {
                tagScrollOffset++;
                lastTagScrollOffset = tagScrollOffset;
                return true;
            }
            return true;
        }
        
        int tagSectionEnd = tagsSectionY + TAGS_SECTION_HEIGHT + 5;
        int resultsY = tagSectionEnd + 5;
        int resultsX = x + PADDING;
        int resultsWidth = width - PADDING * 2;
        
        if (mouseX >= resultsX && mouseX <= resultsX + resultsWidth &&
            mouseY >= resultsY && mouseY <= resultsY + RESULTS_HEIGHT) {
            
            if (scrollY > 0 && scrollOffset > 0) {
                scrollOffset--;
                lastScrollOffset = scrollOffset;
                return true;
            } else if (scrollY < 0 && scrollOffset < searchResults.size() - maxVisibleResults) {
                scrollOffset++;
                lastScrollOffset = scrollOffset;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void tick() {
    }
}
