package com.mcmiddleearth.connect.log;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class BungeeLog extends Log implements Listener {

    private List<ProxiedPlayer> developer = new ArrayList<>();

    public BungeeLog() {
        ConnectBungeePlugin.getInstance().getProxy().getPluginManager().registerListener(ConnectBungeePlugin.getInstance(), this);
        enable();
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if(event.isCommand() && event.getSender() instanceof ProxiedPlayer && ((ProxiedPlayer)event.getSender()).hasPermission(getPermission())) {
            String[] split = event.getMessage().split(" ");
            if(split[0].equalsIgnoreCase("/log") && Arrays.stream(split).sequential().noneMatch(arg -> arg.equalsIgnoreCase("-spigot"))) {
                if((event.getSender() instanceof ProxiedPlayer) && split.length>1 && split[1].equalsIgnoreCase("on")) {
                    developer.add((ProxiedPlayer) event.getSender());
                } else if((event.getSender() instanceof ProxiedPlayer) && split.length>1 && split[1].equalsIgnoreCase("off")) {
                    developer.remove((ProxiedPlayer) event.getSender());
                } else {
                    handleCommand(((ProxiedPlayer) event.getSender()).getName(), Arrays.copyOfRange(split, 1, split.length));
                }
                if (Arrays.stream(split).anyMatch(arg -> arg.equalsIgnoreCase("-bungee"))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent event) {
        developer.remove(event.getPlayer());
    }

    @Override
    public void sendToDeveloper(String component, LogLevel level, String message) {
        developer.forEach(dev -> dev.sendMessage(new ComponentBuilder(component+": "+message).color(ChatColor.WHITE).create()));
    }


}
