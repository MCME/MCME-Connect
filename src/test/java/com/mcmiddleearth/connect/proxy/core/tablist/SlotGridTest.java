package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotGridTest {

    private static List<TabRow> rows(int count, String prefix) {
        List<TabRow> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(new TabRow(Component.text(prefix + i), null));
        }
        return out;
    }

    @Test
    void testAlwaysProducesExactlySixtySlots() {
        List<TabRow> grid = new SlotGrid().assemble(rows(5, "p"), rows(3, "r"));
        assertEquals(60, grid.size());
    }

    @Test
    void testRosterFillsFromSlotOne() {
        List<TabRow> grid = new SlotGrid().assemble(rows(2, "p"), List.of());
        assertEquals(Component.text("p0"), grid.get(0).displayName());
        assertEquals(Component.text("p1"), grid.get(1).displayName());
    }

    @Test
    void testReservedRegionStartsAtSlotFortyOne() {
        List<TabRow> grid = new SlotGrid().assemble(rows(2, "p"), rows(1, "r"));
        assertEquals(Component.text("r0"), grid.get(40).displayName());
    }

    @Test
    void testGapBetweenRosterAndReservedIsBlank() {
        List<TabRow> grid = new SlotGrid().assemble(rows(2, "p"), rows(1, "r"));
        assertEquals(Component.empty(), grid.get(2).displayName());
        assertEquals(Component.empty(), grid.get(39).displayName());
    }

    @Test
    void testRosterOverflowIsTruncatedAndNeverEntersReserved() {
        List<TabRow> grid = new SlotGrid().assemble(rows(45, "p"), rows(1, "r"));
        assertEquals(Component.text("p39"), grid.get(39).displayName());
        assertEquals(Component.text("r0"), grid.get(40).displayName());
    }

    @Test
    void testReservedOverflowIsTruncated() {
        List<TabRow> grid = new SlotGrid().assemble(List.of(), rows(25, "r"));
        assertEquals(60, grid.size());
        assertEquals(Component.text("r19"), grid.get(59).displayName());
    }

    @Test
    void testListOrderDecreasesWithSlotIndexSoSlotOneSortsFirst() {
        assertEquals(60, SlotGrid.listOrderFor(1));
        assertEquals(59, SlotGrid.listOrderFor(2));
        assertEquals(1, SlotGrid.listOrderFor(60));
    }

    /**
     * What production saw on 2026-09-21. Connect 3.0.1 enabled the tab list unconditionally, phase
     * 1a passes no roster, and a config predating the reserved section yields no panel rows either,
     * so the grid is 60 blanks. VelocityTabRenderer still sends all 60 as entries at latency -1,
     * which the client draws as three columns of no-ping rows - and its cleanup pass removes the
     * real players. Hence the config gate: this state must never be reachable by default.
     */
    @Test
    void testAGridWithNoContentIsSixtyBlankRows() {
        List<TabRow> grid = new SlotGrid().assemble(List.of(), List.of());
        assertEquals(SlotGrid.TOTAL_SLOTS, grid.size());
        assertTrue(grid.stream().allMatch(row -> row.equals(TabRow.blank())),
                "every slot blank means a viewer sees empty rows and no players");
    }
}
