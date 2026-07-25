package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders stored announcements as a section: a header row followed by the most recent entries,
 * newest first. Emits nothing at all when there are no announcements, so an empty section header
 * never occupies a slot.
 */
public class AnnouncementProvider implements TabContentProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final AnnouncementStore store;
    private final String headerTemplate;
    private final RowTemplate rowTemplate;
    private final String iconTexture;
    private final int maxRows;

    public AnnouncementProvider(AnnouncementStore store, String headerTemplate,
                                RowTemplate rowTemplate, String iconTexture, int maxRows) {
        this.store = store;
        this.headerTemplate = headerTemplate;
        this.rowTemplate = rowTemplate;
        this.iconTexture = iconTexture;
        this.maxRows = maxRows;
    }

    @Override
    public List<TabRow> rows(int regionSize) {
        List<String> stored = store.list();
        if (stored.isEmpty() || regionSize <= 0) {
            return List.of();
        }
        List<TabRow> rows = new ArrayList<>();
        rows.add(new TabRow(MINI_MESSAGE.deserialize(headerTemplate), iconTexture));
        for (int i = stored.size() - 1; i >= 0 && rows.size() - 1 < maxRows; i--) {
            rows.add(new TabRow(rowTemplate.render(stored.get(i)), iconTexture));
        }
        return rows.size() > regionSize ? rows.subList(0, regionSize) : rows;
    }
}
