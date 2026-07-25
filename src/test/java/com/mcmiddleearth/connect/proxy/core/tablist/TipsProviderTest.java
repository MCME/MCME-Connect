package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipsProviderTest {

    private static String plain(TabRow row) {
        return PlainTextComponentSerializer.plainText().serialize(row.displayName());
    }

    private static TipsProvider provider(AtomicLong clock, int windowSeconds, String... tips) {
        return new TipsProvider(List.of(tips), "<gold>Tips", new RowTemplate("<gray><text>"),
                "tipIcon", 2, windowSeconds, clock::get);
    }

    @Test
    void testFirstRowIsTheSectionHeader() {
        List<TabRow> rows = provider(new AtomicLong(0), 30, "a", "b").rows(20);
        assertEquals("Tips", plain(rows.get(0)));
    }

    @Test
    void testShowsWindowSizedSliceOfTips() {
        List<TabRow> rows = provider(new AtomicLong(0), 30, "a", "b", "c", "d").rows(20);
        assertEquals(3, rows.size());
        assertEquals("a", plain(rows.get(1)));
        assertEquals("b", plain(rows.get(2)));
    }

    @Test
    void testRotatesToNextSliceAfterInterval() {
        AtomicLong clock = new AtomicLong(0);
        TipsProvider tips = provider(clock, 30, "a", "b", "c", "d");
        clock.set(30);
        List<TabRow> rows = tips.rows(20);
        assertEquals("c", plain(rows.get(1)));
        assertEquals("d", plain(rows.get(2)));
    }

    @Test
    void testWrapsAroundToTheStart() {
        AtomicLong clock = new AtomicLong(0);
        TipsProvider tips = provider(clock, 30, "a", "b", "c", "d");
        clock.set(60);
        assertEquals("a", plain(tips.rows(20).get(1)));
    }

    @Test
    void testRendersNothingWhenNoTipsConfigured() {
        assertTrue(provider(new AtomicLong(0), 30).rows(20).isEmpty());
    }

    @Test
    void testDoesNotDivideByZeroWhenIntervalIsZero() {
        List<TabRow> rows = provider(new AtomicLong(5), 0, "a", "b").rows(20);
        assertEquals("a", plain(rows.get(1)));
    }
}
