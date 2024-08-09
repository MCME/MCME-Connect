package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.warp.WarpHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CommandHandler {

    public static void handle(String server, String commandSender, String command) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.COMMAND);
        out.writeUTF(commandSender);
        out.writeUTF(command);
       McmeConnect.getProxy().sendPluginMessage(McmeConnect.getProxy().getServerInfo(server),
                                                Channel.MAIN, out.toByteArray(),true);
    }

    public static boolean handleChatEvent(McmeProxyPlayer player, String chatMessage) {
        String[] message = replaceAlias(chatMessage).split(" ");
        if(message[0].equalsIgnoreCase("/tp") && message.length>1) {
            if(player.hasPermission(Permission.TP)) {
                if(message.length<3) {
                    McmeProxyPlayer destination = getPlayer(message[1]);
                    if(destination != null
                            && !destination.getServerInfo().getName()
                            .equals(player.getServerInfo().getName())) {
                        if(player.hasPermission(Permission.WORLD+"."+destination.getServerInfo().getName())
                                && isMvtpAllowed(player)) {
                            if(!TpHandler.handle(player.getName(),
                                    destination.getServerInfo().getName(),
                                    destination.getName())) {
                                sendError(player);
                            }
                        } else {
                            player.sendError(Component.text("You don't have permission to enter "
                                    +destination.getName()+"'s world."));
                        }
                        return true;
                    }
                } else {
                    if(player.hasPermission(Permission.TP_OTHER)) {
                        McmeProxyPlayer source = getPlayer(message[1]);
                        McmeProxyPlayer destination = getPlayer(message[2]);
                        if(source !=null && destination != null
                                && !source.getServerInfo().getName()
                                .equals(destination.getServerInfo().getName())) {
                            if(source.hasPermission(Permission.WORLD+"."+destination.getServerInfo().getName())
                                    && isMvtpAllowed(source)) {
                                if(!TpHandler.handle(source.getName(),
                                        destination.getServerInfo().getName(),
                                        destination.getName())) {
                                    sendError(player);
                                }
                            } else {
                                player.sendError(Component.text(source.getName()+" is not allowed to enter "
                                        +destination.getName()+"'s world."));
                            }
                            return true;
                        }
                    }
                }
            }
        } else if(message[0].equalsIgnoreCase("/tpa") && message.length>1) {
            if(player.hasPermission(Permission.TPA)) {
                McmeProxyPlayer destination = getPlayer(message[1]);
                if(destination != null
                        && !destination.getServerInfo().getName()
                        .equals(player.getServerInfo().getName())) {
                    if(player.hasPermission(Permission.WORLD+"."+destination.getServerInfo().getName())
                            && isMvtpAllowed(player)) {
                        TpaHandler.sendRequest(player,destination);
                    } else {
                        player.sendError(Component.text("You don't have permission to enter "
                                +destination.getName()+"'s world."));
                    }
                    return true;
                }
            }
        } else if(message[0].equalsIgnoreCase("/tpahere") && message.length>1) {
            if(player.hasPermission(Permission.TPA)) {
                McmeProxyPlayer destination = getPlayer(message[1]);
                if(destination != null
                        && !destination.getServerInfo().getName()
                        .equals(player.getServerInfo().getName())) {
                    if(destination.hasPermission(Permission.WORLD+"."+player.getServerInfo().getName())
                            && isMvtpAllowed(destination)) {
                        TpahereHandler.sendRequest(player,destination);
                    } else {
                        player.sendError(Component.text(destination.getName()+" doesn't have permission to enter "
                                +"your world."));
                    }
                    return true;
                }
            }
        } else if(message[0].equalsIgnoreCase("/tpacancel")) {
            return TpaHandler.cancel(player) || TpahereHandler.cancel(player);
        } else if((message[0].equalsIgnoreCase("/tpaccept") || message[0].equalsIgnoreCase("/tpyes"))) {
            //todo: also handle in server requests?
            for(McmeProxyPlayer sender: TpaHandler.getRequestSender(player)) {
                if(sender!=null) {
                    if (sender.hasPermission(Permission.WORLD + "." + player.getServerInfo().getName())
                            && isMvtpAllowed(sender)) {
                        TpaHandler.accept(player);
                    } else {
                        sender.sendMessage(Component.text(player.getName() + " accepted your request but have no permission to enter his world!")
                                        .color(NamedTextColor.RED));
                        TpaHandler.removeRequests(player);
                    }
                    return true;
                }
            }
            for(McmeProxyPlayer sender: TpahereHandler.getRequestSender(player)) {
                if(sender!=null) {
                    if (player.hasPermission(Permission.WORLD + "." + sender.getServerInfo().getName())
                            && isMvtpAllowed(player)) {
                        TpahereHandler.accept(player);
                    } else {
                        sender.sendMessage(Component.text(player.getName() + " accepted your request but he has no permission to enter your world!")
                                        .color(NamedTextColor.RED));
                        TpahereHandler.removeRequests(player);
                    }
                    return true;
                }
            }
        } else if((message[0].equalsIgnoreCase("/tpdeny") || message[0].equalsIgnoreCase("/tpno"))) {
            return TpaHandler.deny(player) || TpahereHandler.deny(player);
        } else if(message[0].equalsIgnoreCase("/tphere") && message.length>1) {
            if(player.hasPermission(Permission.TPHERE)) {
                McmeProxyPlayer target = getPlayer(message[1]);
                if(target != null
                        && !target.getServerInfo().getName()
                        .equals(player.getServerInfo().getName())) {
                    if(target.hasPermission(Permission.WORLD+"."
                            +player.getServerInfo().getName())
                            && isMvtpAllowed(target)) {
                        if(!TpHandler.handle(target.getName(),
                                player.getServerInfo().getName(),
                                player.getName())) {
                            sendError(player);
                        }
                    } else {
                        player.sendError(Component.text(target.getName()
                                +" has no permission to enter your world."));
                    }
                    return true;
                }
            }
        } else if((message[0].equalsIgnoreCase("/theme"))) {
            String themedWorld = McmeConnect.getConfig().getThemedbuildWorld();
            if(!player.getServerInfo().getName()
                    .equals(themedWorld)) {
                if(!isMvtpAllowed(player)) {
                    player.sendError(Component.text(
                            "/theme isn't allowed here."));
                } else {
                    if(player.hasPermission(Permission.WORLD+"."+themedWorld)) {
                        if(!ThemeHandler.handle(player,themedWorld, chatMessage)) {
                            sendError(player);
                        }
                    } else {
                        player.sendError(Component.text("You don't have permission to enter world '"
                                +themedWorld+"'."));
                    }
                }
                return true;
            }
        } else if (message[0].equalsIgnoreCase("/survival")) {
            String survivalserver = "survivalserver";
            if (!player.getServerInfo().getName().equals(survivalserver) && McmeConnect.getProxy().getServerInfo(survivalserver) != null) {
                if (!isMvtpAllowed(player)) {
                    player.sendError(Component.text("/survival isn't allowed here."));
                } else if (player.hasPermission(Permission.SURVIVAL)) {
                    if (!ConnectionHandler.handleConnectPlayerToServer(player.getName(), survivalserver, true, ((Boolean success, Throwable error) -> {}))) {
                        sendError(player);
                    }
                } else {
                    player.sendError(Component.text("You don't have permission to enter survival server."));
                }
                return true;
            }
        } else if((message[0].equalsIgnoreCase("/mvtp")
                || message[0].equalsIgnoreCase("/switch"))
                && message.length>1
                && McmeConnect.getProxy().getServerInfo(message[1])!=null) {
            String target = message[1];
            if(!player.getServerInfo().getName().equals(target)) {
                if(!isMvtpAllowed(player)) {
                    player.sendError(Component.text(
                            "/mvtp and /switch isn't allowed here."));
                } else {
                    if(player.hasPermission(Permission.WORLD+"."+target)) {
                        if(message[0].equalsIgnoreCase("/mvtp")) {
                            if(!MvtpHandler.handle(player.getName(),target)) {
                                sendError(player);
                            }
                        } else {
                            if(!ConnectionHandler.handleConnectPlayerToServer(player.getName(), target, true, (Boolean success, Throwable error) -> {})) {
                                sendError(player);
                            }
                        }
                    } else {
                        player.sendError(Component.text("You don't have permission to enter world '"
                                +target+"'."));
                    }
                }
                return true;
            }
        } else if(WarpHandler.isWarpCommand(message)) {
            if(!isMvtpAllowed(player)) {
                player.sendError(Component.text(
                        "/warp isn't allowed here."));
                return true;
            } else {
                return WarpHandler.handle(player, message);
            }
        } else if(message[0].equalsIgnoreCase("/reboot")) {
            if(!player.hasPermission(Permission.RESTART)) {
                player.sendError(Component.text(
                        "You are not allowed to use that command."));
                return true;
            }
            if(message.length>1 && !message[1].equalsIgnoreCase("reloadconfig")
                    && !message[1].equalsIgnoreCase("cancel")) {
                RestartHandler.handle(player, Arrays.copyOfRange(message, 1, message.length));
                return true;
            }
        } else if(message[0].equalsIgnoreCase("/stop") && message.length>1) {
            if(!player.hasPermission(Permission.RESTART)) {
                player.sendError(Component.text(
                        "You are not allowed to use that command."));
                return true;
            }
            RestartHandler.handle(player, Arrays.copyOfRange(message, 1, message.length),true);
            return true;
        } else if(message[0].equalsIgnoreCase("/restorestats")) {
            RestorestatsHandler.handle(player,message);
            return true;
        }
        return false;
    }

    public static List<String> processGetSuggestions(String cursor, McmeProxyPlayer sender) {
        List<String> suggestions = new LinkedList<>();
        String[] argtemp = cursor.split(" ");
        if(cursor.charAt(cursor.length()-1)==' ') {
            argtemp = Arrays.copyOf(argtemp, argtemp.length+1);
            argtemp[argtemp.length-1] = "";
        }
        String[] args = argtemp;
        if(args.length>0) {
            switch(args[0]) {
                case "/mvtp":
                case "/switch":
                    Collection<String> servers = McmeConnect.getProxy().getAllServerInfo().stream()
                                                    .map(McmeServerInfo::getName).toList();
                    if(args.length>1) {
                        servers.stream().filter(server -> server.toLowerCase()
                                        .startsWith(args[1].toLowerCase()))
                                .forEach(suggestions::add);
                    } else {
                        suggestions.addAll(servers);
                    }
                    break;
                case "/tp":
                case "/tpa":
                case "/tpahere":
                case "/tphere":
                case "/msg":
                case "/tell":
                    if(args.length>1) {
                        suggestions.addAll(suggestAllOtherPlayers(sender,args[args.length-1]));
                    }
                    break;
                case "/reboot":
                    servers = McmeConnect.getProxy().getAllServerInfo().stream()
                                .map(McmeServerInfo::getName).toList();
                    servers.add("reloadconfig");
                    servers.add("cancel");
                    servers.add("proxy");
                    if(args.length>1) {
                        servers.stream().filter(server -> server.toLowerCase()
                                        .startsWith(args[1].toLowerCase()))
                                .forEach(suggestions::add);
                    } else {
                        suggestions.addAll(servers);
                    }
                    break;
                case "/warp":
                    if(args.length == 2 && !WarpHandler.matchesSubcommand(args[1])) {
                        suggestions.addAll(WarpHandler.getSuggestions(args[1],sender));
                    }
                    break;
                case "/vote":
                    if(args.length == 2) {
                        suggestions.addAll(suggestAllOtherPlayers(sender,args[1]));
                    }
                    break;
                default:
                    if(!args[0].startsWith("/")) {
                        suggestions.addAll(suggestAllOtherPlayers(sender,args[args.length-1]));
                    }
            }
        }
        return suggestions;
    }

    private static boolean isMvtpAllowed(McmeProxyPlayer player) {
        return player.hasPermission(Permission.IGNORE_DISABLED_MVTP)
                || !McmeConnect.getConfig().isMvtpDisabled(player.getServerInfo().getName());
    }

    private static List<String> suggestAllOtherPlayers(McmeCommandSender sender, String start) {
        List<String> suggestions = new LinkedList<>();
        Collection<McmeProxyPlayer> players = McmeConnect.getProxyPlugin().getPlayers();
        players.stream().filter(player -> ((sender==null || !player.getName().equalsIgnoreCase(sender.getName()))
                        && player.getName().toLowerCase().startsWith(start.toLowerCase())
                        && !VanishHandler.isVanished(player)))
                .forEach(player -> suggestions.add(player.getName()));
        return suggestions;
    }

    private static void sendError(McmeProxyPlayer player) {
        player.sendError(Component.text("There was an error!"));
    }

    private static McmeProxyPlayer getPlayer(String name) {
        return McmeConnect.getProxyPlugin().getPlayer(name);
    }

    private static String replaceAlias(String message) {
        message = message.replace("/mv tp", "/mvtp");
        for(String server: McmeConnect.getProxy().getAllServerInfo().stream().map(McmeServerInfo::getName).toList()) {
            message = message.replace("/"+server, "/switch "+server);
        }
        return message;
    }


}
