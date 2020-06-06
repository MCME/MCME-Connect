package com.mcmiddleearth.connect.bungee.tabList;

import net.md_5.bungee.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabListManager implements Listener {

    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();

            ServerConnection server = (ServerConnection) event.getPlayer().getServer();

            ChannelWrapper wrapper = server.getCh();

            PacketHandler packetHandler = new PacketHandler();
            PacketListener packetListener = new PacketListener(server, packetHandler, player);

            wrapper.getHandle().pipeline().addBefore(PipelineUtils.BOSS_HANDLER, "btlp-packet-listener", packetListener);

            packetHandler.onServerSwitch();

        } catch (Exception ex) {
            btlp.getLogger().log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }


}
