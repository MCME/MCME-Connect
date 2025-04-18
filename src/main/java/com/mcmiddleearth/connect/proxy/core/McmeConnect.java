package com.mcmiddleearth.connect.proxy.core;

import com.mcmiddleearth.base.core.configuration.YamlConfiguration;
import com.mcmiddleearth.base.core.logger.McmeLogger;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.plugin.McmeBackendPlugin;
import com.mcmiddleearth.base.core.plugin.McmePlugin;
import com.mcmiddleearth.base.core.plugin.McmeProxyPlugin;
import com.mcmiddleearth.base.core.server.McmeBackend;
import com.mcmiddleearth.base.core.server.McmeProxy;
import com.mcmiddleearth.base.core.taskScheduling.Task;
import com.mcmiddleearth.connect.proxy.core.handler.*;
import com.mcmiddleearth.connect.proxy.core.warp.MyWarpDBConnector;
import com.mcmiddleearth.connect.proxy.core.watchdog.ServerWatchdog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class McmeConnect {

    private static McmeProxyPlugin proxyPlugin;
    private static McmeBackendPlugin backendPlugin;
    private static McmeConnectConfig config;
    private static McmeLogger logger;

    private static final Map<String, ServerInformation> serverInformation = new HashMap<>();

    private static ServerWatchdog watcher;

    private static MyWarpDBConnector myWarpConnector;

    private static boolean myWarpEnabled;

    private static RestartScheduler restartScheduler;
    private static Task tpaCleanupScheduler;
    private static Task tpahereCleanupScheduler;

    private static YamlConfiguration playerServers;
    private static final String playerServerFile = "playerServers.yml";

    public static McmeProxyPlugin getProxyPlugin() {
        return proxyPlugin;
    }

    public static void setLogger(McmeLogger logger) {
        McmeConnect.logger = logger;
    }

    public static void enable(McmeProxyPlugin proxyPlugin) {
        McmeConnect.proxyPlugin = proxyPlugin;
        config = new McmeConnectConfig(proxyPlugin.getDataFolder());
        logger = proxyPlugin.getMcmeLogger();
        enable();
    }

    public static void enable(McmeBackendPlugin backendPlugin) {
        McmeConnect.backendPlugin = backendPlugin;
        config = new McmeConnectConfig(backendPlugin.getDataFolder());
        logger = backendPlugin.getMcmeLogger();
        enable();
    }

    private static void enable() {
        RestartHandler.init();
        File file = new File(getPlugin().getDataFolder(),playerServerFile);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        playerServers = new YamlConfiguration(new File(McmeConnect.getPlugin().getDataFolder(),playerServerFile));
        //config.getKeys()
        //        .forEach(key -> playerServers.put(UUID.fromString(key), config.getString(key,"world")));
        tpaCleanupScheduler = TpaHandler.startCleanupScheduler();
        tpahereCleanupScheduler = TpahereHandler.startCleanupScheduler();
        restartScheduler = new RestartScheduler();
        if(McmeConnect.getConfig().isServerWatchdogEnabled()) {
            watcher = new ServerWatchdog();
        }
        LegacyPlayerHandler.loadLegacyPlayers();
        VanishHandler.setPvSupport(McmeConnect.getConfig().isPVsupported());
        if(VanishHandler.isPvSupport()) {
            VanishHandler.loadVanished();
        }
        if(McmeConnect.getConfig().isMyWarpEnabled()) {
            myWarpConnector = new MyWarpDBConnector(McmeConnect.getConfig().getMyWarpSection());
        }
    }

    public static McmeBackendPlugin getBackendPlugin() {
        return backendPlugin;
    }

    public static McmePlugin getPlugin() {
        return getProxyPlugin()!=null ? getProxyPlugin() : getBackendPlugin();
    }

    public static void disable() {
        watcher.stopWatchdog();
        myWarpConnector.disconnect();
        restartScheduler.cancel();
        tpaCleanupScheduler.cancel();
        tpahereCleanupScheduler.cancel();
    }

    public static McmeProxy getProxy() {
        return proxyPlugin.getMcmeProxy();
    }

    public static McmeBackend getBackend() {
        return backendPlugin.getMcmeBackend();
    }

    public static McmeConnectConfig getConfig() {
        return config;
    }

    public static McmeLogger getLogger() {
        return logger;
    }

    public static MyWarpDBConnector getMyWarpConnector() {
        return myWarpConnector;
    }

    public static ServerInformation getServerInformation(String name) {
        ServerInformation info =  serverInformation.get(name);
        if(info==null) {
            info = new ServerInformation(name);
            serverInformation.put(name,info);
        }
        return info;
    }

    public static Message infoMessage() {
        return getPlugin().createInfoMessage();
    }

    public static  Message errorMessage() {
        return getPlugin().createErrorMessage();
    }

    public static Message infoMessage(String message) {
        return getPlugin().createInfoMessage().add(message);
    }

    public static  Message errorMessage(String message) {
        return getPlugin().createErrorMessage().add(message);
    }

    public static Message message(String message, MessageColor color) {
        return getPlugin().createMessage().add(message, color);
    }

    public static String getPlayerServer(McmeProxyPlayer player) {
        return playerServers.getString(player.getUniqueId().toString(), "newplayer");
    }

    public static void setPlayerServer(McmeProxyPlayer player, String server) {
        playerServers.set(player.getUniqueId().toString(), server);
        playerServers.save(new File(getPlugin().getDataFolder(), playerServerFile));
    }
}
