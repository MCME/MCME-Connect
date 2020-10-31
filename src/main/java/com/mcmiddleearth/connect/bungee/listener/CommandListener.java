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

import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.Handler.ConnectHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpHandler;
import com.mcmiddleearth.connect.bungee.Handler.MvtpHandler;
import com.mcmiddleearth.connect.bungee.Handler.RestartHandler;
import com.mcmiddleearth.connect.bungee.Handler.RestorestatsHandler;
import com.mcmiddleearth.connect.bungee.Handler.ThemeHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpaHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpahereHandler;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.bungee.warp.WarpHandler;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import sun.tools.jconsole.Tab;

/**
 *
 * @author Eriol_Eandur
 */
public class CommandListener implements Listener {

    public CommandListener() {
    }
    
    @EventHandler
    public void onChat(ChatEvent event) {
        if(event.isCommand() && event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String[] message = replaceAlias(event.getMessage()).split(" ");
            if(message[0].equalsIgnoreCase("/tp") && message.length>1) {
                if(player.hasPermission(Permission.TP)) {
                    if(message.length<3) {
                        ProxiedPlayer destination = getPlayer(message[1]);
                        if(destination != null 
                                && !destination.getServer().getInfo().getName()
                                    .equals(player.getServer().getInfo().getName())) {
                            if(player.hasPermission(Permission.WORLD+"."+destination.getServer()
                                                                           .getInfo().getName())
                                    && isMvtpAllowed(player)) {
                                if(!TpHandler.handle(player.getName(), 
                                         destination.getServer().getInfo().getName(),
                                         destination.getName())) {
                                    sendError(player);
                                }
                            } else {
                                player.sendMessage(new ComponentBuilder("You don't have permission to enter "
                                                                          +destination.getName()+"'s world.")
                                                        .color(ChatColor.RED).create());
                            }
                            event.setCancelled(true);
                        }
                    } else {
                        if(player.hasPermission(Permission.TP_OTHER)) {
                            ProxiedPlayer source = getPlayer(message[1]);
                            ProxiedPlayer destination = getPlayer(message[2]);
                            if(source !=null && destination != null 
                                    && !source.getServer().getInfo().getName()
                                        .equals(destination.getServer().getInfo().getName())) {
                                if(source.hasPermission(Permission.WORLD+"."+destination.getServer()
                                                                               .getInfo().getName())
                                        && isMvtpAllowed(source)) {
                                    if(!TpHandler.handle(source.getName(), 
                                             destination.getServer().getInfo().getName(),
                                             destination.getName())) {
                                        sendError(player);
                                    }
                                } else {
                                    player.sendMessage(new ComponentBuilder(source.getName()+" is not allowed to enter "
                                                                              +destination.getName()+"'s world.")
                                                            .color(ChatColor.RED).create());
                                }
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            } else if(message[0].equalsIgnoreCase("/tpa") && message.length>1) {
                if(player.hasPermission(Permission.TPA)) {
                    ProxiedPlayer destination = getPlayer(message[1]);
                    if(destination != null 
                            && !destination.getServer().getInfo().getName()
                                .equals(player.getServer().getInfo().getName())) {
                        if(player.hasPermission(Permission.WORLD+"."+destination.getServer()
                                                                       .getInfo().getName())
                                && isMvtpAllowed(player)) {
                            TpaHandler.sendRequest(player,destination);
                        } else {
                            player.sendMessage(new ComponentBuilder("You don't have permission to enter "
                                                                      +destination.getName()+"'s world.")
                                                    .color(ChatColor.RED).create());
                        }
                        event.setCancelled(true);
                    }
                }
            } else if(message[0].equalsIgnoreCase("/tpahere") && message.length>1) {
                if(player.hasPermission(Permission.TPA)) {
                    ProxiedPlayer destination = getPlayer(message[1]);
                    if(destination != null 
                            && !destination.getServer().getInfo().getName()
                                .equals(player.getServer().getInfo().getName())) {
                        if(destination.hasPermission(Permission.WORLD+"."+player.getServer()
                                                                       .getInfo().getName())
                                && isMvtpAllowed(player)) {
                            TpahereHandler.sendRequest(player,destination);
                        } else {
                            player.sendMessage(new ComponentBuilder("You don't have permission to enter "
                                                                      +destination.getName()+"'s world.")
                                                    .color(ChatColor.RED).create());
                        }
                        event.setCancelled(true);
                    }
                }
            } else if(message[0].equalsIgnoreCase("/tpacancel")) {
                if(TpaHandler.cancel(player) || TpahereHandler.cancel(player)) {
                    event.setCancelled(true);
                }
            } else if((message[0].equalsIgnoreCase("/tpaccept") || message[0].equalsIgnoreCase("/tpyes"))) {
                if(TpaHandler.accept(player) || TpahereHandler.accept(player)) {
                    event.setCancelled(true);
                }
            } else if((message[0].equalsIgnoreCase("/tpdeny") || message[0].equalsIgnoreCase("/tpno"))) {
                if(TpaHandler.deny(player) || TpahereHandler.deny(player)) {
                    event.setCancelled(true);
                }
            } else if(message[0].equalsIgnoreCase("/tphere") && message.length>1) {
                if(player.hasPermission(Permission.TPHERE)) {
                    ProxiedPlayer target = getPlayer(message[1]);
                    if(target != null
                            && !target.getServer().getInfo().getName()
                                    .equals(player.getServer().getInfo().getName())) {
                        if(target.hasPermission(Permission.WORLD+"."
                                                   +player.getServer().getInfo().getName())
                                && isMvtpAllowed(target)) {
                            if(!TpHandler.handle(target.getName(), 
                                                player.getServer().getInfo().getName(),
                                                player.getName())) {
                                sendError(player);
                            }
                        } else {
                            player.sendMessage(new ComponentBuilder(target.getName()
                                                    +" has no permission to enter your world.")
                                                    .color(ChatColor.RED).create());
                        }
                        event.setCancelled(true);
                    }
                }
            } else if((message[0].equalsIgnoreCase("/theme"))) {
                String themedWorld = ConnectBungeePlugin.getConfig().getString("themedbuildWorld", "themedbuilds");
                if(!player.getServer().getInfo().getName()
                        .equals(themedWorld)) {
                    if(!isMvtpAllowed(player)) {
                        player.sendMessage(new ComponentBuilder(
                                                "/theme isn't allowed here.")
                                                .color(ChatColor.RED).create());
                    } else {
                        if(player.hasPermission(Permission.WORLD+"."+themedWorld)) {
                            if(!ThemeHandler.handle(player,themedWorld, event.getMessage())) {
                                sendError(player);
                            }
                        } else {
                            player.sendMessage(new ComponentBuilder("You don't have permission to enter world '"
                                                                     +themedWorld+"'.")
                                                    .color(ChatColor.RED).create());
                        }
                    }
                    event.setCancelled(true);
                }
            } else if (message[0].equalsIgnoreCase("/survival")) {
                String survivalserver = "survivalserver";
                if (!player.getServer().getInfo().getName().equals(survivalserver) && ProxyServer.getInstance().getServerInfo(survivalserver) != null) {
                    if (!this.isMvtpAllowed(player)) {
                        player.sendMessage(new ComponentBuilder("/survival isn't allowed here.").color(ChatColor.RED).create());
                    } else if (player.hasPermission(Permission.SURVIVAL)) {
                        if (!ConnectHandler.handle(player.getName(), survivalserver, true, ((Boolean success, Throwable error) -> {}))) {
                            this.sendError(player);
                        }
                    } else {
                        player.sendMessage(new ComponentBuilder("You don't have permission to enter survival server.").color(ChatColor.RED).create());
                    }
                    event.setCancelled(true);
                }
            } else if((message[0].equalsIgnoreCase("/mvtp")
                       || message[0].equalsIgnoreCase("/switch"))
                    && message.length>1
                    && ProxyServer.getInstance().getServerInfo(message[1])!=null) {
                String target = message[1];
                if(!player.getServer().getInfo().getName().equals(target)) {
                    if(!isMvtpAllowed(player)) {
                        player.sendMessage(new ComponentBuilder(
                                                "/mvtp and /switch isn't allowed here.")
                                                .color(ChatColor.RED).create());
                    } else {
                        if(player.hasPermission(Permission.WORLD+"."+target)) {
                            if(message[0].equalsIgnoreCase("/mvtp")) {
                                if(!MvtpHandler.handle(player.getName(),target)) {
                                    sendError(player);
                                }
                            } else {
                                if(!ConnectHandler.handle(player.getName(), target, true, (Boolean success, Throwable error) -> {})) {
                                    sendError(player);
                                }
                            }
                        } else {
                            player.sendMessage(new ComponentBuilder("You don't have permission to enter world '"
                                                                     +target+"'.")
                                                    .color(ChatColor.RED).create());
                        }
                    }
                    event.setCancelled(true);
                }
            } else if(WarpHandler.isWarpCommand(message)) {
                if(!isMvtpAllowed(player)) {
                    player.sendMessage(new ComponentBuilder(
                                            "/warp isn't allowed here.")
                                            .color(ChatColor.RED).create());
                    event.setCancelled(true);
                } else {
                    event.setCancelled(WarpHandler.handle(player, message));
                }
            } else if(message[0].equalsIgnoreCase("/reboot")) {
                if(!player.hasPermission(Permission.RESTART)) {
                    player.sendMessage(new ComponentBuilder(
                                            "You are not allowed to use that command.")
                                            .color(ChatColor.RED).create());
                    event.setCancelled(true);
                }
                if(message.length>1 && !message[1].equalsIgnoreCase("reloadconfig")
                                    && !message[1].equalsIgnoreCase("cancel")) {
                    RestartHandler.handle(player, Arrays.copyOfRange(message, 1, message.length));
                    event.setCancelled(true);
                }
            } else if(message[0].equalsIgnoreCase("/stop") && message.length>1) {
                if(!player.hasPermission(Permission.RESTART)) {
                    player.sendMessage(new ComponentBuilder(
                                            "You are not allowed to use that command.")
                                            .color(ChatColor.RED).create());
                    event.setCancelled(true);
                }
                RestartHandler.handle(player, Arrays.copyOfRange(message, 1, message.length),true);
                event.setCancelled(true);
            } else if(message[0].equalsIgnoreCase("/restorestats")) {
                RestorestatsHandler.handle(player,message);
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isMvtpAllowed(ProxiedPlayer player) {
        boolean result = player.hasPermission(Permission.IGNORE_DISABLED_MVTP)
            || !ConnectBungeePlugin.isMvtpDisabled(player.getServer().getInfo().getName());
        return result;
    }
    
    

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String[] argtemp = event.getCursor().split(" ");
        if(event.getCursor().charAt(event.getCursor().length()-1)==' ') {
            argtemp = Arrays.copyOf(argtemp, argtemp.length+1);
            argtemp[argtemp.length-1] = "";
        }
        String[] args = argtemp;
        if(args.length>0) {
            switch(args[0]) {
                case "/mvtp":
                case "/switch":
                    Collection<String> servers = ProxyServer.getInstance().getServers().keySet();
                    if(args.length>1) {
                        servers.stream().filter(server -> server.toLowerCase()
                                                       .startsWith(args[1].toLowerCase()))
                                .forEach(server -> event.getSuggestions().add(server));
                    } else {
                        event.getSuggestions().addAll(servers);
                    }
                    break;
                case "/tp":
                case "/tpa":
                case "/tpahere":
                case "/tphere":
                case "/msg":
                case "/tell":
                    if(args.length>1) {
                        suggestAllOtherPlayers(event,args[args.length-1]);
                    }
                    break;
                case "/reboot":
                        servers = ProxyServer.getInstance().getServers().keySet();
                        servers.add("reloadconfig");
                        servers.add("cancel");
                        servers.add("proxy");
                        if(args.length>1) {
                            servers.stream().filter(server -> server.toLowerCase()
                                                           .startsWith(args[1].toLowerCase()))
                                    .forEach(server -> event.getSuggestions().add(server));
                        } else {
                            event.getSuggestions().addAll(servers);
                        }
                    break;
                case "/warp":
                    if(args.length == 2 && !WarpHandler.matchesSubcommand(args[1])) {
                        event.getSuggestions().addAll(WarpHandler.getSuggestions(args[1],(ProxiedPlayer)event.getSender()));
                    }
                    break;
                case "/vote":
                    if(args.length == 2) {
                        suggestAllOtherPlayers(event,args[1]);
                    }
                    break;
                default:
                    if(!args[0].startsWith("/")) {
                        suggestAllOtherPlayers(event,args[args.length-1]);
                    }
            }
        } 
    }
    
    private void suggestAllOtherPlayers(TabCompleteEvent event, String start) {
        ProxiedPlayer thisPlayer = event.getSender() instanceof ProxiedPlayer?(ProxiedPlayer) event.getSender():null;
        Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
        players.stream().filter(player -> ((thisPlayer==null || !player.getName().equalsIgnoreCase(thisPlayer.getName())) 
                                       && player.getName().toLowerCase().startsWith(start.toLowerCase())
                                       && !VanishHandler.isVanished(player)))
                        .forEach(player -> event.getSuggestions().add(player.getName()));
    }
    
    private void sendError(ProxiedPlayer player) {
        player.sendMessage(new ComponentBuilder("There was an error!")
                            .color(ChatColor.RED).create());    
    }
    
    private ProxiedPlayer getPlayer(String name) {
        return ProxyServer.getInstance().getPlayers().stream()
                .filter(player -> player.getName().toLowerCase().startsWith(name.toLowerCase()))
                .findFirst().orElse(null);
    }

    private String replaceAlias(String message) {
        message = message.replace("/mv tp", "/mvtp");
        for(String server: ProxyServer.getInstance().getServers().keySet()) {
            message = message.replace("/"+server, "/switch "+server);
        }
        return message;
    }
    
}
