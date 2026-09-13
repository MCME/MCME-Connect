package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TabRowTest {

    @Test
    void testCarriesNameAndTexture() {
        TabRow row = new TabRow(Component.text("Announcements"), "base64tex");
        assertEquals(Component.text("Announcements"), row.displayName());
        assertEquals("base64tex", row.iconTexture());
    }

    @Test
    void testBlankRowHasEmptyNameAndNoTexture() {
        TabRow blank = TabRow.blank();
        assertEquals(Component.empty(), blank.displayName());
        assertNull(blank.iconTexture());
    }
}
