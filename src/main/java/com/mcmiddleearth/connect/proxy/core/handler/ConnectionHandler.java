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
package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.base.core.taskScheduling.Callback;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionHandler {

    private static final Map<McmeProxyPlayer, String> connectReasons = new HashMap<>();

    private static final ArrayList<String> priorities = new ArrayList<>();
    private static final ArrayList<UUID> welcomedPlayers = new ArrayList<>();

    public static boolean handleConnectPlayerToServer(String sender, String server, boolean welcomeMsg, Callback<Boolean> callback) {
        McmeProxyPlayer source = McmeConnect.getProxyPlugin().getPlayer(sender);
        McmeServerInfo target = McmeConnect.getProxy().getServerInfo(server);
        Server origin = source.getServer();
        if(target!=null && !origin.getInfo().getName().equals(server)) {
            if(welcomeMsg) {
                ChatMessageHandler.handle(server, sender, NamedTextColor.YELLOW+"Welcome to '"+server+"'!",
                        McmeConnect.getConfig().getConnectDelay());
            }
            source.connect(target,callback);
            return true;
        }
        return false;
    }

    public static void sendJoinMessage(McmeProxyPlayer player, boolean fake) {
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
        }, McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
    }

    public static void sendLeaveMessage(McmeProxyPlayer player, boolean fake) {
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

    private static McmeProxyPlayer getOtherPlayer(McmeProxyPlayer player) {
        Iterator<ProxiedPlayer> iterator = ProxyServer.getInstance().getPlayers().iterator();
        if(!iterator.hasNext()) return null;
        ProxiedPlayer other = iterator.next();
        if(other.equals(player)) {
            if(!iterator.hasNext()) return null;
            other = iterator.next();
        }
        return other;
    }


    public static void handleServerConnect(McmeProxyPlayer player, String reason) {
        connectReasons.put(player, reason);
    }

    public static void handleServerConnected(McmeProxyPlayer player, McmeServerInfo destination) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            String reason = connectReasons.get(player);
            if(reason!=null) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.JOIN);
                out.writeUTF(player.getName());
                out.writeUTF(reason);
                connectReasons.remove(player);
                destination.sendData(Channel.MAIN, out.toByteArray(), true);
            }
        }, McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);

    }

    public static void handlePlayerLeave(McmeProxyPlayer player) {
        if(welcomedPlayers.contains(player.getUniqueId())) {
            TpaHandler.removeRequests(player);
            if (!VanishHandler.isPvSupport()) {
                sendLeaveMessage(player, false);
            } else {
                VanishHandler.quit(player);
            }
            welcomedPlayers.remove(player.getUniqueId());
        }

    }

    public static void handlePlayerJoin(McmeProxyPlayer player) {
        if(RestorestatsHandler.getBlacklist().contains(event.getPlayer().getUniqueId())) {
            player.disconnect(Component.text(
                    "Your statistics are currently restored. Please wait a minute before rejoining.")
                    .color(NamedTextColor.WHITE));
        }
        McmeConnect.getProxyPlugin().getTask( () -> {
            if(player.isConnected()) {
                if (!VanishHandler.isPvSupport()) {
                    sendJoinMessage(player, false);
                } else {
                    VanishHandler.join(player);
                }
                welcomedPlayers.add(player.getUniqueId());
            }
        }).schedule(2, TimeUnit.SECONDS);

    }
}