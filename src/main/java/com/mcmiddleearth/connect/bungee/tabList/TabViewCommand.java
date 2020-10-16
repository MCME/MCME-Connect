package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.Permission;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.HashSet;
import java.util.Set;

public class TabViewCommand extends Command implements TabExecutor {

    public TabViewCommand() {
        super("tablist");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(commandSender.hasPermission(Permission.TABVIEW)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!commandSender.hasPermission(Permission.RELOAD)) {
                    commandSender.sendMessage(new ComponentBuilder(
                            "You are not allowed to use that command.")
                            .color(ChatColor.RED).create());
                } else {
                    TabViewManager.reloadConfig();
                    commandSender.sendMessage(new ComponentBuilder(
                            "Player tab list config reloaded.")
                            .color(ChatColor.AQUA).create());
                }
            } else {
                if(commandSender instanceof ProxiedPlayer) {
                    ProxiedPlayer player = (ProxiedPlayer) commandSender;
                    if (args.length > 0) {
                        if (TabViewManager.getAvailableTabViewIdentifiers(player).contains(args[0])) {
                            TabViewManager.setTabView(args[0], player);
                            commandSender.sendMessage(new ComponentBuilder("Player tab list switched.")
                                    .color(ChatColor.AQUA).create());
                        } else {
                            commandSender.sendMessage(new ComponentBuilder("Player tab list not found or not available at this world.")
                                    .color(ChatColor.RED).create());
                        }
                    } else {
                        StringBuilder messageBuilder = new StringBuilder();
                        TabViewManager.getAvailableTabViewIdentifiers(player).forEach(name -> messageBuilder.append("\n- ").append(name));
                        commandSender.sendMessage(new ComponentBuilder("Available player tab lists:" + messageBuilder.toString())
                                .color(ChatColor.AQUA).create());
                    }
                } else {
                    commandSender.sendMessage(new ComponentBuilder("This command needs to be executed by a player.")
                            .color(ChatColor.RED).create());
                }
            }
        } else {
            commandSender.sendMessage(new ComponentBuilder(
                    "You are not allowed to use that command.")
                    .color(ChatColor.RED).create());
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        Set<String> result = new HashSet<>();
        if(commandSender instanceof ProxiedPlayer && commandSender.hasPermission(Permission.TABVIEW)) {
            TabViewManager.getAvailableTabViewIdentifiers((ProxiedPlayer)commandSender)
                    .stream().filter(view -> args.length == 0 || view.startsWith(args[0]))
                    .filter(view -> !result.contains(view))
                    .forEach(result::add);
        }
        if(commandSender.hasPermission(Permission.RELOAD) && (args.length==0 || "reload".startsWith(args[0]))) {
            result.add("reload");
        }
        return result;
    }
}
