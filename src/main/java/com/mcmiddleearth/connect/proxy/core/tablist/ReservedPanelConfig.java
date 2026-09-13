package com.mcmiddleearth.connect.proxy.core.tablist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parsed {@code reserved:} config. Every field falls back to a usable default, so a malformed edit
 * degrades one setting rather than breaking the whole tab list.
 */
public class ReservedPanelConfig {

    public static final int DEFAULT_MAX_ROWS = 6;
    public static final int DEFAULT_WINDOW_SIZE = 2;
    public static final int DEFAULT_ROTATE_SECONDS = 30;

    private final String announcementIcon;
    private final String announcementHeader;
    private final String announcementTemplate;
    private final int announcementMaxRows;
    private final String tipIcon;
    private final String tipHeader;
    private final String tipTemplate;
    private final int tipWindowSize;
    private final int tipRotateSeconds;
    private final List<String> tipEntries;

    private ReservedPanelConfig(Map<String, Object> announcements, Map<String, Object> tips) {
        this.announcementIcon = string(announcements, "icon", "");
        this.announcementHeader = string(announcements, "header", "<gold>Announcements");
        this.announcementTemplate = string(announcements, "template", "<white><text>");
        this.announcementMaxRows = integer(announcements, "maxRows", DEFAULT_MAX_ROWS);
        this.tipIcon = string(tips, "icon", "");
        this.tipHeader = string(tips, "header", "<gold>Tips");
        this.tipTemplate = string(tips, "template", "<gray><text>");
        this.tipWindowSize = integer(tips, "windowSize", DEFAULT_WINDOW_SIZE);
        this.tipRotateSeconds = integer(tips, "rotateSeconds", DEFAULT_ROTATE_SECONDS);
        this.tipEntries = stringList(tips, "entries");
    }

    @SuppressWarnings("unchecked")
    public static ReservedPanelConfig parse(Map<String, Object> reservedSection) {
        Map<String, Object> section = reservedSection == null ? Map.of() : reservedSection;
        Object announcements = section.get("announcements");
        Object tips = section.get("tips");
        return new ReservedPanelConfig(
                announcements instanceof Map ? (Map<String, Object>) announcements : Map.of(),
                tips instanceof Map ? (Map<String, Object>) tips : Map.of());
    }

    private static String string(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value instanceof String s ? s : fallback;
    }

    private static int integer(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    private static List<String> stringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                out.add(item.toString());
            }
        }
        return List.copyOf(out);
    }

    public String announcementIcon() {
        return announcementIcon;
    }

    public String announcementHeader() {
        return announcementHeader;
    }

    public String announcementTemplate() {
        return announcementTemplate;
    }

    public int announcementMaxRows() {
        return announcementMaxRows;
    }

    public String tipIcon() {
        return tipIcon;
    }

    public String tipHeader() {
        return tipHeader;
    }

    public String tipTemplate() {
        return tipTemplate;
    }

    public int tipWindowSize() {
        return tipWindowSize;
    }

    public int tipRotateSeconds() {
        return tipRotateSeconds;
    }

    public List<String> tipEntries() {
        return tipEntries;
    }
}
