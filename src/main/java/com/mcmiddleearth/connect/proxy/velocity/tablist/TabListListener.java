package com.mcmiddleearth.connect.proxy.velocity.tablist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;

/**
 * Re-asserts immediately after a join, server switch or disconnect. Those are when the backend
 * sends its own player list, so correcting here addresses the clobber at its cause rather than
 * waiting for the next scheduled tick.
 *
 * <p>Both handlers re-assert for <em>every</em> viewer, not just the player who moved. When one
 * player joins a backend, that backend's player-list packet reaches everyone else on it, pushing
 * them to 61 entries — and the vanilla client relays out 61 entries as 4 columns of 16 instead of
 * 3 of 20, moving the reserved panel until the next re-assert. Correcting only the moving player
 * would leave everyone else's grid visibly wrong for up to a full tick interval.
 */
public class TabListListener {

    private final TabListService service;

    public TabListListener(TabListService service) {
        this.service = service;
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        service.applyToAll();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        service.applyToAll();
    }
}
