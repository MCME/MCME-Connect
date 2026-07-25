package com.mcmiddleearth.connect.proxy.velocity.tablist;

import com.mcmiddleearth.connect.proxy.core.tablist.SlotGrid;
import com.mcmiddleearth.connect.proxy.core.tablist.TabRow;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Applies an assembled grid to one viewer's tab list.
 *
 * <p>Entries are rebuilt per viewer on every apply. A {@code TabListEntry} keeps a reference to the
 * {@code TabList} it was built for, so sharing instances between players corrupts state
 * (PaperMC/Velocity#1455).
 *
 * <p>Re-asserting every tick is cheap: {@code VelocityTabList.addEntry} merges against its internal
 * model and writes nothing when an entry is unchanged. It is also the correction mechanism — a
 * backend {@code UpsertPlayerInfo} that clobbers a display name desyncs Velocity's model, and the
 * next apply detects the drift and emits a fix.
 */
public class VelocityTabRenderer {

    /** Deterministic UUID per slot so re-assertion targets the same entry every tick. */
    private static UUID slotId(int slot) {
        return UUID.nameUUIDFromBytes(("mcme:tabslot:" + slot).getBytes(StandardCharsets.UTF_8));
    }

    public void apply(Player viewer, List<TabRow> grid) {
        TabList tabList = viewer.getTabList();
        Set<UUID> desired = new HashSet<>();

        for (int i = 0; i < grid.size(); i++) {
            int slot = i + 1;
            UUID id = slotId(slot);
            desired.add(id);
            tabList.addEntry(buildEntry(id, slot, grid.get(i)));
        }

        for (TabListEntry existing : tabList.getEntries()) {
            UUID id = existing.getProfile().getId();
            if (!desired.contains(id)) {
                tabList.removeEntry(id);
            }
        }
    }

    private TabListEntry buildEntry(UUID id, int slot, TabRow row) {
        List<GameProfile.Property> properties = new ArrayList<>();
        if (row.iconTexture() != null && !row.iconTexture().isEmpty()) {
            properties.add(new GameProfile.Property("textures", row.iconTexture(), ""));
        }
        return TabListEntry.builder()
                .profile(new GameProfile(id, "slot" + slot, properties))
                .displayName(row.displayName())
                .latency(-1)
                .gameMode(3)
                .listed(true)
                .listOrder(SlotGrid.listOrderFor(slot))
                .showHat(false)
                .build();
    }
}
