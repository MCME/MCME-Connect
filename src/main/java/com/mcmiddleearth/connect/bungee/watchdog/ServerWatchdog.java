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
package com.mcmiddleearth.connect.bungee.watchdog;

import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ServerWatchdog {

    ScheduledTask watchdog;

    int counter = 0;
    
    List<String> downList = new ArrayList<>();
    
    List<String> upList = new ArrayList<>();
    
    
    public ServerWatchdog() {
        watchdog = ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            downList.clear();
            upList.clear();
            counter++;
            ProxyServer.getInstance().getServers().forEach((name,info) -> {
                String finalName = name;
                info.ping((result, error) -> {
                    if(error!=null && !(error.getMessage()!=null && error.getMessage().equals(""))) {
                        downList.add(finalName);
                    } else {
                        upList.add(finalName);
                    }
                });
            });
            if(true || counter==12) {
                ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(),() -> {
                    if(!downList.isEmpty()) {
                        String downserver = "";
                        String separator = "";
                        downList.sort((one,two) -> (one==null?-1:(two==null?1:one.compareToIgnoreCase(two))));
                        for(int i=0; i<downList.size(); i++) {
                            downserver = downserver + separator + ChatColor.DARK_RED+downList.get(i);
                            separator = ", ";
                        }
                        int last = downserver.lastIndexOf(',');
                        if(last>0) {
                            downserver = downserver.substring(0, last)+ChatColor.RED+" and"
                                        +ChatColor.DARK_RED+downserver.substring(last+1);
                        }
                        String finalDown = downserver;
                        String single = (downList.size()>1?"":"s");
                        ProxyServer.getInstance().getPlayers().stream()
                                   .filter(player -> player.hasPermission(Permission.WATCHDOG))
                                   .forEach(player -> player.sendMessage(
                                           new ComponentBuilder("WARNING! Server "+ChatColor.DARK_RED
                                                                        +finalDown+ChatColor.RED+" seem"
                                                                        +single+ " to be down.")
                                                               .color(ChatColor.RED).create()));
                    }
                }, 10, TimeUnit.SECONDS);
                counter = 0;
            }
        }, 2, 2, TimeUnit.MINUTES);
    }  
    
    public void stopWatchdog() {
        watchdog.cancel();
    }

    public List<String> getUpList() {
        return upList;
    }
}
