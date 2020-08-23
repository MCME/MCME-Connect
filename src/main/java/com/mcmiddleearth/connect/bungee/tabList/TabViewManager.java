package com.mcmiddleearth.connect.bungee.tabList;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class TabViewManager implements Listener {

    //Available views for each server
    private final static Map<String,ITabView> tabViews = new HashMap<>();
    
    private final static String defaultView = "global";
    
    static {
        tabViews.put(defaultView, new GlobalTabView());
    }

    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            ServerConnection server = (ServerConnection) event.getPlayer().getServer();
            ChannelWrapper wrapper = server.getCh();
            PacketListener packetListener = new PacketListener(server, player);
            //packetHandler.onServerSwitch();
//Logger.getGlobal().info("addView 1");
            if(getTabView(player)==null) {
//Logger.getGlobal().info("addView 2");
                addToTabView(defaultView, player);
            }
//Logger.getGlobal().info("inject");
            Log.info("tab_packet","inject listener for "+player.getName());
            wrapper.getHandle().pipeline().addBefore(PipelineUtils.BOSS_HANDLER, "mcme-connect-packet-listener", packetListener);
        } catch (Exception ex) {
            Logger.getLogger(TabViewManager.class.getName()).log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ITabView tabView = getTabView(event.getPlayer());
//Logger.getGlobal().info("disconnect");
        if(tabView!=null) {
//Logger.getGlobal().info("remove player 1");
            tabView.removeViewer(event.getPlayer());
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(event.getPlayer().getUniqueId());
        packet.setItems(new PlayerListItem.Item[]{item});
        handleRemovePlayerPacket(event.getPlayer(),packet);
    }

    public static void handleUpdateAfk(ProxiedPlayer player, boolean afk) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updateAfk(player.getUniqueId(),afk);
        tabViews.forEach((identfier, tabView) -> tabView.handleUpdateDisplayName(player, items));
    }

    public static void handleAddPlayerPacket(ProxiedPlayer player, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.addPlayerItems(player, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleAddPlayer(player,items));
    }
    
    public static void handleUpdateGamemodePacket(ProxiedPlayer player, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(player, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateGamemode(player,items));
    }
    
    public synchronized static void handleUpdateLatencyPacket(ProxiedPlayer player, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(player, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateLatency(player,items));
    }
    
    public static void handleUpdateDisplayNamePacket(ProxiedPlayer player, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(player, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateDisplayName(player,items));
    }
    
    public static void handleRemovePlayerPacket(ProxiedPlayer player, PlayerListItem packet) {
        //PacketListener.printListItemPacket(packet);
        Set<TabViewPlayerItem> items = PlayerItemManager.removePlayerItems(player, packet);
        tabViews.forEach((identfier, tabView) -> tabView.handleRemovePlayer(player, items));
    }

    public static void handleHeaderFooter(ProxiedPlayer player, PlayerListHeaderFooter packet) {
        tabViews.forEach((identfier,tabView) -> tabView.handleHeaderFooter(player,packet));
    }

    private static void addToTabView(String viewName, ProxiedPlayer player) {
//Logger.getGlobal().info("addToTabView  1");
        ITabView nextView = getTabView(viewName); 
        ITabView lastView = getTabView(player);
        if(nextView!=null) {
            if(lastView != null) {
//Logger.getGlobal().info("removeTabView  1");
                lastView.removeViewer(player);
            }
//Logger.getGlobal().info("addView  2");
            nextView.addViewer(player);
        } else if(lastView==null) {
            ITabView defaultViewObject = getTabView(defaultView);
            if(defaultViewObject!=null) {
                defaultViewObject.addViewer(player);
            } else {
                getTabViews().iterator().next().addViewer(player);
            }
        }
    }
    
    private static ITabView getTabView(ProxiedPlayer player) {
        for(ITabView view : tabViews.values()) {
            if(view.isViewer(player)) {
                return view;
            }
        }
        return null;
    }
    
    public static ITabView getTabView(String identifier) {
        return tabViews.get(identifier);
    }
    
    public static Set<String> getTabViewIdentifiers() {
        return tabViews.keySet();
    }
    
    public static Collection<ITabView> getTabViews() {
        return tabViews.values();
    }

}
