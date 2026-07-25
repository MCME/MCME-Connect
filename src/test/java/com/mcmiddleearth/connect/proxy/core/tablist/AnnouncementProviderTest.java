package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementProviderTest {

    private static String plain(TabRow row) {
        return PlainTextComponentSerializer.plainText().serialize(row.displayName());
    }

    private static AnnouncementProvider provider(Path dir, int maxRows, String... entries) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        for (String entry : entries) {
            store.add(entry);
        }
        return new AnnouncementProvider(store, "<gold>Announcements",
                new RowTemplate("<white><text>"), "iconTex", maxRows);
    }

    @Test
    void testFirstRowIsTheSectionHeader(@TempDir Path dir) {
        List<TabRow> rows = provider(dir, 5, "one").rows(20);
        assertEquals("Announcements", plain(rows.get(0)));
        assertEquals("iconTex", rows.get(0).iconTexture());
    }

    @Test
    void testNewestAnnouncementAppearsFirst(@TempDir Path dir) {
        List<TabRow> rows = provider(dir, 5, "older", "newer").rows(20);
        assertEquals("newer", plain(rows.get(1)));
        assertEquals("older", plain(rows.get(2)));
    }

    @Test
    void testCapsAtMaxRowsKeepingNewest(@TempDir Path dir) {
        List<TabRow> rows = provider(dir, 2, "a", "b", "c").rows(20);
        assertEquals(3, rows.size());
        assertEquals("c", plain(rows.get(1)));
        assertEquals("b", plain(rows.get(2)));
    }

    @Test
    void testRendersNothingAtAllWhenEmpty(@TempDir Path dir) {
        assertTrue(provider(dir, 5).rows(20).isEmpty());
    }

    @Test
    void testNeverExceedsRegionSize(@TempDir Path dir) {
        List<TabRow> rows = provider(dir, 50, "a", "b", "c", "d", "e").rows(3);
        assertEquals(3, rows.size());
    }
}
