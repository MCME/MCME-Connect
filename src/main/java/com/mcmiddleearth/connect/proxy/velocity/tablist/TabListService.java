package com.mcmiddleearth.connect.proxy.velocity.tablist;

import com.mcmiddleearth.connect.proxy.core.tablist.AnnouncementProvider;
import com.mcmiddleearth.connect.proxy.core.tablist.AnnouncementStore;
import com.mcmiddleearth.connect.proxy.core.tablist.ReservedPanelConfig;
import com.mcmiddleearth.connect.proxy.core.tablist.RowTemplate;
import com.mcmiddleearth.connect.proxy.core.tablist.SafeContentProvider;
import com.mcmiddleearth.connect.proxy.core.tablist.SlotGrid;
import com.mcmiddleearth.connect.proxy.core.tablist.TabRow;
import com.mcmiddleearth.connect.proxy.core.tablist.TipsProvider;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the providers and drives the re-assert. Roster rows are empty in phase 1a; the reserved
 * panel is the shipped content.
 */
public class TabListService {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final SlotGrid slotGrid = new SlotGrid();
    private final VelocityTabRenderer renderer = new VelocityTabRenderer();

    private final SafeContentProvider announcements;
    private final SafeContentProvider tips;

    private final long startNanos = System.nanoTime();

    private volatile boolean renderFailureLogged = false;

    public TabListService(ProxyServer proxyServer, Logger logger,
                          ReservedPanelConfig config, AnnouncementStore store) {
        this.proxyServer = proxyServer;
        this.logger = logger;

        this.announcements = new SafeContentProvider("announcements",
                new AnnouncementProvider(store, config.announcementHeader(),
                        new RowTemplate(config.announcementTemplate()),
                        config.announcementIcon(), config.announcementMaxRows()),
                logger::warn);

        this.tips = new SafeContentProvider("tips",
                new TipsProvider(config.tipEntries(), config.tipHeader(),
                        new RowTemplate(config.tipTemplate()), config.tipIcon(),
                        config.tipWindowSize(), config.tipRotateSeconds(), this::elapsedSeconds),
                logger::warn);
    }

    private long elapsedSeconds() {
        return (System.nanoTime() - startNanos) / 1_000_000_000L;
    }

    public void applyTo(Player viewer) {
        applyGuarded(viewer, assemble());
    }

    public void applyToAll() {
        List<TabRow> grid = assemble();
        for (Player viewer : proxyServer.getAllPlayers()) {
            applyGuarded(viewer, grid);
        }
    }

    /**
     * Renders one viewer, containing any failure to that viewer. Without this a player who
     * disconnects mid-iteration would abort the loop and starve every viewer after them — and
     * because the task repeats, it would do so on every tick.
     */
    private void applyGuarded(Player viewer, List<TabRow> grid) {
        try {
            renderer.apply(viewer, grid);
            viewer.sendPlayerListHeaderAndFooter(
                    Component.text("Players in the MCME network"),
                    Component.empty());
        } catch (RuntimeException e) {
            if (!renderFailureLogged) {
                renderFailureLogged = true;
                logger.warn("Tab list render failed for {}; skipping this viewer: {}",
                        viewer.getUsername(), e.toString());
            }
        }
    }

    private List<TabRow> assemble() {
        List<TabRow> reserved = new ArrayList<>();
        reserved.addAll(announcements.rows(SlotGrid.RESERVED_SLOTS));
        reserved.addAll(tips.rows(SlotGrid.RESERVED_SLOTS - reserved.size()));
        return slotGrid.assemble(List.of(), reserved);
    }
}
