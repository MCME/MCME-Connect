package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.YamlConfiguration;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.PlayerItemManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.tabList.tabView.GlobalTabView;
import com.mcmiddleearth.connect.bungee.tabList.tabView.ITabView;
import com.mcmiddleearth.connect.bungee.tabList.tabView.ServerTabView;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.IPlayerItemConfig;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.PlayerItemConfig;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.ViewableTabViewConfig;
import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TabViewManager implements Listener {

    //Available views for each server
    private final static Map<String, ITabView> tabViews = new HashMap<>();
    private final static Map<String, IPlayerItemConfig> playerItemConfigs= new HashMap<>();
    private final static Map<String, String> headers = new HashMap<>();
    private final static Map<String, String> footers = new HashMap<>();

    private final static String viewConfigFileName = "views.yml";
    private final static String playerItemConfigFileName = "playerItems.yml";
    private final static String headerFooterConfigFileName = "headerFooter.yml";
    private final static File configFolder = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"tabList");
    private final static File viewConfigFile = new File(configFolder, viewConfigFileName);
    private final static File playerItemConfigFile = new File(configFolder, playerItemConfigFileName);
    private final static File headerFooterConfigFile = new File(configFolder, headerFooterConfigFileName);

    public static void init() {
        if(!configFolder.exists()) {
            configFolder.mkdirs();
        }
        ConnectBungeePlugin.getInstance().saveDefaultConfig(viewConfigFile,viewConfigFileName);
        ConnectBungeePlugin.getInstance().saveDefaultConfig(playerItemConfigFile,playerItemConfigFileName);
        ConnectBungeePlugin.getInstance().saveDefaultConfig(headerFooterConfigFile,headerFooterConfigFileName);
        reloadConfig();
        /*playerItemConfigs.clear();
        YamlConfiguration config = new YamlConfiguration();
        config.load(playerItemConfigFile);
        for(String key: config.getKeys()) {
            Map<String, Object> map = config.getSection(key);
            YamlConfiguration playerItemConfig = new YamlConfiguration(map);
            playerItemConfigs.put(key, new PlayerItemConfig(playerItemConfig));
        }
        config = new YamlConfiguration();
        config.load(viewConfigFile);
        for(String key: config.getKeys()) {
            Map<String, Object> map = config.getSection(key);
            YamlConfiguration tabViewConfig = new YamlConfiguration(map);
            String type = tabViewConfig.getString("type", "global");
            createTabView(type, key, tabViewConfig);
        }*/
    }

    public static void createTabView(String type, String key, YamlConfiguration tabViewConfig) {
        switch(type) {
            case "GlobalTabView":
                tabViews.put(key, new GlobalTabView(new ViewableTabViewConfig(tabViewConfig)));
                break;
            case "ServerTabView":
                tabViews.put(key, new ServerTabView(new ViewableTabViewConfig(tabViewConfig)));
                break;
        }
    }

    public static synchronized void reloadConfig() {
        Map<ProxiedPlayer,String> playerViews = getPlayerViews();
        tabViews.values().forEach(view ->
                view.getViewers().forEach(viewer -> view.removeViewer(ProxyServer.getInstance().getPlayer(viewer))));
        tabViews.clear();
        playerItemConfigs.clear();
        YamlConfiguration config = new YamlConfiguration();
        config.load(playerItemConfigFile);
        for(String key: config.getKeys()) {
            Map<String, Object> map = config.getSection(key);
            YamlConfiguration playerItemConfig = new YamlConfiguration(map);
            playerItemConfigs.put(key, new PlayerItemConfig(playerItemConfig));
        }
        headers.clear();
        footers.clear();
        config = new YamlConfiguration();
        config.load(headerFooterConfigFile);
        YamlConfiguration headerConfig = new YamlConfiguration(config.getSection("header"));
        for(String key: headerConfig.getKeys()) {
            headers.put(key, headerConfig.getString(key,"Welcome to MCME!"));
        }
        YamlConfiguration footerConfig = new YamlConfiguration(config.getSection("footer"));
        for(String key: footerConfig.getKeys()) {
            footers.put(key, footerConfig.getString(key,"Welcome to MCME!"));
        }
        config = new YamlConfiguration();
        config.load(viewConfigFile);
        for(String key: config.getKeys()) {
            Map<String, Object> map = config.getSection(key);
            YamlConfiguration tabViewConfig = new YamlConfiguration(map);
            ITabView view = tabViews.get(key);
            /*if(view!=null) {
                view.getConfig().reload(tabViewConfig);
            } else {*/
                String type = tabViewConfig.getString("type", "GlobalTabView");
                createTabView(type, key, tabViewConfig);
            //}
        }
        for(Map.Entry<ProxiedPlayer,String> entry: playerViews.entrySet()) {
            setTabView(entry.getValue(),entry.getKey());
        }
    }

    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            ServerConnection server = (ServerConnection) event.getPlayer().getServer();
            ChannelWrapper wrapper = server.getCh();
            PacketListener packetListener = new PacketListener(server, player);
            if(getTabView(player)==null || !getTabView(player).isViewerAllowed(player)) {
                setTabView(null, player);
            }
            Log.info("tab_packet","inject listener for "+player.getName());
            wrapper.getHandle().pipeline().addBefore(PipelineUtils.BOSS_HANDLER, "mcme-connect-packet-listener", packetListener);
        } catch (Exception ex) {
            Logger.getLogger(TabViewManager.class.getName()).log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ITabView tabView = getTabView(event.getPlayer());
        if(tabView!=null) {
            tabView.removeViewer(event.getPlayer());
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(event.getPlayer().getUniqueId());
        packet.setItems(new PlayerListItem.Item[]{item});
        handleRemovePlayerPacket(event.getPlayer(),packet);
    }

    public static void handlePlayerVanish(ProxiedPlayer player) {
        TabViewPlayerItem item = PlayerItemManager.getPlayerItem(player.getUniqueId());
        if(item!=null) {
            tabViews.forEach((identifier, tabView) -> tabView.handleVanishPlayer(item));
        }
    }

    public static void handlePlayerUnvanish(ProxiedPlayer player) {
        TabViewPlayerItem item = PlayerItemManager.getPlayerItem(player.getUniqueId());
        if(item!=null) {
            tabViews.forEach((identifier, tabView) -> tabView.handleUnvanishPlayer(item));
        }
    }

    public static void handleUpdateAfk(ProxiedPlayer vanillaRecipient, boolean afk) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updateAfk(vanillaRecipient.getUniqueId(),afk);
        tabViews.forEach((identfier, tabView) -> tabView.handleUpdateDisplayName(vanillaRecipient, items));
    }

    public static void handleAddPlayerPacket(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.addPlayerItems(vanillaRecipient, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleAddPlayer(vanillaRecipient,items));
    }
    
    public static void handleUpdateGamemodePacket(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(vanillaRecipient, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateGamemode(vanillaRecipient,items));
    }
    
    public synchronized static void handleUpdateLatencyPacket(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(vanillaRecipient, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateLatency(vanillaRecipient,items));
    }
    
    public static void handleUpdateDisplayNamePacket(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        Set<TabViewPlayerItem> items = PlayerItemManager.updatePlayerItems(vanillaRecipient, packet);
        tabViews.forEach((identfier,tabView) -> tabView.handleUpdateDisplayName(vanillaRecipient,items));
    }

    public static void handleRemovePlayerPacket(ProxiedPlayer vanillaRecipient, PlayerListItem packet) {
        //PacketListener.printListItemPacket(packet);
        Set<TabViewPlayerItem> items = PlayerItemManager.removePlayerItems(vanillaRecipient, packet);
        tabViews.forEach((identfier, tabView) -> tabView.handleRemovePlayer(vanillaRecipient, items));
    }

    public static void handleHeaderFooter(ProxiedPlayer player, PlayerListHeaderFooter packet) {
        tabViews.forEach((identfier,tabView) -> tabView.handleHeaderFooter(player,packet));
    }

    public static synchronized boolean setTabView(String viewName, ProxiedPlayer player) {
        ITabView nextView = getTabView(viewName);
        ITabView lastView = getTabView(player);
        if(nextView!=null && nextView.isViewerAllowed(player)) {
            switchTabView(lastView,nextView,player);
            return true;
        } else {
            List<Map.Entry<String,ITabView>> tabViewList = new ArrayList<>(tabViews.entrySet());
            tabViewList.sort(Comparator
                    .comparingInt(stringITabViewEntry -> stringITabViewEntry.getValue()
                                                         .getPriority(player.getServer().getInfo().getName())*-1));
            for(Map.Entry<String,ITabView> entry: tabViewList) {
                if (entry.getValue().isViewerAllowed(player)) {
                    switchTabView(lastView,entry.getValue(),player);
                    return true;
                }
            }
        }
        return false;
    }

    private static void switchTabView(ITabView lastView, ITabView nextView, ProxiedPlayer player) {
        if (nextView != lastView) {
            if (lastView != null) {
                lastView.removeViewer(player);
            }
            nextView.addViewer(player);
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

    public static Set<String> getAvailableTabViewIdentifiers(ProxiedPlayer player) {
        return tabViews.entrySet().stream().filter(entry -> entry.getValue().isViewerAllowed(player))
                       .map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    public static Collection<ITabView> getTabViews() {
        return tabViews.values();
    }

    public static Map<ProxiedPlayer,String> getPlayerViews() {
        Map<ProxiedPlayer,String> map = new HashMap<>();
        tabViews.forEach((identifier, tabView) -> {
            tabView.getViewers().forEach(viewer -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(viewer);
                if(player!=null) {
                    map.put(player,identifier);
                }
            });
        });
        return map;
    }

    public static IPlayerItemConfig getPlayerItemConfig(String playerItemConfig) {
        return playerItemConfigs.get(playerItemConfig);
    }

    public static String getHeader(String identifier) {
        return headers.getOrDefault(identifier, "not found");
    }

    public static String getFooter(String identifier) {
        return footers.getOrDefault(identifier,"not found");
    }


    /*public static boolean setTabView(ProxiedPlayer player, String tabView) {
        ITabView currentView = getTabView(player);
        ITabView nextView = getTabView(tabView);
        if(nextView!=null && nextView.isViewerAllowed(player)) {
            if (currentView != null) {
                currentView.removeViewer(player);
            }
            nextView.addViewer(player);
            return true;
        }
        return false;
    }*/
}
