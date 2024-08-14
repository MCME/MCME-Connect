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
package com.mcmiddleearth.connect.proxy.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.base.velocity.server.VelocityMcmeServerInfo;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.ConnectionHandler;
import com.mcmiddleearth.connect.proxy.core.handler.LegacyPlayerHandler;
import com.mcmiddleearth.connect.proxy.velocity.ConnectVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionListener {

    public ConnectionListener() {
        //priorities.add("world");
        //priorities.add("moria");
        //priorities.add("plotworld");
        //priorities.add("themedbuilds");
        //priorities.add("freebuild");
        //priorities.add("newplayerworld");
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        ConnectionHandler.handlePlayerJoin(new VelocityMcmePlayer(event.getPlayer()));
    }
    
    @Subscribe
    public void onLeave(DisconnectEvent event) {
        ConnectionHandler.handlePlayerLeave(new VelocityMcmePlayer(event.getPlayer()));
    }
    
    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        String joinReason;
        if(event.getPreviousServer() == null) {
            LegacyPlayerHandler.handle(new VelocityMcmePlayer(event.getPlayer()),
                                       event.getOriginalServer().getServerInfo().getName());
            joinReason = "JOIN_PROXY";
        } else if(!event.getOriginalServer().equals(event.getResult().getServer().orElse(null))) {
            joinReason = "SERVER_DOWN_REDIRECT";
        } else {
            joinReason = "UNKNOWN";
        }
        ConnectionHandler.handleServerConnect(new VelocityMcmePlayer(event.getPlayer()), joinReason);
    }
    
    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        ConnectionHandler.handleServerConnected(new VelocityMcmePlayer(event.getPlayer()),
                        new VelocityMcmeServerInfo(((ConnectVelocityPlugin)McmeConnect.getPlugin()).getProxyServer(),
                                                    event.getPlayer().getCurrentServer().orElseThrow().getServerInfo()));
    }

}
