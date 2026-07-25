package com.mcmiddleearth.connect.proxy.core.tablist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementStoreTest {

    @Test
    void testStartsEmptyWhenFileAbsent(@TempDir Path dir) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        assertTrue(store.list().isEmpty());
    }

    @Test
    void testAddThenListReturnsEntry(@TempDir Path dir) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        store.add("Server meeting Sunday");
        assertEquals(List.of("Server meeting Sunday"), store.list());
    }

    @Test
    void testPersistsAcrossReload(@TempDir Path dir) {
        File file = new File(dir.toFile(), "announcements.yml");
        new AnnouncementStore(file).add("Moria build week");
        assertEquals(List.of("Moria build week"), new AnnouncementStore(file).list());
    }

    @Test
    void testRemoveByIndexIsOneBased(@TempDir Path dir) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        store.add("first");
        store.add("second");
        assertTrue(store.remove(1));
        assertEquals(List.of("second"), store.list());
    }

    @Test
    void testRemoveOutOfRangeReturnsFalse(@TempDir Path dir) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        store.add("only");
        assertFalse(store.remove(0));
        assertFalse(store.remove(2));
        assertEquals(List.of("only"), store.list());
    }

    @Test
    void testListIsNotLiveModifiable(@TempDir Path dir) {
        AnnouncementStore store = new AnnouncementStore(new File(dir.toFile(), "announcements.yml"));
        store.add("one");
        store.list().clear();
        assertEquals(1, store.list().size());
    }
}
