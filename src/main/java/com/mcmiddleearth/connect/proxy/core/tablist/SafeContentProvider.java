package com.mcmiddleearth.connect.proxy.core.tablist;

import java.util.List;
import java.util.function.Consumer;

/**
 * Isolates one provider's failures from the rest of the tab list. A throwing provider yields an
 * empty region and logs exactly once, so a broken feed degrades visibly without spamming the log
 * at the re-assert interval or taking down the whole grid.
 */
public class SafeContentProvider implements TabContentProvider {

    private final String name;
    private final TabContentProvider delegate;
    private final Consumer<String> errorLogger;

    private boolean alreadyLogged = false;

    public SafeContentProvider(String name, TabContentProvider delegate, Consumer<String> errorLogger) {
        this.name = name;
        this.delegate = delegate;
        this.errorLogger = errorLogger;
    }

    @Override
    public List<TabRow> rows(int regionSize) {
        try {
            List<TabRow> rows = delegate.rows(regionSize);
            return rows == null ? List.of() : rows;
        } catch (RuntimeException e) {
            if (!alreadyLogged) {
                alreadyLogged = true;
                errorLogger.accept("Tab list provider '" + name + "' failed, region left empty: " + e);
            }
            return List.of();
        }
    }

    /** Called on config reload so a fixed provider can report a fresh failure. */
    public void resetFailureLatch() {
        alreadyLogged = false;
    }
}
