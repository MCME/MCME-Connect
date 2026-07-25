package com.mcmiddleearth.connect.proxy.core.tablist;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the fixed 60-entry grid. Sending exactly 60 entries forces the vanilla client to render
 * 3 columns of 20, so slot positions never move as players join and quit.
 *
 * <p>Slots are 1-based and filled column-major: slots 1-20 are the first column, top to bottom.
 */
public class SlotGrid {

    public static final int TOTAL_SLOTS = 60;
    public static final int ROSTER_SLOTS = 40;
    public static final int FIRST_RESERVED_SLOT = ROSTER_SLOTS + 1;
    public static final int RESERVED_SLOTS = TOTAL_SLOTS - ROSTER_SLOTS;

    /**
     * Maps a 1-based slot to a listOrder value. Slot 1 gets the highest value so it sorts first.
     *
     * <p>Direction is unverified against a live client. If the dev server renders the grid
     * upside down, invert this single expression to {@code slot} and the whole layout follows.
     */
    public static int listOrderFor(int slot) {
        return TOTAL_SLOTS - slot + 1;
    }

    /**
     * @param rosterRows    player rows, highest priority first; truncated past {@link #ROSTER_SLOTS}
     * @param reservedRows  info panel rows; truncated past {@link #RESERVED_SLOTS}
     * @return exactly {@link #TOTAL_SLOTS} rows, blank-padded
     */
    public List<TabRow> assemble(List<TabRow> rosterRows, List<TabRow> reservedRows) {
        List<TabRow> grid = new ArrayList<>(TOTAL_SLOTS);
        for (int i = 0; i < ROSTER_SLOTS; i++) {
            grid.add(i < rosterRows.size() ? rosterRows.get(i) : TabRow.blank());
        }
        for (int i = 0; i < RESERVED_SLOTS; i++) {
            grid.add(i < reservedRows.size() ? reservedRows.get(i) : TabRow.blank());
        }
        return grid;
    }
}
