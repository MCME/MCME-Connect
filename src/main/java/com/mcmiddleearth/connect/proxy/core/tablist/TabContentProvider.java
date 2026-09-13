package com.mcmiddleearth.connect.proxy.core.tablist;

import java.util.List;

/** Supplies rows for one region of the tab list grid. */
@FunctionalInterface
public interface TabContentProvider {

    /**
     * @param regionSize  how many slots the region has; implementations may return fewer
     * @return rows to place, never null
     */
    List<TabRow> rows(int regionSize);
}
