package dev.thefern.buildinggadgets2gui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_DEBUG_TAB = BUILDER
            .comment("Whether to show the Debug tab in the Copy/Paste GUI (for development)")
            .define("showDebugTab", false);

    public static final ModConfigSpec.IntValue MAX_HISTORY_ENTRIES = BUILDER
            .comment("Maximum number of history entries to keep (older entries will be removed)")
            .defineInRange("maxHistoryEntries", 30, 0, 500);

    public static final ModConfigSpec.IntValue MAX_TRASH_ITEMS = BUILDER
            .comment("Maximum number of items to keep in trash (0 = unlimited)")
            .defineInRange("maxTrashItems", 50, 0, 500);

    public static final ModConfigSpec.IntValue TRASH_RETENTION_DAYS = BUILDER
            .comment("Days to keep items in trash before auto-delete (0 = never auto-delete)")
            .defineInRange("trashRetentionDays", 30, 0, 365);

    static final ModConfigSpec SPEC = BUILDER.build();
}
