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

import com.mcmiddleearth.base.adventure.AdventureMessage;
import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.base.velocity.server.VelocityMcmeProxy;
import com.mcmiddleearth.base.velocity.server.VelocityMcmeServerInfo;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.ConnectionHandler;
import com.mcmiddleearth.connect.proxy.velocity.ConnectVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
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
McmeConnect.getLogger().info("onJoin: "+event.getPlayer().getUsername());
        ConnectionHandler.handlePlayerJoin(new VelocityMcmePlayer(event.getPlayer()));
    }
    
    @Subscribe
    public void onLeave(DisconnectEvent event) {
McmeConnect.getLogger().info("onDiconnect: "+event.getPlayer().getUsername());
        ConnectionHandler.handlePlayerLeave(new VelocityMcmePlayer(event.getPlayer()));
    }
    
    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
//McmeConnect.getLogger().info("onServerConnect: "+event.getPlayer().getUsername());
        String joinReason;
        if(event.getPreviousServer() == null) {
            joinReason = "JOIN_PROXY";
        } else if(!event.getOriginalServer().equals(event.getResult().getServer().orElse(null))) {
            joinReason = "SERVER_DOWN_REDIRECT";
        } else {
            joinReason = "UNKNOWN";
        }
//McmeConnect.getLogger().info("reason: "+joinReason);
        ConnectionHandler.handleServerConnect(new VelocityMcmePlayer(event.getPlayer()), joinReason);
    }

    @Subscribe
    public void onServerSelect(PlayerChooseInitialServerEvent event) {
        String target = ConnectionHandler.chooseInitialServer(new VelocityMcmePlayer(event.getPlayer()));
        if(target != null) {
            ((ConnectVelocityPlugin) McmeConnect.getPlugin()).getProxyServer()
                    .getServer(target).ifPresent(event::setInitialServer);
        }
    }
    
    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
McmeConnect.getLogger().info("onServerConnected: "+event.getPlayer().getUsername());
        ConnectionHandler.handleServerConnected(new VelocityMcmePlayer(event.getPlayer()),
                        new VelocityMcmeServerInfo(((ConnectVelocityPlugin)McmeConnect.getPlugin()).getProxyServer(),
                                                    event.getPlayer().getCurrentServer().orElseThrow().getServerInfo()));
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
//McmeConnect.getLogger().warn("Kicked from: "+event.getServer().getServerInfo().getName());
        ConnectionHandler.KickResult kickResult = ConnectionHandler.handleKick(VelocityMcmeProxy.getPlayer(event.getPlayer()),
                                                            event.getServer().getServerInfo().getName());
        if(kickResult.redirect) {
            event.setResult(KickedFromServerEvent.RedirectPlayer
                    .create(((ConnectVelocityPlugin) McmeConnect.getPlugin()).getProxyServer()
                            .getServer(kickResult.redirectServer)
                            .orElseThrow()));
            event.getPlayer().sendMessage(((AdventureMessage)kickResult.message).getComponent());
        } else {
            event.setResult(KickedFromServerEvent.DisconnectPlayer
                    .create(((AdventureMessage)kickResult.message).getComponent()));
        }

    }

}
