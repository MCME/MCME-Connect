/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList.playerItem;

import com.mcmiddleearth.connect.Channel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

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
        if(item != null && item.getAfk() != afk) {
            item.setAfk(afk);
            result.add(item);
        }
        return result;
    }

    public static synchronized Set<TabViewPlayerItem> addPlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Map<UUID,TabViewPlayerItem> items = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
        if(items==null) {
            items =  new HashMap<>();
            playerItems.put(vanillaRecipient.getServer().getInfo().getName(), items);
        }
        Set<TabViewPlayerItem> updates = new HashSet<>();
        for(Item packetItem: packet.getItems()) {
            if(packetItem.getUuid()!=null) {
                TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
                TabViewPlayerItem storedItem = items.get(item.getUuid());
                if (storedItem == null
                        || !storedItem.sameData(item)) { //edit "NOT"??
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

        public static synchronized Set<TabViewPlayerItem> addPlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItemUpdate packet) {
            Map<UUID,TabViewPlayerItem> items = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
            if(items==null) {
                items =  new HashMap<>();
                playerItems.put(vanillaRecipient.getServer().getInfo().getName(), items);
            }
            Set<TabViewPlayerItem> updates = new HashSet<>();
            for(Item packetItem: packet.getItems()) {
                if(packetItem.getUuid()!=null) {
                    TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
                    TabViewPlayerItem storedItem = items.get(item.getUuid());
                    if (storedItem == null
                            || !storedItem.sameData(item)) { //edit "NOT"??
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
            for(Item packetItem: packet.getItems()) {
                TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
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
                            storedItem.setPing(item.getPing());
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
        return updates;
    }

        public static synchronized Set<TabViewPlayerItem> updatePlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItemUpdate packet, PlayerListItemUpdate.Action action) {
            Map<UUID,TabViewPlayerItem> storedItems = getPlayerItems(vanillaRecipient.getServer().getInfo().getName());
            Set<TabViewPlayerItem> updates = new HashSet<>();
            if(storedItems!=null) {
                for(Item packetItem: packet.getItems()) {
                    TabViewPlayerItem item = new TabViewPlayerItem(packetItem);
                    if(storedItems.containsKey(item.getUuid())) {
                        TabViewPlayerItem storedItem = storedItems.get(item.getUuid());
                        boolean update = false;
                        switch(action) {
                            case UPDATE_GAMEMODE:
                                update = storedItem.getGamemode() != item.getGamemode();
                                storedItem.setGamemode(item.getGamemode());
                                break;
                            case UPDATE_LATENCY:
                                update = storedItem.getPing() != item.getPing();
                                storedItem.setPing(item.getPing());
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
            return updates;
        }

    public static synchronized Set<TabViewPlayerItem> removePlayerItems(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        if(vanillaRecipient!=null) {
            if(vanillaRecipient.getServer()==null) {
                return new HashSet<>();
            }
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

    public static void showItems() {
        playerItems.entrySet().forEach(entry-> {
            Logger.getGlobal().info(entry.getKey());
            entry.getValue().entrySet().forEach((entry2 -> {
                Logger.getGlobal().info("  -"+entry2.getKey().toString()+" "+entry2.getValue().getUsername()+" "
                                                +entry2.getValue().getDisplayname()+" "+entry2.getValue().getGamemode());
            }));
        });
    }

    public static Map<String,Map<UUID, TabViewPlayerItem>> getSnapshot() {
        Map<String,Map<UUID, TabViewPlayerItem>> result = new HashMap<>();
        playerItems.forEach((server,map) -> {
            Map<UUID, TabViewPlayerItem> items = new HashMap<>();
            map.forEach((uuid,item) -> {
                items.put(uuid, item.clone());
            });
            result.put(server,items);
        });
        return result;
    }
}
