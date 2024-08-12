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
package com.mcmiddleearth.connect.proxy.core.warp;

import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.ChatMessageHandler;
import com.mcmiddleearth.connect.proxy.core.handler.TpposHandler;

import java.util.*;

/**
 *
 * @author Eriol_Eandur
 */
public class WarpHandler {

    private static final Set<String> commands = new HashSet<>();
    private static final Set<String> subcommands = new HashSet<>();

    private static Set<WarpData> cache = new HashSet<>();
    
    static {
        commands.addAll(Arrays.asList("/warp","/to"));
        subcommands.addAll(Arrays.asList("list","pcreate","create","delete",
                "public","private","invite","uninvite","give",
                "info","stats","limits","assets","welcome","update",
                "import","reload","player","point","help"));
    }
    
    
    /** 
     * Handles cross server warping.
     * @param player sender of the warp command
     * @param message command message
     * @return true if the warp command was or will be handled.
     */
    public static boolean handle(McmeProxyPlayer player, String[] message) {
        String warpName = message[1];
        for(int i = 2; i<message.length;i++) {
            warpName = warpName + " " + message[i];
        }
        WarpData warp = McmeConnect.getMyWarpConnector().getWarp(player, warpName);
        if(warp !=null && !warp.getWorld().equals(player.getServerInfo().getName())) {
            if(warp.getWorld().equals("_unknown")) {
                ChatMessageHandler.handle(player.getServerInfo().getName(), player.getName(),
                                          NamedTextColor.RED+"The world of that warp could not be found!", 10);
            } else if((player.hasPermission(Permission.WORLD+"."
                       +warp.getWorld().toLowerCase()))){
                TpposHandler.handle(player.getName(), warp.getServer(),
                                    warp.getWorld(), warp.getLocation(), 
                                    NamedTextColor.AQUA+warp.getWelcomeMessage()
                                           .replace("%player%", player.getName())
                                           .replace("%warp%",warp.getName()));
            } else {
                player.sendMessage(Component.text("You don't have permission to enter world '"
                                                         +warp.getWorld()+"'."));
            }
            return true;
        }
        return false;
    }
    
    public static boolean isWarpCommand(String[] message) {
        return message.length>1
            && commands.contains(message[0].toLowerCase())
            && !subcommands.contains(message[1].toLowerCase());
    }

    public static boolean matchesSubcommand(String message) {
        return subcommands.stream().anyMatch(subcommand -> subcommand.startsWith(message));
    }

    public static void updateCache() {
        cache = McmeConnect.getMyWarpConnector().getWarps();
    }

    public static List<String> getSuggestions(String search, McmeProxyPlayer player) {
        List<String> result = new ArrayList<>();
        cache.stream().filter(warp -> warp.isVisible(player)
                                   && warp.getName().startsWith(search))
                      .sorted(Comparator.comparing(WarpData::getName))
                      .forEachOrdered(warp -> result.add(warp.getName()));
        return result;
   }

}
