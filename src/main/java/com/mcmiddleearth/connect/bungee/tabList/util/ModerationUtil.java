package com.mcmiddleearth.connect.bungee.tabList.util;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ModerationUtil {

    public static String getWatchlistPrefix() {
        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugin("MCME-Moderation");
        if(plugin != null) {
            try {
                return (String) plugin.getClass().getDeclaredMethod("getTablistPrefix").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static boolean isWatched(ProxiedPlayer player) {
        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugin("MCME-Moderation");
        if(plugin != null) {
            try {
                return (boolean) plugin.getClass().getDeclaredMethod("isOnWatchlist", ProxiedPlayer.class).invoke(null, player);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return false;
   }
}
