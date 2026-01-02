package dev.thefern.buildinggadgets2gui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_DEBUG_TAB = BUILDER
            .comment("Whether to show the Debug tab in the Copy/Paste GUI (for development)")
            .define("showDebugTab", false);

    public static final ModConfigSpec.IntValue MAX_HISTORY_ENTRIES = BUILDER
            .comment("Maximum number of history entries to keep (older entries will be removed)")
            .defineInRange("maxHistoryEntries", 30, 1, 500);

    static final ModConfigSpec SPEC = BUILDER.build();
}
