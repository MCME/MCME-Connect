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

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

/**
 *
 * @author Eriol_Eandur
 */
public class TpaHandler {
    
    private static List<TpaRequest> requests = new ArrayList<>();
   
    private final static long REQUEST_PERIOD = 120000; // in milliseconds
    
    public static void sendRequest(ProxiedPlayer sender, ServerInfo server, ProxiedPlayer target) {
        if(requests.stream().anyMatch(request -> request.getSender().getName().equalsIgnoreCase(sender.getName())
                                              && request.getTarget().getName().equalsIgnoreCase(target.getName()))) {
            sender.sendMessage(new ComponentBuilder("You already sent "+target.getName()+" a teleport request.")
                .color(ChatColor.RED).create());  
            return;
        }
        removeRequestsForSender(sender);
        requests.add(new TpaRequest(sender, server,target));
        sender.sendMessage(new ComponentBuilder("Teleport request sent to "
                +ChatColor.RED+target.getName()+ChatColor.GOLD+".\n"
                +"To cancel this request, type "+ChatColor.RED+"/tpcancel"+ChatColor.GOLD+".")
            .color(ChatColor.GOLD).create());  
        target.sendMessage(new ComponentBuilder(ChatColor.RED+sender.getName()
                + ChatColor.GOLD+" has requested to teleport to you.\n"
                +"To teleport, type "+ChatColor.RED+"/tpaccept"+ChatColor.GOLD+"\n"
                +"To deny this request, type "+ChatColor.RED+"/tpdeny"+ChatColor.GOLD+"\n"
                +"This request will timeout after "+ChatColor.RED+"120 seconds"+ChatColor.GOLD+".")
            .color(ChatColor.GOLD).create());  
    }
    
    public static void accept(ProxiedPlayer player) {
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> {
            if(!TpHandler.handle(request.getSender().getName(), 
                                 request.getServer().getName(), 
                                 request.getTarget().getName())) {
                request.getSender().sendMessage(new ComponentBuilder("There was an error with your teleportation!")
                    .color(ChatColor.RED).create());  
            } else {
                request.getSender().sendMessage(new ComponentBuilder("Teleport request accepted.")
                    .color(ChatColor.GOLD).create());  
                request.getTarget().sendMessage(new ComponentBuilder(ChatColor.RED+""+request.getTarget().getName()
                        +ChatColor.GOLD+" accepted your teleport request.\n"
                        +"Teleporting to "+ChatColor.RED+request.getTarget().getName()+ChatColor.GOLD+".")
                    .color(ChatColor.GOLD).create());  
            }
        });
        removeRequestsForTarget(player);
    }
    
    public static void deny(ProxiedPlayer player) {
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> {
            request.getSender().sendMessage(new ComponentBuilder(""+ChatColor.RED+request.getTarget().getName()
                    +ChatColor.GOLD+" denied your teleport request.")
                .color(ChatColor.GOLD).create());  
        });
        player.sendMessage(new ComponentBuilder("Teleport request denied.")
            .color(ChatColor.GOLD).create());  
        removeRequestsForTarget(player);
    }
    
    public static void cancel(ProxiedPlayer player) {
        removeRequestsForSender(player);
        player.sendMessage(new ComponentBuilder("All outstanding teleport requests cancelled.")
            .color(ChatColor.GOLD).create());  
        removeRequestsForTarget(player);
    }
    
    
    public static void removeRequestsForSender(ProxiedPlayer sender) {
        List<TpaRequest> removal = new ArrayList<>();
        requests.stream().filter(request->request.getSender().getName().equalsIgnoreCase(sender.getName()))
                         .forEach(request->removal.add(request));
        requests.removeAll(removal);
    }
    
    public static void removeRequestsForTarget(ProxiedPlayer target) {
        List<TpaRequest> removal = new ArrayList<>();
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(target.getName()))
                         .forEach(request->removal.add(request));
        requests.removeAll(removal);
    }
    
    public static void removeRequests(ProxiedPlayer player) {
        removeRequestsForTarget(player);
        removeRequestsForSender(player);
    }
    
    public static boolean hasPendingRequest(ProxiedPlayer target) {
        return requests.stream().anyMatch(request->request.getTarget().getName().equalsIgnoreCase(target.getName()));
    }
    
    public static ScheduledTask startCleanupScheduler() {
        List<TpaRequest> removal = new ArrayList<>();
        return ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                long time = System.currentTimeMillis();
                requests.stream().filter(request -> request.getTimestamp()+REQUEST_PERIOD<time)
                     .forEach(request -> {
                         removal.add(request);
                         request.getSender().sendMessage(new ComponentBuilder("Your teleportation request timed out!")
                              .color(ChatColor.RED).create());  
                       });
                requests.removeAll(removal);
        }, 20, 20, TimeUnit.SECONDS);
    }
    
    public static class TpaRequest {
        
        @Getter
        private ProxiedPlayer sender, target;
        @Getter
        private ServerInfo server;
        @Getter
        private long timestamp;
       
        
        public TpaRequest(ProxiedPlayer sender, ServerInfo server, ProxiedPlayer target) {
            this.sender = sender;
            this.server = server;
            this.target = target;
            timestamp = System.currentTimeMillis();
        }
    }
}
