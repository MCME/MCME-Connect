/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.log.Log;
import com.mcmiddleearth.connect.tabList.PlayerList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class GlobalTabView implements ITabView {

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
        }, 20, 20 , TimeUnit.SECONDS);

    }

    @Override
    public void handleAddPlayer(ProxiedPlayer triggerPlayer, Set<TabViewPlayerItem> tabViewItems) {
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

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateGamemode(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
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

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateLatency(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
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

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateDisplayName(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
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

        sendToViewers(packet);
    }

    @Override
    public void handleRemovePlayer(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
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

        sendToViewers(packet);
    }

    @Override
    public void handleHeaderFooter(ProxiedPlayer player, PlayerListHeaderFooter packet) {
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

                player.unsafe().sendPacket(packet);
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

                player.unsafe().sendPacket(packet);
            }
        }
    }

    @Override
    public synchronized boolean isViewer(ProxiedPlayer player) {
        return viewers.contains(player.getUniqueId());
    }

    private synchronized void sendToViewers(PlayerListItem packet) {
        String component = "tab.out";
        switch(packet.getAction()) {
            case ADD_PLAYER:
                logPacketOut(component + ".add", packet);
                break;
            case UPDATE_DISPLAY_NAME:
                logPacketOut(component + ".display", packet);
                break;
            case REMOVE_PLAYER:
                logPacketOut(component + ".remove", packet);
                break;
            case UPDATE_GAMEMODE:
                logPacketOut(component + ".gamemode", packet);
                break;
            case UPDATE_LATENCY:
                logPacketOut(component + ".latency", packet);
                break;
        }
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Viewers: " + viewers.size());
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null) {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Send packet to: " + player.getName());
                player.unsafe().sendPacket(packet);
            }
        });
    }

    private void logPacketOut(String component, PlayerListItem packet) {
        Log.LogLevel level = Log.LogLevel.VERBOSE;
        Log.log(component, Log.LogLevel.INFO,"Sending: "+packet.getAction().name());
        for(PlayerListItem.Item item : packet.getItems()) {
            Log.log(component,level,"Items: "+packet.getItems().length);
            Log.log(component,level,"uuid: "+item.getUuid());
            Log.log(component,level,"username: "+item.getUsername());
            Log.log(component,level,"displayName: "+item.getDisplayName());
            Log.log(component,level,"ping: "+item.getPing());
            Log.log(component,level,"gamemode: "+item.getGamemode());
            level = Log.LogLevel.FREQUENT;
            if(item.getProperties()!=null) {
                Log.log(component,level,"Properties: "+item.getProperties().length);
                for (String[] propertie : item.getProperties()) {
                    Log.log(component,level,"Name: " + propertie[0]);
                    Log.log(component,level,"Value: " + propertie[1]);
                }
            }
        }
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
            if (item.getAfk()) {
                suffix = suffix + "§8AFK";
                suffixLength = lengthWithoutFormatting(suffix);
            }
            String tempPlayername = player.getName();//+"1234567890";
            String username = tempPlayername.substring(0, Math.min(tempPlayername.length(), 20 - prefixLength - suffixLength));
            return "\" " + prefix + roleColor + username + suffix + "\"";
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