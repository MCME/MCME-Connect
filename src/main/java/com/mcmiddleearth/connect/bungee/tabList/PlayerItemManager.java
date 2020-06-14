/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

/**
 *
 * @author Eriol_Eandur
 */
public class PlayerItemManager {
    
    private final static Map<String,Map<UUID,TabViewPlayerItem>> playerItems =  new HashMap<>();

    public static synchronized Set<TabViewPlayerItem> updateAfk(UUID uuid, boolean afk) {
        Set<TabViewPlayerItem> result = new HashSet<>();
        TabViewPlayerItem item = getPlayerItem(uuid);
Logger.getLogger(PlayerItemManager.class.getName()).info("Afk: "+afk);
        if(item != null && item.getAfk() != afk) {
            item.setAfk(afk);
            result.add(item);
        }
        return result;
    }

    public static synchronized Set<TabViewPlayerItem> addPlayerItems(ProxiedPlayer player, PlayerListItem packet) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("AddPlayer recipient: "+player.getName());
        Map<UUID,TabViewPlayerItem> items = getPlayerItems(player.getServer().getInfo().getName());
        if(items==null) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("Create Map: "+player.getServer().getInfo().getName());
            items =  new HashMap<>();
            playerItems.put(player.getServer().getInfo().getName(), items);
        }
        Set<TabViewPlayerItem> updates = new HashSet<>();
//Logger.getLogger(PlayerItemManager.class.getName()).info("Items: "+packet.getItems().length);
        for(Item packetItem: packet.getItems()) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("uuid: "+packetItem.getUuid());
            if(packetItem.getUuid()!=null) {
                TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
//Logger.getLogger(PlayerItemManager.class.getName()).info("contains: "+items.containsKey(item.getUuid()));
                if (!items.containsKey(item.getUuid())
                        || items.get(item.getUuid()).sameData(item)) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("update");
                    items.put(item.getUuid(), item);
                    sendPlayerListUpdate(item, false);
                    updates.add(item);
                }
            }
        }
        return updates;
    }
    
    public static synchronized Set<TabViewPlayerItem> updatePlayerItems(ProxiedPlayer player, PlayerListItem packet) {
        Map<UUID,TabViewPlayerItem> storedItems = getPlayerItems(player.getServer().getInfo().getName());
        Set<TabViewPlayerItem> updates = new HashSet<>();
        if(storedItems!=null) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("Items: "+packet.getItems().length);
            for(Item packetItem: packet.getItems()) {
                TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
//Logger.getLogger(PlayerItemManager.class.getName()).info("item uuid: "+item.getUuid());
                if(storedItems.containsKey(item.getUuid())) {
                    TabViewPlayerItem storedItem = storedItems.get(item.getUuid());
                    boolean update = false;
                    switch(packet.getAction()) {
                        case UPDATE_GAMEMODE:
                            update = storedItem.getGamemode() != item.getGamemode();
                            storedItem.setGamemode(item.getGamemode());
                            break;
                        case UPDATE_LATENCY:
                            update = storedItem.getPing() != item.getPing();
//Logger.getLogger(PlayerItemManager.class.getName()).info("Latency Update: "+storedItem.getPing()+" "+item.getPing()+" "+ update+" For: "+player.getName());
                            storedItem.setPing(item.getPing());
//Logger.getLogger(PlayerItemManager.class.getName()).info("Updated ping: "+playerItems.get(player.getServer().getInfo().getName()).get(item.getUuid()).getPing());
                            break;
                        case UPDATE_DISPLAY_NAME:
                            update = storedItem.getDisplayname()==null || storedItem.getDisplayname().equals(item.getDisplayname());
                            storedItem.setDisplayname(item.getDisplayname());
                            sendPlayerListUpdate(storedItem,false);
                            break;
                    }
                    if(update) {
                        updates.add(item);
                    }
                }
            }
        }
        /*
        for(Item packetItem: packet.getItems()) {
            TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
            if(   !items.containsKey(item.getUuid())
               || !items.get(item.getUuid()).sameData(item)) {
                items.put(item.getUuid(),item);
                updates.add(item);
            }
        }*/
        return updates;
    }
    
    public static synchronized Set<TabViewPlayerItem> removePlayerItems(ProxiedPlayer player, PlayerListItem packet) {
        Map<UUID,TabViewPlayerItem> items = getPlayerItems(player.getServer().getInfo().getName());
        Set<TabViewPlayerItem> updates = new HashSet<>();
        if(items==null) {
            return updates;
        }
        for(Item packetItem: packet.getItems()) {
            TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
            if(items.containsKey(item.getUuid())) {
                items.remove(item.getUuid());
                sendPlayerListUpdate(item,true);
                updates.add(item);
            }
        }
        return updates;
    }

    private static void sendPlayerListUpdate(TabViewPlayerItem item, boolean remove) {
Logger.getGlobal().info("send Player List update: "+item.getUuid()+" "+remove);
        ProxyServer.getInstance().getServers().forEach((name, info) -> info.sendData(Channel.MAIN, item.toByteArray(remove)));
    }

    public static void sendAllPlayerList(ServerInfo info) {
Logger.getGlobal().info("send Player List update to server: "+info.getName());
        getPlayerItems().forEach(item -> info.sendData(Channel.MAIN, item.toByteArray(false)));
    }

    public static synchronized TabViewPlayerItem getPlayerItem(UUID uuid) {
        for(Map<UUID,TabViewPlayerItem> itemMap: playerItems.values()) {
            TabViewPlayerItem item = itemMap.get(uuid);
            if(item!=null) {
                return item;
            }
        }
        return null;
    }
    /*public TabViewPlayerItem getPlayerItem(ProxiedPlayer player) {
        Server server = player.getServer();
        if(server==null) {
            for(Map<ProxiedPlayer,TabViewPlayerItem> itemMap: playerItems.values()) {
                TabViewPlayerItem item = itemMap.get(player);
                if(item!=null) {
                    return item;
                }
            }
        } else {
            Map<ProxiedPlayer, TabViewPlayerItem> itemMap = playerItems.get(server);
            if(itemMap!=null) {
                return itemMap.get(player);
            }
        }
        return null;
    }*/

    private synchronized static Map<UUID,TabViewPlayerItem> getPlayerItems(String server) {
        return playerItems.get(server);
    }

    public synchronized static Set<TabViewPlayerItem> getPlayerItems() {
        Set<TabViewPlayerItem> result = new HashSet<>();
        playerItems.forEach((server,serverItemMap) -> { 
            result.addAll(serverItemMap.values());
        });
        return result;
    }
    

}
