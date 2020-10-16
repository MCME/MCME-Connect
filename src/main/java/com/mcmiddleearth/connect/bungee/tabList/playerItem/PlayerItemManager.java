/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList.playerItem;

import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class PlayerItemManager {

    //Server name, player UUID, tab view item
    private final static Map<String,Map<UUID, TabViewPlayerItem>> playerItems =  new HashMap<>();

    public static synchronized Set<TabViewPlayerItem> updateAfk(UUID uuid, boolean afk) {
        Set<TabViewPlayerItem> result = new HashSet<>();
        TabViewPlayerItem item = getPlayerItem(uuid);
        Log.info("AFK",item.getUsername()+afk);
        if(item != null && item.getAfk() != afk) {
            item.setAfk(afk);
            Log.info("AFK","add");
            result.add(item);
        }
        return result;
    }

    public static synchronized Set<TabViewPlayerItem> addPlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("AddPlayer recipient: "+vanillaRecipient.getName());
        Map<UUID,TabViewPlayerItem> items = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
        if(items==null) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("Create Map: "+vanillaRecipient.getServer().getInfo().getName());
            items =  new HashMap<>();
            playerItems.put(vanillaRecipient.getServer().getInfo().getName(), items);
        }
        Set<TabViewPlayerItem> updates = new HashSet<>();
//Logger.getLogger(PlayerItemManager.class.getName()).info("Items: "+packet.getItems().length);
        for(Item packetItem: packet.getItems()) {
//Logger.getLogger(PlayerItemManager.class.getName()).info("uuid: "+packetItem.getUuid());
            if(packetItem.getUuid()!=null) {
                TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
//Logger.getLogger(PlayerItemManager.class.getName()).info("contains: "+items.containsKey(item.getUuid()));
                TabViewPlayerItem storedItem = items.get(item.getUuid());
                if (storedItem == null
                        || !storedItem.sameData(item)) { //edit "NOT"??
//Logger.getLogger(PlayerItemManager.class.getName()).info("update");
                    if(storedItem != null) {
                        item.setAfk(storedItem.getAfk());
                    }
                    items.put(item.getUuid(), item);
                    sendPlayerListUpdate(item, false);
                    updates.add(item);
                }
            }
        }
        return updates;
    }
    
    public static synchronized Set<TabViewPlayerItem> updatePlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Map<UUID,TabViewPlayerItem> storedItems = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
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
//Logger.getLogger(PlayerItemManager.class.getName()).info("Latency Update: "+storedItem.getPing()+" "+item.getPing()+" "+ update+" For: "+vanillaRecipient.getName());
                            storedItem.setPing(item.getPing());
//Logger.getLogger(PlayerItemManager.class.getName()).info("Updated ping: "+playerItems.get(vanillaRecipient.getServer().getInfo().getName()).get(item.getUuid()).getPing());
                            break;
                        case UPDATE_DISPLAY_NAME:
                            update = storedItem.getDisplayname()==null || !storedItem.getDisplayname().equals(item.getDisplayname());
                            storedItem.setDisplayname(item.getDisplayname());
                            sendPlayerListUpdate(storedItem,false);
                            break;
                    }
                    if(update) {
                        updates.add(storedItem);
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
    
    public static synchronized Set<TabViewPlayerItem> removePlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        if(vanillaRecipient!=null) {
            Map<UUID, TabViewPlayerItem> items = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
            return remove(items,packet);
        } else {
            Set<TabViewPlayerItem> updates = new HashSet<>();
            for(Map<UUID, TabViewPlayerItem> items: playerItems.values()) {
                updates.addAll(remove(items, packet));
            }
            return updates;
        }
    }

    private static synchronized Set<TabViewPlayerItem> remove(Map<UUID,TabViewPlayerItem> items,
                                                              PlayerListItem packet) {
        Set<TabViewPlayerItem> updates = new HashSet<>();
        if (items == null) {
            return updates;
        }
        for (Item packetItem : packet.getItems()) {
            TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
            if (items.containsKey(item.getUuid())) {
                items.remove(item.getUuid());
                sendPlayerListUpdate(item, true);
                updates.add(item);
            }
        }
        return updates;
    }

    private static void sendPlayerListUpdate(TabViewPlayerItem item, boolean remove) {
        ProxyServer.getInstance().getServers().forEach((name, info) -> info.sendData(Channel.MAIN, item.toByteArray(remove)));
    }

    public static void sendAllPlayerList(ServerInfo info) {
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

    public synchronized static Map<UUID,TabViewPlayerItem> getPlayerItems(String server) {
        return playerItems.get(server);
    }

    public synchronized static Set<TabViewPlayerItem> getPlayerItems() {
        Set<TabViewPlayerItem> result = new HashSet<>();
        playerItems.forEach((server,serverItemMap) -> { 
            result.addAll(serverItemMap.values());
        });
        return result;
    }

    public static String getServer(TabViewPlayerItem item) {
        for(Map.Entry<String,Map<UUID, TabViewPlayerItem>> items: playerItems.entrySet()) {
            if(items.getValue().containsKey(item.getUuid())) {
                return items.getKey();
            }

        }
        return "";
    }

    /*public static void showItems() {
        playerItems.entrySet().forEach(entry-> {
            Logger.getGlobal().info(entry.getKey());
            entry.getValue().entrySet().forEach((entry2 -> {
                Logger.getGlobal().info("-"+entry2.getKey().toString()+" "+entry2.getValue().getUsername()+" "+entry2.getValue().getDisplayname());
            }));
        });
    }*/
}
