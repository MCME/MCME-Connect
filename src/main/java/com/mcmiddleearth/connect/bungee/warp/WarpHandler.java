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
package com.mcmiddleearth.connect.bungee.warp;

import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.Handler.ChatMessageHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpposHandler;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public class WarpHandler {

    private static final Set<String> commands = new HashSet<>();
    private static final Set<String> subcommands = new HashSet<>();
    
    static {
        commands.addAll(Arrays.asList(new String[]{"/warp","/to"}));
        subcommands.addAll(Arrays.asList(new String[]{"list","pcreate","create","delete",
                        "public","private","invite","uninvite","give",
                        "info","stats","limits","assets","welcome","update",
                        "import","reload","player","point","help"}));
    }
    
    
    /** 
     * Handles cross server warping.
     * @param player sender of the warp command
     * @param message command message
     * @return true if the warp command was or will be handled.
     */
    public static boolean handle(ProxiedPlayer player, String[] message) {
        String warpName = message[1];
        for(int i = 2; i<message.length;i++) {
            warpName = warpName + " " + message[i];
        }
        Warp warp = MyWarpDBConnector.getWarp(player, warpName);
//Logger.getGlobal().info("found warp "+warp);
        if(warp !=null && !warp.getWorld().equals(player.getServer().getInfo().getName())) {
//Logger.getGlobal().info("is Cross World! ");
            if(warp.getWorld().equals("_unknown")) {
//Logger.getGlobal().info("World unknown!");
                ChatMessageHandler.handle(player.getServer().getInfo().getName(), player.getName(), 
                                          ChatColor.RED+"The world of that warp could not be found!", 10);
            } else if((player.hasPermission(Permission.WORLD+"."
                       +warp.getWorld().toLowerCase()))){
//Logger.getGlobal().info("Warpin!!");
                TpposHandler.handle(player.getName(), warp.getServer(), 
                                    warp.getWorld(), warp.getLocation(), 
                                    ChatColor.AQUA+warp.getWelcomeMessage()
                                           .replace("%player%", player.getName())
                                           .replace("%warp%",warp.getName()));
            } else {
                player.sendMessage(new ComponentBuilder("You don't have permission to enter world '"
                                                         +warp.getWorld()+"'.")
                                        .color(ChatColor.RED).create());
            }
            return true;
        }
        return false;
    }
    
    public static boolean isWarpCommand(String[] message) {
//Logger.getGlobal().info("warpCommand? "+message[0]+message[1]);
        return message.length>1
            && commands.contains(message[0].toLowerCase())
            && !subcommands.contains(message[1].toLowerCase());
    }
}
