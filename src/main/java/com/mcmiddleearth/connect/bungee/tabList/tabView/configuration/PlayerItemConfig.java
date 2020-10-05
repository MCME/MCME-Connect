package com.mcmiddleearth.connect.bungee.tabList.tabView.configuration;

import com.google.common.base.Joiner;
import com.mcmiddleearth.connect.bungee.YamlConfiguration;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.tabList.util.JsonTextUtil;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.log.Log;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.util.List;
import java.util.*;

public class PlayerItemConfig implements IPlayerItemConfig{

    private Map<String, JsonTextUtil.ColorAdjustment> colorAdjustments = new HashMap<>();
    private int maxPlayerItemLength;
    private List<PlayerItemPartConfig> playerItemPartConfiguration;

    public PlayerItemConfig(YamlConfiguration config) {reload(config);}

    @Override
    public void reload(YamlConfiguration config){
        Map<String, Object> colorAdjustmentConfig = config.getSection("colorAdjustment");
        if(colorAdjustmentConfig!=null) {
            colorAdjustmentConfig.forEach((condition, adjustmentData) -> {
                YamlConfiguration adjustmentConfig = new YamlConfiguration((Map<String,Object>)adjustmentData);
                JsonTextUtil.ColorAdjustment adjustment = new JsonTextUtil.ColorAdjustment(JsonTextUtil.AdjustmentMethod
                        .valueOf(adjustmentConfig.getString("method","NONE")));
                adjustment.setBrightness(adjustmentConfig.getFloat("brightness",1));
                adjustment.setSaturation(adjustmentConfig.getFloat("saturation",1));
                adjustment.setHue(adjustmentConfig.getFloat("hue",1));
                colorAdjustments.put(condition, adjustment);
            });
        }
        maxPlayerItemLength = config.getInt("maxLength",20);
        List<Object> playerItem =  config.getList("format");
        playerItemPartConfiguration = new ArrayList<>();
        playerItem.forEach(object -> {
            ((Map<String,Object>) object).values().stream().findFirst().ifPresent(map -> {
                YamlConfiguration itemConfig = new YamlConfiguration((Map<String, Object>) map);
                PlayerItemPartConfig playerItemPartConfig = new PlayerItemPartConfig();
                playerItemPartConfig.setAfk((Boolean) itemConfig.getValue("afk"));
                playerItemPartConfig.setVanished((Boolean) itemConfig.getValue("vanished"));
                playerItemPartConfig.setRequireAllPermissions(itemConfig.getBoolean("requireAllPermissions", false));
                playerItemPartConfig.setShorten(itemConfig.getBoolean("shorten", false));
                playerItemPartConfig.setText(itemConfig.getString("text", "No text found."));
                playerItemPartConfig.setServers(itemConfig.getStringList("servers"));
                playerItemPartConfig.setPermissions(itemConfig.getStringList("permissions"));
                playerItemPartConfiguration.add(playerItemPartConfig);
            });
        });
    }

