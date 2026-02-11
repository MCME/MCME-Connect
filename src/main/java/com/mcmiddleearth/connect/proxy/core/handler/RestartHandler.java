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
import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.base.core.taskScheduling.Callback;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class RestartHandler {
    
    private static final File restartFile = new File(McmeConnect.getProxyPlugin().getDataFolder(),"restart.nfo");

    public static void init() {
        if(restartFile.exists()) {
            restartFile.delete();
        }
    }
    
    public static void handle(McmeProxyPlayer player, String[] message) {
        handle(player,message,false);
    }
    
    public static void handle(McmeProxyPlayer player, String[] message, boolean shutdown) {
        List<String> servers = new ArrayList<>();
        if(message[0].equalsIgnoreCase("all")) {
            servers.addAll(McmeConnect.getProxy().getAllServerInfo().stream().map(McmeServerInfo::getName).toList());
            servers.add("proxy");
        } else {
            for (String s : message) {
                if (McmeConnect.getProxy().getAllServerInfo().stream().anyMatch(info -> info.getName().equals(s))
                        || s.equals("proxy")) {
                    servers.add(s);
                }
            }
        }
        if(servers.size()>0) {
            String next = servers.get(0);
            while(servers.remove(next));
            if(next.equals("proxy")) {
                if(servers.size()>0) {
                    servers.add(next);
                    next = servers.get(0);
                    while(servers.remove(next));
                } else {
                    restartProxy(shutdown);
                    return;
                }
            }
            String finalNext = next;
            StringBuilder others = new StringBuilder();
            for(String name: servers) {
                others.append(name).append(" ");
            }
            String otherServers = others.toString();
            Callback<Boolean> callback = (connected, error) -> {
                if(connected) {
                    McmeConnect.getProxyPlugin().getTask( () -> {
                        McmeServerInfo dest = McmeConnect.getProxy().getServerInfo(finalNext);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(Channel.RESTART);
                        out.writeBoolean(shutdown);
                        out.writeUTF(player.getName());
                        out.writeUTF(otherServers);
                        dest.sendPluginMessage(Channel.MAIN, out.toByteArray(),true);
                    }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
                }
            };
            if(!ConnectionHandler.handleConnectPlayerToServer(player.getName(), next, false, callback)) {
                callback.done(true, null);
            }
        }
    }
    
    public static void restartProxy(boolean shutdown) {
        McmeConnect.getProxyPlugin().getTask( () -> {
            if(!shutdown && !restartFile.exists()) {
                try {
                    restartFile.createNewFile();
                } catch (IOException ex) {
                    McmeConnect.getProxyPlugin().getMcmeLogger().error("IOException", ex);
                }
                McmeConnect.getProxy().stop(McmeConnect.message("MCME network is restarting.", MessageColor.GRAY));
            } else {
                McmeConnect.getProxy().stop(McmeConnect.message("MCME network is shutting down.", MessageColor.GRAY));
            }
        }).schedule(5, TimeUnit.SECONDS);
    }
}
