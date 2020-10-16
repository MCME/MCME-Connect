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
package com.mcmiddleearth.connect.bungee.Handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.ConnectPlugin;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public class RestartHandler {
    
    private static File restartFile = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"restart.nfo");

    public static void init() {
        if(restartFile.exists()) {
            restartFile.delete();
        }
    }
    
    public static void handle(ProxiedPlayer player, String[] message) {
        handle(player,message,false);
    }
    
    public static void handle(ProxiedPlayer player, String[] message, boolean shutdown) {
        List<String> servers = new ArrayList<>();
        if(message[0].equalsIgnoreCase("all")) {
            servers.addAll(ProxyServer.getInstance().getServers().keySet());
            servers.add("proxy");
        } else {
            for(int i = 0; i<message.length;i++) {
                if(ProxyServer.getInstance().getServers().keySet().contains(message[i])
                        || message[i].equals("proxy")) {
                    servers.add(message[i]);
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
            String others = "";
            for(String name: servers) {
                others = others + name+" ";
            }
            String otherServers = others;
            Callback<Boolean> callback = (connected, error) -> {
                if(connected) {
                    ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                        ServerInfo dest = ProxyServer.getInstance().getServerInfo(finalNext);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(Channel.RESTART);
                        out.writeBoolean(shutdown);
                        out.writeUTF(player.getName());
                        out.writeUTF(otherServers);
                        dest.sendData(Channel.MAIN, out.toByteArray(),true);   
                    }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
                }
            };
            if(!ConnectHandler.handle(player.getName(), next, false, callback)) {
                callback.done(true, null);
            }
        }
    }
    
    public static void restartProxy(boolean shutdown) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            if(!shutdown && !restartFile.exists()) {
                try {
                    restartFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(RestartHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                ProxyServer.getInstance().stop("MCME network is restarting.");
            } else {
                ProxyServer.getInstance().stop("MCME network is shutting down.");
            }
        }, 5, TimeUnit.SECONDS);
    }
}
