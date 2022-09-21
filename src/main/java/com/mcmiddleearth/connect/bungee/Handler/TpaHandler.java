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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class TpaHandler {
    
    private final static List<TpaRequest> requests = new ArrayList<>();
   
    private final static long REQUEST_PERIOD = 120000; // in milliseconds
    
    public static void sendRequest(ProxiedPlayer sender, ProxiedPlayer target) {
        if(requests.stream().anyMatch(request -> request.getSender().getName().equalsIgnoreCase(sender.getName())
                                              && request.getTarget().getName().equalsIgnoreCase(target.getName()))) {
            ConnectBungeePlugin.getAudience(sender)
                    .sendMessage(Component.text("You already sent "+target.getName()+" a teleport request.")
                                    .color(NamedTextColor.RED));
            return;
        }
        removeRequestsForSender(sender);
        requests.add(new TpaRequest(sender, target));
        ConnectBungeePlugin.getAudience(sender)
                .sendMessage(Component.text("Teleport request sent to ").color(NamedTextColor.GOLD)
                                .append(Component.text(target.getName()).color(NamedTextColor.RED))
                                .append(Component.text(".\nTo cancel this request, type ").color(NamedTextColor.GOLD))
                                .append(Component.text("/tpacancel").color(NamedTextColor.RED))
                                .append(Component.text(".").color(NamedTextColor.GOLD)));
        ConnectBungeePlugin.getAudience(target)
                .sendMessage(Component.text(sender.getName()).color(NamedTextColor.RED)
                        .append(Component.text(" has requested to teleport to you.\n").color(NamedTextColor.GOLD))
                        .append(Component.text("To teleport, type ").color(NamedTextColor.GOLD))
                        .append(Component.text("/tpaccept").color(NamedTextColor.RED))
                        .append(Component.text("\nTo deny this request, type ").color(NamedTextColor.GOLD))
                        .append(Component.text("/tpdeny").color(NamedTextColor.RED))
                        .append(Component.text("\nThis request will timeout after ").color(NamedTextColor.GOLD))
                        .append(Component.text("120 seconds").color(NamedTextColor.RED))
                        .append(Component.text(".").color(NamedTextColor.GOLD)));
    }
    
    public static boolean accept(ProxiedPlayer player) {
        if(!hasPendingRequest(player)) {
            return false;
        }
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> {
            if(!TpHandler.handle(request.getSender().getName(), 
                                 request.getTarget().getServer().getInfo().getName(), 
                                 request.getTarget().getName())) {
                ConnectBungeePlugin.getAudience(request.getSender())
                        .sendMessage(Component.text("There was an error with your teleportation!")
                                        .color(NamedTextColor.RED));
            } else {
                ConnectBungeePlugin.getAudience(request.getTarget())
                        .sendMessage(Component.text("Teleport request accepted.")
                                .color(NamedTextColor.GOLD));
                ConnectBungeePlugin.getAudience(request.getSender())
                        .sendMessage(Component.text(request.getTarget().getName()).color(NamedTextColor.RED)
                                .append(Component.text(" accepted your teleport request.").color(NamedTextColor.GOLD)));
            }
        });
        removeRequestsForTarget(player);
        return true;
    }
    
    public static boolean deny(ProxiedPlayer player) {
        if(!hasPendingRequest(player)) {
            return false;
        }
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> {
             ConnectBungeePlugin.getAudience(request.getSender())
                     .sendMessage(Component.text(request.getTarget().getName()).color(NamedTextColor.RED)
                     .append(Component.text(" denied your teleport request.").color(NamedTextColor.GOLD)));
        });
        ConnectBungeePlugin.getAudience(player).sendMessage(Component.text("Teleport request denied.")
                .color(NamedTextColor.GOLD));
        removeRequestsForTarget(player);
        return true;
    }
    
    public static boolean cancel(ProxiedPlayer player) {
        if(!hasOutstandingRequest(player)) {
            return false;
        }
        removeRequestsForSender(player);
        ConnectBungeePlugin.getAudience(player).sendMessage(Component.text("All outstanding teleport requests cancelled.")
                .color(NamedTextColor.GOLD));
        removeRequestsForTarget(player);
        return true;
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
    
    public static boolean hasOutstandingRequest(ProxiedPlayer sender) {
        return requests.stream().anyMatch(request->request.getSender().getName().equalsIgnoreCase(sender.getName()));
    }
    
    public static ScheduledTask startCleanupScheduler() {
        List<TpaRequest> removal = new ArrayList<>();
        return ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                long time = System.currentTimeMillis();
                requests.stream().filter(request -> request.getTimestamp()+REQUEST_PERIOD<time)
                     .forEach(request -> {
                         removal.add(request);
                         ConnectBungeePlugin.getAudience(request.getSender())
                                 .sendMessage(Component.text("Your teleportation request timed out!")
                                         .color(NamedTextColor.RED));
                       });
                requests.removeAll(removal);
        }, 20, 20, TimeUnit.SECONDS);
    }
    
    public static class TpaRequest {
        
        private ProxiedPlayer sender, target;
        private long timestamp;
       
        
        public TpaRequest(ProxiedPlayer sender, ProxiedPlayer target) {
            this.sender = sender;
            this.target = target;
            timestamp = System.currentTimeMillis();
        }

        public ProxiedPlayer getSender() {
            return sender;
        }

        public ProxiedPlayer getTarget() {
            return target;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
