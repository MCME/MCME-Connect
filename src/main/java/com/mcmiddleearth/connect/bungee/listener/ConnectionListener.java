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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.Handler.LegacyPlayerHandler;
import com.mcmiddleearth.connect.bungee.Handler.RestorestatsHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpaHandler;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionListener implements Listener {

    ArrayList<String> priorities = new ArrayList<>();
    
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
        if(RestorestatsHandler.getBlacklist().contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().disconnect(new ComponentBuilder(
                                    "Your statistics are currently restored. Please wait a minute before rejoining.")
                                    .color(ChatColor.WHITE).create());
        }
        ProxiedPlayer player = event.getPlayer();
        if(!VanishHandler.isPvSupport()) {
            sendJoinMessage(player,false);
        } else {
            VanishHandler.join(player);
        }
    }
    
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        TpaHandler.removeRequests(player);
        if(!VanishHandler.isPvSupport()) {
            sendLeaveMessage(player,false);
        } else {
            VanishHandler.quit(player);
        }
    }
    
    @EventHandler
    public void handleLegacyPlayers(ServerConnectEvent event) {
        if(event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            if(!ConnectBungeePlugin.isLegacyRedirectEnabled()) {
                return;
            }
            if(ConnectBungeePlugin.getLegacyPlayers().contains(event.getPlayer().getUniqueId())
                    && event.getTarget().getName().equals(ConnectBungeePlugin.getLegacyRedirectFrom())) {
                //event.setTarget(ProxyServer.getInstance().getServerInfo(ConnectBungeePlugin.getLegacyRedirectTo()));
                LegacyPlayerHandler.handle(event.getPlayer(),
                                           ConnectBungeePlugin.getLegacyRedirectFrom(),
                                           ConnectBungeePlugin.getLegacyRedirectTo());
            }
        }
    }
    
    public static void sendJoinMessage(ProxiedPlayer player, boolean fake) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport() 
                          || !fake 
                          || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" joined the game.")
                                            .color(ChatColor.YELLOW).create());
        });
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            Iterator<ProxiedPlayer> it = ProxyServer.getInstance().getPlayers().iterator();
            if(it.hasNext()) {
                ProxiedPlayer other = it.next();
                if(other.getServer()==null) {
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.DISCORD);
                out.writeUTF("Global");
                out.writeUTF(":bangbang: **"+player.getName()+" joined the game.**");
                other.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(),true);
            }
        }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
    }
    
    public static void sendLeaveMessage(ProxiedPlayer player, boolean fake) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport() 
                          || !fake 
                          || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" left the game.")
                                            .color(ChatColor.YELLOW).create());
        });
        ProxiedPlayer other = getOtherPlayer(player); 
        if(other != null && other.getServer() != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.DISCORD);
            out.writeUTF("Global");
            out.writeUTF(":x: **"+player.getName()+" left the game.**");
            other.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(),false);
        }
    }

    private static ProxiedPlayer getOtherPlayer(ProxiedPlayer player) {
        Iterator<ProxiedPlayer> iterator = ProxyServer.getInstance().getPlayers().iterator();
        if(!iterator.hasNext()) return null;
        ProxiedPlayer other = iterator.next();
        if(other.equals(player)) {
            if(!iterator.hasNext()) return null;
            other = iterator.next();
        }
        return other;
    }
    
    private final Map<ProxiedPlayer, ServerConnectEvent.Reason> connectReasons = new HashMap<>();
    
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        connectReasons.put(event.getPlayer(),event.getReason());
    }
    
    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            ProxiedPlayer player = event.getPlayer();
            ServerConnectEvent.Reason reason = connectReasons.get(player);
            if(reason!=null) {
                ServerInfo dest = event.getServer().getInfo();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.JOIN);
                out.writeUTF(player.getName());
                out.writeUTF(reason.name());
                connectReasons.remove(player);
                dest.sendData(Channel.MAIN, out.toByteArray(), true);
            }
        }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
    }

}
