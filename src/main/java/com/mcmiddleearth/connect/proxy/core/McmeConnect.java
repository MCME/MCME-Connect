package com.mcmiddleearth.connect.proxy.core;

import com.mcmiddleearth.base.core.plugin.McmeBackendPlugin;
import com.mcmiddleearth.base.core.plugin.McmeProxyPlugin;
import com.mcmiddleearth.base.core.server.McmeBackend;
import com.mcmiddleearth.base.core.server.McmeProxy;

public class McmeConnect {

    private static McmeProxyPlugin proxyPlugin;
    private static McmeBackendPlugin backendPlugin;
    private static McmeConnectConfig config;

    public static McmeProxyPlugin getProxyPlugin() {
        return proxyPlugin;
    }

    public static void setProxyPlugin(McmeProxyPlugin proxyPlugin) {
        McmeConnect.proxyPlugin = proxyPlugin;
        config = new McmeConnectConfig(proxyPlugin.getDataFolder());
    }

    public static McmeBackendPlugin getBackendPlugin() {
        return backendPlugin;
    }

    public static void setBackendPlugin(McmeBackendPlugin backendPlugin) {
        McmeConnect.backendPlugin = backendPlugin;
        config = new McmeConnectConfig(backendPlugin.getDataFolder());
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

}
