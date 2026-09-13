package com.mcmiddleearth.connect.proxy.core.tablist;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservedPanelConfigTest {

    private static Map<String, Object> section(Object announcementsMaxRows, List<String> tips) {
        return Map.of(
                "announcements", Map.of(
                        "icon", "annIcon",
                        "header", "<gold>Announcements",
                        "template", "<white><text>",
                        "maxRows", announcementsMaxRows),
                "tips", Map.of(
                        "icon", "tipIcon",
                        "header", "<gold>Tips",
                        "template", "<gray><text>",
                        "windowSize", 2,
                        "rotateSeconds", 30,
                        "entries", tips));
    }

    @Test
    void testReadsAnnouncementSettings() {
        ReservedPanelConfig config = ReservedPanelConfig.parse(section(6, List.of("t1")));
        assertEquals("annIcon", config.announcementIcon());
        assertEquals("<gold>Announcements", config.announcementHeader());
        assertEquals(6, config.announcementMaxRows());
    }

    @Test
    void testReadsTipEntries() {
        ReservedPanelConfig config = ReservedPanelConfig.parse(section(6, List.of("t1", "t2")));
        assertEquals(List.of("t1", "t2"), config.tipEntries());
        assertEquals(30, config.tipRotateSeconds());
        assertEquals(2, config.tipWindowSize());
    }

    @Test
    void testFallsBackToDefaultsWhenSectionMissing() {
        ReservedPanelConfig config = ReservedPanelConfig.parse(null);
        assertEquals(ReservedPanelConfig.DEFAULT_MAX_ROWS, config.announcementMaxRows());
        assertEquals(List.of(), config.tipEntries());
    }

    @Test
    void testFallsBackToDefaultsWhenValuesAreWrongType() {
        ReservedPanelConfig config = ReservedPanelConfig.parse(section("not-a-number", List.of()));
        assertEquals(ReservedPanelConfig.DEFAULT_MAX_ROWS, config.announcementMaxRows());
    }
}
