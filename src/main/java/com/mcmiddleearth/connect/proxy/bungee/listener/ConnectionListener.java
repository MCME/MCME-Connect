/*
 * Copyright (C) 2019 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.connect.proxy.bungee.listener;

import com.mcmiddleearth.base.bungee.server.BungeeMcmeServerInfo;
import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.ConnectionHandler;
import com.mcmiddleearth.connect.proxy.core.handler.LegacyPlayerHandler;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionListener implements Listener {

    public ConnectionListener() {
        //priorities.add("world");
        //priorities.add("moria");
        //priorities.add("plotworld");
        //priorities.add("themedbuilds");
        //priorities.add("freebuild");
        //priorities.add("newplayerworld");
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ConnectionHandler.handlePlayerJoin(((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer(event.getPlayer()));
    }
    
    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        ConnectionHandler.handlePlayerLeave(((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer(event.getPlayer()));
    }
    
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            LegacyPlayerHandler.handle(((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer(event.getPlayer()),
                                       event.getTarget().getName());
        }
        ConnectionHandler.handleServerConnect(((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer(event.getPlayer()),
                                                event.getReason().name());
    }
    
    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ConnectionHandler.handleServerConnected(((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer(event.getPlayer()),
                                                new BungeeMcmeServerInfo(event.getServer().getInfo()));
    }

}
