package com.mcmiddleearth.connect.bungee.tabList.tabView;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.ServerInformation;
import com.mcmiddleearth.connect.bungee.tabList.PacketLogger;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleHeaderFooter implements IHeaderFooter {

    private String header, footer;

    //private int animationCounter = 0;

    public SimpleHeaderFooter(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void send(ProxiedPlayer player) {
        PlayerListHeaderFooter packet = new PlayerListHeaderFooter();
        packet.setFooter("\""+replacePlaceholder(player, TabViewManager.getFooter(footer))+"\"");
        packet.setHeader("\""+replacePlaceholder(player, TabViewManager.getHeader(header))+"\"");
        PacketLogger.sendHeaderFooter(player,packet);
    }

    private String replacePlaceholder(ProxiedPlayer player, String content) {
        ServerInformation info = ConnectBungeePlugin.getInstance()
                .getServerInformation(player.getServer().getInfo().getName());
        for(Placeholder search: Placeholder.values()) {
            String searchString = "{"+search.getPlaceholderString()+"}";
            switch(search) {
                case PING:
                    int ping = player.getPing();
                    String color = ""+(ping<300? ChatColor.GREEN: (ping < 600? ChatColor.YELLOW: ChatColor.RED));
                    content = content.replace(searchString,color+player.getPing());
                    break;
                case PLAYER:
                    content = content.replace(searchString,""+player.getDisplayName());
                    break;
                case SERVER:
                    content = content.replace(searchString,""+player.getServer().getInfo().getName());
                    break;
                case TIME:
                    LocalDateTime now = LocalDateTime.now();
                    content = content.replace(searchString,now.format(DateTimeFormatter.ofPattern("kk:mm:ss")));
                    break;
                case TPS_1:
                    double tps = info.getTps()[0];
                    color = ""+(tps>15?ChatColor.GREEN:(tps>10?ChatColor.YELLOW:ChatColor.RED));
                    content = content.replace(searchString,color+String.format("%1$.1f",tps));
                    break;
                case TPS_5:
                    tps = info.getTps()[1];
                    color = ""+(tps>15?ChatColor.GREEN:(tps>10?ChatColor.YELLOW:ChatColor.RED));
                    content = content.replace(searchString,color+String.format("%1$.1f",tps));
                    break;
                case TPS_15:
                    tps = info.getTps()[2];
                    color = ""+(tps>15?ChatColor.GREEN:(tps>10?ChatColor.YELLOW:ChatColor.RED));
                    content = content.replace(searchString,color+String.format("%1$.1f",tps));
                    break;
                case TICK:
                    tps = info.getCurrentTick();
                    color = ""+(tps>15?ChatColor.GREEN:(tps>10?ChatColor.YELLOW:ChatColor.RED));
                    content = content.replace(searchString,color+String.format("%1$.1f",tps));
                    break;
                case PAPER:
                    content = content.replace(searchString,info.getPaperVersion());
                    break;
                case BUKKIT:
                    content = content.replace(searchString,info.getBukkitVersion());
                    break;
                case MINECRAFT:
                    content = content.replace(searchString,info.getMinecraftVersion());
                    break;
                case VIEW_DISTANCE:
                    content = content.replace(searchString,""+info.getViewDistance());
                    break;
                case WORLD_TYPE:
                    content = content.replace(searchString,""+info.getWorldType());
                    break;
                case WELCOME:
                    content = content.replace(searchString,"§eWelcome to §6M§4C§bM§6C §f"+player.getDisplayName());
            }
        }
        return content;
    }

    enum Placeholder {
        PING    ("Ping"),
        PLAYER  ("Player"),
        SERVER  ("Server"),
        TIME    ("Time"),
        TICK    ("CurrentTick"),
        PAPER   ("PaperVersion"),
        BUKKIT   ("BukkitVersion"),
        MINECRAFT  ("MinecraftVersion"),
        VIEW_DISTANCE   ("ViewDistance"),
        WORLD_TYPE  ("WorldType"),
        TPS_1     ("TPS_1"),
        TPS_5     ("TPS_5"),
        TPS_15     ("TPS_15"),
        WELCOME     ("Welcome");

        String placeholderString;

        Placeholder(String placeholderString) {
            this.placeholderString = placeholderString;
        }

        public String getPlaceholderString() {
            return placeholderString;
        }
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
