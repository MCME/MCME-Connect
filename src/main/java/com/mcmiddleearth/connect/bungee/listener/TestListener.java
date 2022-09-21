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
package com.mcmiddleearth.connect.bungee.listener;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class TestListener implements Listener {
    
    @EventHandler
    public void onHandshake(PlayerHandshakeEvent event) {
        log("Handshake Protokoll: "+event.getHandshake().getProtocolVersion());
        log("Handshake requested Protokoll: "+event.getHandshake().getRequestedProtocol());
        log("Handshake requested name: "+event.getConnection().getName());
    }
    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        log("PreLogin name: "+event.getConnection().getName());
        log("PreLogin Cancel Reasons: "+concat(event.getCancelReasonComponents()));
        log("PreLogin version: "+event.getConnection().getVersion());
    }
    @EventHandler
    public void onLogin(LoginEvent event) {
        log("Login name: "+event.getConnection().getName());
        log("Login Cancel Reasons: "+concat(event.getCancelReasonComponents()));
        log("PostLogin version: "+event.getConnection().getVersion());
    }
    public void onPostLogin(PostLoginEvent event) {
        log("PostLogin name: "+event.getPlayer().getName());
        log("PostLogin reconnect: "+event.getPlayer().getReconnectServer());
        log("PostLogin version: "+event.getPlayer().getPendingConnection().getVersion());
    }
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        log("Disconnect name: "+event.getPlayer().getName());
        log("Disconnect reconnect: "+event.getPlayer().getReconnectServer());
    }
    public void onServerConnect(ServerConnectEvent event) {
        log("ServerConnect name: "+event.getPlayer().getName());
        log("ServerConnect reason: "+event.getReason().name());
        log("ServerConnect target: "+event.getTarget().getName());
        log("ServerConnect reconnect: "+event.getPlayer().getReconnectServer());
    }
    
    public void onServerConnected(ServerConnectedEvent event) {
        log("ServerConnected name: "+event.getPlayer().getName());
        log("ServerConnected server: "+event.getServer().getInfo().getName());
        log("ServerConnected reconnect: "+event.getPlayer().getReconnectServer());
    }
    public void onServerDisconnect(ServerDisconnectEvent event) {
        log("ServerDisonnect name: "+event.getPlayer().getName());
        log("ServerDisonnect target: "+event.getTarget().getName());
        log("ServerDisonnect reconnect: "+event.getPlayer().getReconnectServer());
    }
    public void onServerKick(ServerKickEvent event) {
        log("ServerKick name: "+event.getPlayer().getName());
        log("ServerKick reason: "+concat(event.getKickReasonComponent()));
        log("ServerKick cancel Server: "+event.getCancelServer().getName());
        log("ServerKick from Server: "+event.getKickedFrom().getName());
        log("ServerKick state: "+event.getState().name());
        log("ServerKick reconnect: "+event.getPlayer().getReconnectServer());
    }
    public void onServerSwitch(ServerSwitchEvent event) {
        log("ServerSwitch name: "+event.getPlayer().getName());
        log("ServerSwitch reconnect: "+event.getPlayer().getReconnectServer());
    }
    
    private String concat(BaseComponent[] messages) {
        String result = "";
        if(messages != null) {
            for(BaseComponent comp: messages) {
                result = result+" "+comp.toPlainText();
            }
        }
        return result;
    }
    
    private void log(String message) {
        Logger.getLogger(ConnectBungeePlugin.class.getName()).log(Level.INFO, message);
    }
}