    @Override
    public String getDisplayName(TabViewPlayerItem item) {
        //if(true) return invalid_getDisplayName(item);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(item.getUuid());
        if(player!=null) {
            List<String> displayNameParts = new ArrayList<>();
            List<Boolean> displayNameShortenParts = new ArrayList<>();
            playerItemPartConfiguration.stream()
                    .filter(part -> hasPermission(player, part.getPermissions(), part.isRequireAllPermissions()))
                    .filter(part ->
                            part.isAfk() == null
                                    || part.isAfk() == item.getAfk())
                    .filter(part ->
                            part.isVanished() == null
                                    || part.isVanished() == VanishHandler.isVanished(item.getUuid()))
                    .filter(part ->
                            part.getServers() == null
                                    || part.getServers().isEmpty()
                                    || part.getServers().contains(player.getServer().getInfo().getName()))
                    .forEach(part -> {
                        displayNameParts.add(replacePlacholders(part.getText(),player));
                        displayNameShortenParts.add(part.isShorten());
                    });
            int length = 0;
            for(String part: displayNameParts) {
                length += lengthWithoutFormatting(part);
            }
            for(int i = 0; i < displayNameParts.size() && length > maxPlayerItemLength; i++) {
                if(displayNameShortenParts.get(i)) {
                    int shortage = Math.min(length - maxPlayerItemLength, displayNameParts.get(i).length());
                    displayNameParts.set(i, shortenText(displayNameParts.get(i), shortage));
                    length -= shortage;
                }
            }
            JsonTextUtil.ColorAdjustment itemAdjust = colorAdjustments.get(colorAdjustments.keySet()
                    .stream().filter(condition -> matchesCondition(condition,item))
                    .findFirst().orElse(null));
            if(itemAdjust == null) itemAdjust = new JsonTextUtil.ColorAdjustment(JsonTextUtil.AdjustmentMethod.NONE);
            String result = JsonTextUtil.parseColoredText(Joiner.on("").join(displayNameParts),itemAdjust).toString();
            //Logger.getLogger("ViewableTabViewConfig").info(result);
            Log.setLogLevel("all", Log.LogLevel.OFF);
            Log.setLogLevel("getDisplayname", Log.LogLevel.INFO);
            Log.info("getDisplayname",result);
            return result;//"{\"text\":\""+player.getName()+"\"}";//result;
        }
        throw new RuntimeException("ViewableTabViewConfig: null player");
    }

    private boolean matchesCondition(String condition, TabViewPlayerItem playerItem) {
        if(condition==null) return false;
        switch(condition) {
            case "afk": return playerItem.getAfk();
            case "vanished": return VanishHandler.isVanished(playerItem.getUuid());
        }
        return false;
    }

    private boolean hasPermission(ProxiedPlayer player, List<String> permissions, boolean allRequired) {
        if(permissions == null || permissions.isEmpty()) {
            return true;
        } else {
            for(String permission: permissions) {
                if(player.hasPermission(permission)) {
                    if(!allRequired) {
                        return true;
                    }
                } else {
                    if(allRequired) {
                        return false;
                    }
                }
            }
            return allRequired;
        }

    }

    private String replacePlacholders(String text, ProxiedPlayer player) {
        String roleColor = getRankColor(player).replace("&", "§");
        return text.replace("{RoleColor}",roleColor).replace("{Player}",player.getName());
    }

    private static int lengthWithoutFormatting(String formatted) {
        int length = 0;
        int position = 0;
        while(position < formatted.length()) {
            if(formatted.charAt(position) == '§') {
                position += 2;
            } else if (formatted.charAt(position) == '#') {
                position += 7;
            } else {
                length++;
                position++;
            }
        }
        return length;
    }

    private static String shortenText(String text, int shortage) {
        int desiredLength = lengthWithoutFormatting(text)-shortage;
        int length = 0;
        int position = 0;
        while(length < desiredLength) {
            if(text.charAt(position) == '§') {
                position += 2;
            } else if (text.charAt(position) == '#') {
                position += 7;
            } else {
                length++;
                position++;
            }
        }
        return text.substring(0,position);
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

            return color;
        }
        return "";
    }

    private static LuckPerms getApi() {
        LuckPerms api = LuckPermsProvider.get();
        return api;
    }

    public String invalid_getDisplayName(TabViewPlayerItem item) {
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
            String tempPlayername = player.getName();//+"1234567890";
            String username = tempPlayername.substring(0, Math.min(tempPlayername.length(), 20 - prefixLength - suffixLength));
            String displayName = "{\"text\":\""+prefix+"\",\"italic\":\""+italic+"\",\"extra\":[{\"text\":\""
                    +username+suffix+"\",\"color\":\""+roleColor+"\"},{\"text\":\""
                    +status+"\",\"color\":\""+statusColor+"\"}]}";
            return displayName;
        }
        throw new RuntimeException("ViewableTabViewConfig: null player");
    }

}
