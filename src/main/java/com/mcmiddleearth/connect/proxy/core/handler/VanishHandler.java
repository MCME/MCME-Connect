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

import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Eriol_Eandur
 */
public class VanishHandler {
    
    private static boolean pvSupport;
    
    private static final Set<UUID> vanishedPlayers = new HashSet<>();
    
    private static final File vanishFile = new File(McmeConnect.getProxyPlugin().getDataFolder(),"vanished.uid");
    
    public static void join(McmeProxyPlayer player) {
//McmeConnect.getLogger().info("vanish:join is vanished: "+isVanished(player));
        if(player.hasPermission(Permission.JOIN_VANISHED)) {
            vanishedPlayers.add(player.getUniqueId());
            saveVanished();
        }
        if(isVanished(player)) {
            McmeConnect.getProxy().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(otherPlayer -> {
                otherPlayer.sendMessage(McmeConnect
                        .message(" joined the MCME-Network while being vanished.", MessageColor.GREEN));
            });
        } else {
            ConnectionHandler.sendJoinMessage(player, false);
        }
    }
    
    public static void quit(McmeProxyPlayer player) {
//McmeConnect.getLogger().info("vanish:quit isVanished: "+isVanished(player));
        if(isVanished(player)) {
            McmeConnect.getProxy().getPlayers().stream()
                    .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                    p.sendMessage(McmeConnect
                            .message(player.getName()+" left the MCME-Network while being vanished.", MessageColor.GREEN));
            });
        } else {
            ConnectionHandler.sendLeaveMessage(player, false);
        }
    }
    
    public static void vanish(McmeProxyPlayer player) {
//McmeConnect.getLogger().info("vanish: "+player.getUniqueId());
        vanishedPlayers.add(player.getUniqueId());
        saveVanished();
        McmeConnect.getProxy().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                p.sendMessage(McmeConnect.message(player.getName()+" vanished.", MessageColor.GREEN));
        });
        ConnectionHandler.sendLeaveMessage(player,true);
        //TabViewManager.handlePlayerVanish(player);
    }
    
    public static void unvanish(McmeProxyPlayer player) {
//McmeConnect.getLogger().info("unvanish: "+player.getUniqueId());
        vanishedPlayers.remove(player.getUniqueId());
        saveVanished();
        McmeConnect.getProxy().getPlayers().stream()
                .filter(VanishHandler::hasVanishSeePermission).forEach(p -> {
                p.sendMessage(McmeConnect.message(player.getName()+" unvanished.", MessageColor.GREEN));
        });
        ConnectionHandler.sendJoinMessage(player,true);
        //TabViewManager.handlePlayerUnvanish(player);
    }
    
    public static boolean isVanished(McmeProxyPlayer player) {
        return isVanished(player.getUniqueId());
    }

    public static boolean isVanished(UUID player) {
//McmeConnect.getLogger().info("is vanished: "+player);
        return pvSupport && vanishedPlayers.contains(player);
    }
    
    public static void saveVanished() {
        if(!vanishFile.exists()) {
            try {
                vanishFile.createNewFile();
            } catch (IOException ex) {
                McmeConnect.getProxyPlugin().getMcmeLogger().error( "IOException", ex);
            }
        }
        try(PrintWriter out = new PrintWriter(new FileWriter(vanishFile))) {
            vanishedPlayers.forEach(uuid -> out.println(uuid.toString()));
        } catch (IOException ex) {
            McmeConnect.getProxyPlugin().getMcmeLogger().error( "IOException", ex);
        }
    }
    
    public static void loadVanished() {
        vanishedPlayers.clear();
        try(Scanner scanner = new Scanner(vanishFile)) {
            while(scanner.hasNext()) {
                vanishedPlayers.add(UUID.fromString(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            McmeConnect.getProxyPlugin().getMcmeLogger().warn("No vanished player file found.");
        }
    }

    public static boolean isPvSupport() {
        return pvSupport;
    }

    public static void setPvSupport(boolean pvSupport) {
        VanishHandler.pvSupport = pvSupport;
    }

    public static boolean hasVanishSeePermission(McmeProxyPlayer player) {
        return isPvSupport() && player.hasPermission(Permission.VANISH_SEE);
    }
}
