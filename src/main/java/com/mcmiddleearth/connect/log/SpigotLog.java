package com.mcmiddleearth.connect.log;

import com.mcmiddleearth.connect.ConnectPlugin;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpigotLog extends Log implements CommandExecutor, Listener {

    private List<Player> developer = new ArrayList<>();

    public SpigotLog() {
        ConnectPlugin.getInstance().getCommand("log").setExecutor(this);
        ConnectPlugin.getInstance().getServer().getPluginManager().registerEvents(this, ConnectPlugin.getInstance());
        enable();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(ChatColor.RED+"You don't have permission");
        } else {
            if((commandSender instanceof Player) && args.length>1 && args[1].equalsIgnoreCase("on")) {
                developer.add((Player) commandSender);
            } else if((commandSender instanceof Player) && args.length>1 && args[1].equalsIgnoreCase("off")) {
                developer.remove((Player) commandSender);
            } else {
                handleCommand((commandSender instanceof Player ? ((Player) commandSender).getName() : "Console"), args);
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        developer.remove(event.getPlayer());
    }

    @Override
    public void sendToDeveloper(String component, LogLevel level, String message) {
        developer.forEach(dev -> dev.sendMessage(new ComponentBuilder(message).color(net.md_5.bungee.api.ChatColor.WHITE).create()));
    }
}
