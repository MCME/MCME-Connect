package com.mcmiddleearth.connect.proxy.core;

import com.mcmiddleearth.base.core.configuration.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class McmeConnectConfig {

    private final YamlConfiguration config;

    private int connectDelay = 200;
    private boolean legacyRedirectEnabled = true;
    private String legacyRedirectFrom = "newplayerworld";
    private String legacyRedirectTo = "world";
    private final Set<String> noMVTP = new HashSet<>();


    public McmeConnectConfig(File dataFolder) {
        this.config = new YamlConfiguration(new File(dataFolder, "config.yml"));

        legacyRedirectEnabled = config.getBoolean("legacyRedirect.enabled",true);
        legacyRedirectFrom = config.getString("legacyRedirect.from","newplayerworld");
        legacyRedirectTo = config.getString("legacyRedirect.to","world");
        noMVTP.addAll(config.getStringList("disableMVTP"));
        connectDelay = config.getInt("connectDelay",200);
    }

    public boolean isGamemodeSyncEnabled(String server) {
        return config.getBoolean("syncGamemode."+server, false);
    }

    public int getConnectDelay() {
        return connectDelay;
    }

    public boolean isLegacyRedirectEnabled() {
        return legacyRedirectEnabled;
    }

    public String getLegacyRedirectFrom() {
        return legacyRedirectFrom;
    }

    public String getLegacyRedirectTo() {
        return legacyRedirectTo;
    }

    public  boolean isMvtpDisabled(String server) {
        return noMVTP.contains(server);
    }


    public String getThemedbuildWorld() {
        return config.getString("themedbuildWorld", "themedbuilds");
    }

    public Map<String, Object> getDatabaseConfig() {
        config.getSection("database");
    }
}
