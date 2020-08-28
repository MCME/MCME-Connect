/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import com.google.common.collect.Sets;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.log.Log;
import com.mcmiddleearth.connect.tabList.PlayerList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class GlobalTabView extends VanishSupportTabView {

    private Set<UUID> viewers = new HashSet<>();

    private SimpleHeaderFooter headerFooter = new SimpleHeaderFooter("§eWelcome to §6§lMCME §f{Player}",
                                                   "§6Time: §e{Time} §4| §6Node: §e{Server}\n§6Ping: {Ping} §4| {TPS_1} tps");

    public GlobalTabView() {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("GlobalTabView constructor");
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("HeaderFooter Update "+viewers.size());
            viewers.forEach(viewer -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(viewer);
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("send to "+viewer);
                if(player!=null) {
                    headerFooter.send(player);
                }
            });
        }, 2, 1 , TimeUnit.SECONDS);
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            viewers.forEach(viewer -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(viewer);
                if(player!=null) {
                    handleUpdateDisplayName(player,PlayerItemManager.getPlayerItems());
                }
            });
        }, 20, 2 , TimeUnit.SECONDS);

    }

    @Override
    public void handleAddPlayer(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setUsername(tabViewItem.getUsername());
            item.setDisplayName(getDisplayName(tabViewItem));
            item.setGamemode(tabViewItem.getGamemode());
            String[][] prop = tabViewItem.getProperties();
            if(prop != null) {
                item.setProperties(prop.clone());
            }
            item.setPing(tabViewItems.iterator().next().getPing());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateGamemode(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setGamemode(tabViewItem.getGamemode());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateLatency(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setPing(tabViewItem.getPing());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateDisplayName(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            ProxiedPlayer itemPlayer = ProxyServer.getInstance().getPlayer(item.getUuid());
            item.setDisplayName(getDisplayName(tabViewItem));
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleRemovePlayer(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
//Logger.getGlobal().info("7 handleREmove!!");
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleVanishPlayer(TabViewPlayerItem tabViewItem) {
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[1];
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(tabViewItem.getUuid());
        item.setDisplayName(getDisplayName(tabViewItem));
        items[0] = item;
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        sendVanishFakeToViewers(viewers, packet);
    }

    @Override
    public void handleUnvanishPlayer(TabViewPlayerItem tabViewItem) {
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[1];
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(tabViewItem.getUuid());
        item.setUsername(tabViewItem.getUsername());
        item.setDisplayName(getDisplayName(tabViewItem));
        item.setGamemode(tabViewItem.getGamemode());
        String[][] prop = tabViewItem.getProperties();
        if(prop != null) {
            item.setProperties(prop.clone());
        }
        item.setPing(tabViewItem.getPing());
        items[0] = item;
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        sendVanishFakeToViewers(viewers, packet);
    }

    @Override
    public void handleHeaderFooter(ProxiedPlayer vanillaRecipient, PlayerListHeaderFooter packet) {
        //do nothing!
    }

    @Override
    public synchronized void addViewer(ProxiedPlayer player) {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("AddPlayer: "+player.getName()+" **********************************");
        if(player.getUniqueId()!=null) {
            viewers.add(player.getUniqueId());
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItem packet = new PlayerListItem();
                PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("items: "+items.length);
                Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
                for(int i = 0; i<tabViewItems.size();i++) {
                    TabViewPlayerItem tabViewItem = iterator.next();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(tabViewItem.getUuid());
                    item.setUsername(tabViewItem.getUsername());
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("PlayerItem: "+tabViewItem.getUsername());
                    item.setDisplayName(tabViewItem.getDisplayname());
                    item.setGamemode(tabViewItem.getGamemode());
                    String[][] prop = tabViewItem.getProperties();
                    if(prop != null) {
                        item.setProperties(prop.clone());
                    }
                    item.setPing(tabViewItems.iterator().next().getPing());
                    items[i] = item;
                }
                packet.setItems(items);
                packet.setAction(PlayerListItem.Action.ADD_PLAYER);

                sendToViewers(Sets.newHashSet(player.getUniqueId()), packet);
            }
        }
    }

    @Override
    public synchronized void removeViewer(ProxiedPlayer player) {
        boolean removed = viewers.remove(player.getUniqueId());
//viewers.forEach(viewer -> Logger.getLogger(GlobalTabView.class.getSimpleName()).info("stored: "+viewer.toString()));
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("remove viewer 2: "+player.getUniqueId().toString()+" "+removed);
        if(player.getUniqueId()!=null) {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("remove viewer 3");
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItem packet = new PlayerListItem();
                PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("items: "+items.length);
                Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
                for(int i = 0; i<tabViewItems.size();i++) {
                    TabViewPlayerItem tabViewItem = iterator.next();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(tabViewItem.getUuid());
                    items[i] = item;
                }
                packet.setItems(items);
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);

                sendToViewers(Sets.newHashSet(player.getUniqueId()), packet);
            }
        }
    }

    @Override
    public synchronized boolean isViewer(ProxiedPlayer player) {
        return viewers.contains(player.getUniqueId());
    }

    private static String getDisplayName(TabViewPlayerItem item) {
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
            if (item.getAfk()) {
                //rColor = rColor.darker();
                //suffix = suffix + "§8AFK";
                status = "AFK";
                statusColor = "#777777";
                italic = true;
            }
            //chatColor = net.md_5.bungee.api.ChatColor.of(new Color(0,120+10 * 6, 90+10*7));
            if (VanishHandler.isVanished(item.getUuid()) && roleColor.length()>1) {
                //ChatColor chatColor = ChatColor.of(new Color(rColor.getRed()-100,rColor.getGreen()-50,rColor.getBlue()-50));
                rColor = rColor.brighter().brighter();
                //suffix = suffix + "§fV";
                //status = "Vanish";
                //statusColor = "#cccccc";
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
