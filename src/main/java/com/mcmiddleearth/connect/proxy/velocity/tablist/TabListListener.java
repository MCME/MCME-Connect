package com.mcmiddleearth.connect.proxy.velocity.tablist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;

/**
 * Re-asserts immediately after a server switch. Join and switch are when the backend sends its own
 * player list, so correcting here addresses the clobber at its cause rather than waiting a tick.
 */
public class TabListListener {

    private final TabListService service;

    public TabListListener(TabListService service) {
        this.service = service;
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        service.applyTo(event.getPlayer());
    }
}
