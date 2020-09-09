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
package com.mcmiddleearth.connect.bungee.vanish;

import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.listener.ConnectionListener;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class VanishHandler {
    
    private static boolean pvSupport;
    
    private static Set<UUID> vanishedPlayers = new HashSet<>();
    
    private static File vanishFile = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"vanished.uid");
    
    public static void join(ProxiedPlayer player) {
        if(player.hasPermission(Permission.JOIN_VANISHED)) {
            vanishedPlayers.add(player.getUniqueId());
            saveVanished();
        }
        if(isVanished(player)) {
            ProxyServer.getInstance().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" joined the MCME-Network while being vanished.")
                                            .color(ChatColor.GREEN).create());
            });
        } else {
            ConnectionListener.sendJoinMessage(player, false);
        }
    }
    
    public static void quit(ProxiedPlayer player) {
        if(isVanished(player)) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                    p.sendMessage(new ComponentBuilder(player.getName()+" left the MCME-Network while being vanished.")
                                                .color(ChatColor.GREEN).create());
            });
        } else {
            ConnectionListener.sendLeaveMessage(player, false);
        }
    }
    
    public static void vanish(ProxiedPlayer player) {
//Logger.getGlobal().info("Vanish!");
        vanishedPlayers.add(player.getUniqueId());
        saveVanished();
        ProxyServer.getInstance().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" vanished.")
                                            .color(ChatColor.GREEN).create());
        });
        ConnectionListener.sendLeaveMessage(player,true);
        TabViewManager.handlePlayerVanish(player);
    }
    
    public static void unvanish(ProxiedPlayer player) {
        vanishedPlayers.remove(player.getUniqueId());
        saveVanished();
        ProxyServer.getInstance().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" unvanished.")
                                            .color(ChatColor.GREEN).create());
        });
        ConnectionListener.sendJoinMessage(player,true);
        TabViewManager.handlePlayerUnvanish(player);
    }
    
    public static boolean isVanished(ProxiedPlayer player) {
        return isVanished(player.getUniqueId());
    }

    public static boolean isVanished(UUID player) {
//Logger.getGlobal().info("Vanished: "+player.getName()+" "+pvSupport+" "+vanishedPlayers.contains(player.getUniqueId()));
        return pvSupport && vanishedPlayers.contains(player);
    }
    
    public static void saveVanished() {
//Logger.getGlobal().info("saveVanished");
        if(!vanishFile.exists()) {
            try {
                vanishFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(VanishHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try(PrintWriter out = new PrintWriter(new FileWriter(vanishFile))) {
            vanishedPlayers.forEach(uuid -> {
                out.println(uuid.toString());
            });
        } catch (IOException ex) {
            Logger.getLogger(VanishHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void loadVanished() {
        vanishedPlayers.clear();
        try(Scanner scanner = new Scanner(vanishFile)) {
            while(scanner.hasNext()) {
                vanishedPlayers.add(UUID.fromString(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VanishHandler.class.getName()).log(Level.WARNING, "No vanished player file found.");
        }
    }

    public static boolean isPvSupport() {
        return pvSupport;
    }

    public static void setPvSupport(boolean pvSupport) {
        VanishHandler.pvSupport = pvSupport;
    }

    public static boolean hasVanishSeePermission(ProxiedPlayer player) {
        return isPvSupport() && player.hasPermission(Permission.VANISH_SEE);
    }
}
