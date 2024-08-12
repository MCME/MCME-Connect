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

import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.taskScheduling.Task;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author Eriol_Eandur
 */
public class TpaHandler {
    
    private final static List<TpaRequest> requests = new ArrayList<>();
   
    private final static long REQUEST_PERIOD = 120000; // in milliseconds
    
    public static void sendRequest(McmeProxyPlayer sender, McmeProxyPlayer target) {
        if(requests.stream().anyMatch(request -> request.getSender().getName().equalsIgnoreCase(sender.getName())
                                              && request.getTarget().getName().equalsIgnoreCase(target.getName()))) {
            sender.sendMessage(Component.text("You already sent "+target.getName()+" a teleport request.")
                                    .color(NamedTextColor.RED));
            return;
        }
        removeRequestsForSender(sender);
        requests.add(new TpaRequest(sender, target));
        sender.sendMessage(Component.text("Teleport request sent to ").color(NamedTextColor.GOLD)
                                .append(Component.text(target.getName()).color(NamedTextColor.RED))
                                .append(Component.text(".\nTo cancel this request, type ").color(NamedTextColor.GOLD))
                                .append(Component.text("/tpacancel").color(NamedTextColor.RED))
                                .append(Component.text(".").color(NamedTextColor.GOLD)));
        sender.sendMessage(Component.text(sender.getName()).color(NamedTextColor.RED)
                        .append(Component.text(" has requested to teleport to you.\n").color(NamedTextColor.GOLD))
                        .append(Component.text("To teleport, type ").color(NamedTextColor.GOLD))
                        .append(Component.text("/tpaccept").color(NamedTextColor.RED))
                        .append(Component.text("\nTo deny this request, type ").color(NamedTextColor.GOLD))
                        .append(Component.text("/tpdeny").color(NamedTextColor.RED))
                        .append(Component.text("\nThis request will timeout after ").color(NamedTextColor.GOLD))
                        .append(Component.text("120 seconds").color(NamedTextColor.RED))
                        .append(Component.text(".").color(NamedTextColor.GOLD)));
    }
    
    public static boolean accept(McmeProxyPlayer player) {
        if(!hasPendingRequest(player)) {
            return false;
        }
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> {
            if(!TpHandler.handle(request.getSender().getName(),
                                 request.getTarget().getServerInfo().getName(),
                                 request.getTarget().getName())) {
                request.getSender().sendMessage(Component.text("There was an error with your teleportation!")
                                        .color(NamedTextColor.RED));
            } else {
                request.getTarget().sendMessage(Component.text("Teleport request accepted.")
                                .color(NamedTextColor.GOLD));
                request.getSender().sendMessage(Component.text(request.getTarget().getName()).color(NamedTextColor.RED)
                                .append(Component.text(" accepted your teleport request.").color(NamedTextColor.GOLD)));
            }
        });
        removeRequestsForTarget(player);
        return true;
    }
    
    public static boolean deny(McmeProxyPlayer player) {
        if(!hasPendingRequest(player)) {
            return false;
        }
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(player.getName()))
                         .forEach(request-> request.getSender().sendMessage(Component.text(request.getTarget().getName()).color(NamedTextColor.RED)
                                 .append(Component.text(" denied your teleport request.").color(NamedTextColor.GOLD))));
        player.sendMessage(Component.text("Teleport request denied.")
                .color(NamedTextColor.GOLD));
        removeRequestsForTarget(player);
        return true;
    }
    
    public static boolean cancel(McmeProxyPlayer player) {
        if(!hasOutstandingRequest(player)) {
            return false;
        }
        removeRequestsForSender(player);
        player.sendMessage(Component.text("All outstanding teleport requests cancelled.")
                .color(NamedTextColor.GOLD));
        removeRequestsForTarget(player);
        return true;
    }
    
    public static void removeRequestsForSender(McmeProxyPlayer sender) {
        List<TpaRequest> removal = new ArrayList<>();
        requests.stream().filter(request->request.getSender().getName().equalsIgnoreCase(sender.getName()))
                         .forEach(removal::add);
        requests.removeAll(removal);
    }
    
    public static void removeRequestsForTarget(McmeProxyPlayer target) {
        List<TpaRequest> removal = new ArrayList<>();
        requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(target.getName()))
                         .forEach(removal::add);
        requests.removeAll(removal);
    }
    
    public static void removeRequests(McmeProxyPlayer player) {
        removeRequestsForTarget(player);
        removeRequestsForSender(player);
    }
    
    public static boolean hasPendingRequest(McmeProxyPlayer target) {
        return requests.stream().anyMatch(request->request.getTarget().getName().equalsIgnoreCase(target.getName()));
    }

    public static Collection<McmeProxyPlayer> getRequestSender(McmeProxyPlayer target) {
        return requests.stream().filter(request->request.getTarget().getName().equalsIgnoreCase(target.getName()))
                .map(TpaRequest::getSender).collect(Collectors.toSet());
    }
    
    public static boolean hasOutstandingRequest(McmeProxyPlayer sender) {
        return requests.stream().anyMatch(request->request.getSender().getName().equalsIgnoreCase(sender.getName()));
    }
    
    public static Task startCleanupScheduler() {
        List<TpaRequest> removal = new ArrayList<>();
        Task task = McmeConnect.getProxyPlugin().getTask( () -> {
                long time = System.currentTimeMillis();
                requests.stream().filter(request -> request.getTimestamp()+REQUEST_PERIOD<time)
                     .forEach(request -> {
                         removal.add(request);
                         request.getSender().sendError(Component.text("Your teleportation request timed out!"));
                       });
                requests.removeAll(removal);
        });
        task.scheduleRepeating(20, 20, TimeUnit.SECONDS);
        return task;
    }
    
    public static class TpaRequest {
        
        private final McmeProxyPlayer sender;
        private final McmeProxyPlayer target;
        private final long timestamp;
       
        
        public TpaRequest(McmeProxyPlayer sender, McmeProxyPlayer target) {
            this.sender = sender;
            this.target = target;
            timestamp = System.currentTimeMillis();
        }

        public McmeProxyPlayer getSender() {
            return sender;
        }

        public McmeProxyPlayer getTarget() {
            return target;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
