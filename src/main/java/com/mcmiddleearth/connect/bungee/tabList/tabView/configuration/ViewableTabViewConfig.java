package com.mcmiddleearth.connect.bungee.tabList.tabView.configuration;

import com.mcmiddleearth.connect.bungee.YamlConfiguration;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class ViewableTabViewConfig {

    private File file;
    private String key;

    private List<String> servers;
    private String header;
    private String footer;
    private int maxPlayerItemLength;
    private List<PlayerItemConfig> playerItemConfiguration;

    public ViewableTabViewConfig(File file, String key) {
        this.file = file;
        this.key = key;
        reload();
    }

    public void reload() {
        YamlConfiguration config = new YamlConfiguration();
        config.load(file);
        servers = config.getStringList(key+".servers");
        header = config.getString(key+".header", "§eWelcome to §6§lMCME §f{Player}");
        footer = config.getString(key+".footer", "§6Time: §e{Time} §4| §6Node: §e{Server}\n§6Ping: {Ping} §4| {TPS_1} tps");
        maxPlayerItemLength = config.getInt(key+".maxPlayerItemLength",20);
        List<Object> playerItem =  config.getList(key+".playerItem");
        playerItemConfiguration = new ArrayList<>();
        playerItem.forEach(object -> {
            YamlConfiguration itemConfig = new YamlConfiguration((Map<String,Object>) object);
            PlayerItemConfig playerItemConfig = new PlayerItemConfig();
            playerItemConfig.setAfk(itemConfig.getBoolean("afk",false));
            playerItemConfig.setVanished(itemConfig.getBoolean("vanished",false));
            playerItemConfig.setRequireAllPermissions(itemConfig.getBoolean("requireAllPermissions",false));
            playerItemConfig.setShorten(itemConfig.getBoolean("shorten",false));
            playerItemConfig.setText(itemConfig.getString("text","No text found."));
            playerItemConfig.setServers(itemConfig.getStringList("servers"));
            playerItemConfig.setPermissions(itemConfig.getStringList("permissions"));
            playerItemConfiguration.add(playerItemConfig);
        });
    }

    public String getFooter() {
        return footer;
    }

    public String getHeader() {
        return header;
    }

    public String getDisplayName(TabViewPlayerItem item) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(item.getUuid());
        if(player!=null) {
            String roleColor = getRankColor(player).replace("&", "§");
            String prefix = "";
            int prefixLength = 0;
            int suffixLength = 0;
            if (player.hasPermission("group.badge_moderator")) {
                prefix = "§6M";
                prefixLength = lengthWithoutFormatting(prefix);
            }
            String suffix = "";
            if (player.hasPermission("group.badge_minigames")
                    || player.hasPermission("group.badge_tours")
                    || player.hasPermission("group.badge_animations")
                    || player.hasPermission("group.badge_worldeditfull")
                    || player.hasPermission("group.badge_worldeditlimited")
                    || player.hasPermission("group.badge_voxel")) {
                suffix = "~";
                suffixLength = lengthWithoutFormatting(suffix);
            }
            Color rColor =  new Color(255,255,255);
            if(roleColor.length()>1) {
                rColor = ChatColor.getByChar(roleColor.charAt(1)).getColor();
            }
            boolean italic = false;
            String status = "";
            String statusColor = "#ffffff";
            boolean grayout = false;
            if (item.getAfk()) {
                //rColor = rColor.darker();
                //suffix = suffix + "§8AFK";
                status = "AFK";
                statusColor = "#777777";
                grayout = true;
            }
            //chatColor = net.md_5.bungee.api.ChatColor.of(new Color(0,120+10 * 6, 90+10*7));
            if (VanishHandler.isVanished(item.getUuid()) && roleColor.length()>1) {
                //ChatColor chatColor = ChatColor.of(new Color(rColor.getRed()-100,rColor.getGreen()-50,rColor.getBlue()-50));
                rColor = rColor.brighter().brighter();
                italic = true;
                //suffix = suffix + "§fV";
                status = status + "_V";
                statusColor = "#cccccc";
                grayout = true;
            }
            if(grayout) {
                float[] hsb = Color.RGBtoHSB(rColor.getRed(),rColor.getGreen(),rColor.getBlue(),null);
                rColor = Color.getHSBColor(hsb[0],hsb[1]*0.5f,hsb[2]*0.8f);
            }
            suffixLength = lengthWithoutFormatting(suffix+status);
            roleColor = "#"+Integer.toHexString(rColor.getRGB()).substring(2);
            //Logger.getGlobal().info(roleColor);
            //roleColor = chatColor+"ak";
            String tempPlayername = player.getName();//+"1234567890";
            String username = tempPlayername.substring(0, Math.min(tempPlayername.length(), 20 - prefixLength - suffixLength));
            //BaseComponent[] displayName = new ComponentBuilder(" ").appendLegacy(prefix).append("username"+suffix).color(chatColor).create();
            String displayName = "{\"text\":\""+prefix+"\",\"italic\":\""+italic+"\",\"extra\":[{\"text\":\""
                    +username+suffix+"\",\"color\":\""+roleColor+"\"},{\"text\":\""
                    +status+"\",\"color\":\""+statusColor+"\"}]}";
//player.sendMessage(displayName);
//Logger.getGlobal().info(displayName);
            //return "\" " + prefix + "#ffee33" + username + suffix + "\"";
            return displayName;
        }
        return "null player";
    }

    private static int lengthWithoutFormatting(String formatted) {
        int length = 0;
        int position = 0;
        while(position < formatted.length()) {
            if(formatted.charAt(position) == '§') {
                position += 2;
            } else {
                length++;
                position++;
            }
        }
        return length;
    }

    private static String getRankColor(ProxiedPlayer player) {
        if(player==null) {
            return "null Player";
        }
        if(true ) {//|| ChatPlugin.isLuckPerms()) {
            LuckPerms api = getApi();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if(user == null) {
                return "";
            }
            SortedMap<Integer, String> prefixes = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getPrefixes();
            //Optional<Entry<Integer, String>> maxPrefix = user.getNodes()
            //.filter(node -> node instanceof PrefixNode)
            //.map(node -> new SimpleEntry<>(((PrefixNode) node).getPriority(),((PrefixNode) node).getMetaValue()))
            //.max((entry1, entry2) -> entry1.getKey() > entry2.getKey() ? 1 : -1);
            String color;
            if(!prefixes.isEmpty()) {
                color = prefixes.get(prefixes.firstKey());
                if(color.length()>1 && color.charAt(0) == '&') {
                    if(color.length()>3 && color.charAt(2) == '&') {
                        color = color.substring(0, 4);
                    } else {
                        color = color.substring(0, 2);
                    }
                } else {
                    color = "";
                }
            } else {
                color = "";
            }

//Logger.getGlobal().info("tt"+color+"test");
            return color;
        }
        return "";
    }

    private static LuckPerms getApi() {
        LuckPerms api = LuckPermsProvider.get();
        return api;
    }

}
