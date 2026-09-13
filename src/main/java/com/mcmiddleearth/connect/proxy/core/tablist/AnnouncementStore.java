package com.mcmiddleearth.connect.proxy.core.tablist;

import com.mcmiddleearth.base.core.configuration.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores staff-authored announcement strings as plain text. Content only — styling is applied at
 * render time by {@link RowTemplate}, so a moderator never writes markup.
 *
 * <p>All accessors are synchronized: the re-assert scheduler, the join listener and the
 * {@code /tabnews} command all reach this object from different threads. Unsynchronized reads of an
 * {@link ArrayList} being structurally modified can observe trailing nulls, and concurrent
 * {@link #save()} calls would interleave into the same truncating writer and corrupt the file.
 */
public class AnnouncementStore {

    private static final String KEY = "announcements";

    private final File file;
    private final List<String> announcements = new ArrayList<>();

    public AnnouncementStore(File file) {
        this.file = file;
        if (file.exists()) {
            YamlConfiguration config = new YamlConfiguration(file);
            List<String> stored = config.getStringList(KEY);
            if (stored != null) {
                announcements.addAll(stored);
            }
        }
    }

    /**
     * @return a defensive copy, oldest first
     */
    public synchronized List<String> list() {
        return new ArrayList<>(announcements);
    }

    public synchronized void add(String text) {
        announcements.add(text);
        save();
    }

    /**
     * @param oneBasedIndex  position as shown by {@code /tabnews list}
     * @return false if out of range
     */
    public synchronized boolean remove(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > announcements.size()) {
            return false;
        }
        announcements.remove(oneBasedIndex - 1);
        save();
        return true;
    }

    private void save() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set(KEY, new ArrayList<>(announcements));
        config.save(file);
    }
}
