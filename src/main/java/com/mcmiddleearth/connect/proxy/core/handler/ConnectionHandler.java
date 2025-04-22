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
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.base.core.taskScheduling.Callback;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionHandler {

    private static final Map<UUID, String> connectReasons = new HashMap<>();

    private static final ArrayList<UUID> welcomedPlayers = new ArrayList<>();

    public static boolean handleConnectPlayerToServer(String sender, String server, boolean welcomeMsg, Callback<Boolean> callback) {
//McmeConnect.getLogger().info("ConnectionHandler");
        McmeProxyPlayer source = McmeConnect.getProxy().getPlayer(sender);
        McmeServerInfo target = McmeConnect.getProxy().getServerInfo(server);
        if(target!=null && !source.getServerInfo().getName().equals(server)) {
            if(welcomeMsg) {
                ChatMessageHandler.handle(server, sender, McmeConnect.message("Welcome to '"+server+"'!",
                                                                                MessageColor.YELLOW),
                        McmeConnect.getConfig().getConnectDelay());
            }
//McmeConnect.getLogger().info("Connect!");
            source.connect(target,callback);
            return true;
        }
        return false;
    }

    public static void sendJoinMessage(McmeProxyPlayer player, boolean fake) {
        McmeConnect.getProxy().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport()
                        || !fake
                        || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> p.sendMessage(McmeConnect.message(player.getName()+" joined the game.",
                                                                MessageColor.YELLOW)));
        McmeConnect.getProxyPlugin().getTask( () -> {
            Iterator<McmeProxyPlayer> it = McmeConnect.getProxy().getPlayers().iterator();
            if(it.hasNext()) {
                McmeProxyPlayer other = it.next();
                if(other.getServerInfo()==null) {
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.DISCORD);
                out.writeUTF("Global");
                out.writeUTF(":bangbang: **"+player.getName()+" joined the game.**");
                other.getServerInfo().sendPluginMessage(Channel.MAIN, out.toByteArray(),true);
            }
        }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
    }

    public static void sendLeaveMessage(McmeProxyPlayer player, boolean fake) {
        McmeConnect.getProxy().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport()
                        || !fake
                        || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> p.sendMessage(McmeConnect.message(player.getName()+" left the game.",
                                                                 MessageColor.YELLOW)));
        McmeProxyPlayer other = getOtherPlayer(player);
        if(other != null && other.getServerInfo() != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.DISCORD);
            out.writeUTF("Global");
            out.writeUTF(":x: **"+player.getName()+" left the game.**");
            other.getServerInfo().sendPluginMessage(Channel.MAIN, out.toByteArray(),false);
        }
    }

    private static McmeProxyPlayer getOtherPlayer(McmeProxyPlayer player) {
        Iterator<McmeProxyPlayer> iterator = McmeConnect.getProxy().getPlayers().iterator();
        if(!iterator.hasNext()) return null;
        McmeProxyPlayer other = iterator.next();
        if(other.equals(player)) {
            if(!iterator.hasNext()) return null;
            other = iterator.next();
        }
        return other;
    }


    public static void handleServerConnect(McmeProxyPlayer player, String reason) {
        connectReasons.put(player.getUniqueId(), reason);
    }

    public static void handleServerConnected(McmeProxyPlayer player, McmeServerInfo destination) {
        McmeConnect.getProxyPlugin().getTask( () -> {
            McmeConnect.setPlayerServer(player, destination.getName());
            String reason = connectReasons.get(player.getUniqueId());
            if(reason!=null) {
//McmeConnect.getLogger().info("ConnectReason: "+reason);
                if(reason.equals("JOIN_PROXY")) {
                    LegacyPlayerHandler.handle(player, destination.getName());
                }
                connectReasons.remove(player.getUniqueId());
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.JOIN);
                out.writeUTF(player.getName());
                out.writeUTF(reason);
                destination.sendPluginMessage(Channel.MAIN, out.toByteArray(), true);
            }
        }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);

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
        if(RestorestatsHandler.getBlacklist().contains(player.getUniqueId())) {
            player.disconnect(McmeConnect.message(
                    "Your statistics are currently restored. Please wait a minute before rejoining.",
                    MessageColor.WHITE));
        }
        String playerName = player.getName();
        McmeConnect.getProxyPlugin().getTask( () -> {
            McmeProxyPlayer finalPlayer = McmeConnect.getProxy().getPlayer(playerName);
            if(finalPlayer != null) {
                if (!VanishHandler.isPvSupport()) {
                    sendJoinMessage(finalPlayer, false);
                } else {
                    VanishHandler.join(finalPlayer);
                }
                welcomedPlayers.add(finalPlayer.getUniqueId());
            }
        }).schedule(2, TimeUnit.SECONDS);

    }

    public static String chooseInitialServer(McmeProxyPlayer player) {
        String server = McmeConnect.getPlayerServer(player);
        if(server == null && LegacyPlayerHandler.getLegacyPlayers().contains(player.getUniqueId())) {
            server = "world";
        }
        return server;
    }

    public static KickResult handleKick(McmeProxyPlayer player, String kickServer, Message reason) {
        KickResult result = new KickResult();
        if(reason.toString().contains("unsupported")) {
            result.message = reason;
            result.redirect = false;
            return result;
        }
        result.redirect = !kickServer.equals("newplayer");
        if(result.redirect) {
            String server = (kickServer.equalsIgnoreCase("world")?"moria":"world");
            result.redirectServer = server;
            if(kickServer.equalsIgnoreCase("world")) {
                kickServer = "mainworld";
            }
            if(server.equalsIgnoreCase("world")) {
                server = "mainworld";
            }
            result.message = reason.add(" Trying to redirect you to "+server+" server.");
        } else {
            result.message = reason;
        }

        return result;
    }

    public static class KickResult {
        public Message message;
        public boolean redirect;
        public String redirectServer;
    }
}