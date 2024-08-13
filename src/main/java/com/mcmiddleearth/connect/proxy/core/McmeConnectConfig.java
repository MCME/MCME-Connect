package com.mcmiddleearth.connect.proxy.core;

import com.mcmiddleearth.base.core.configuration.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class McmeConnectConfig {

    private final YamlConfiguration config;

    private final int connectDelay;
    private final boolean legacyRedirectEnabled;
    private final boolean isPVsupported;
    private final boolean isServerWatchdogEnabled;
    private final boolean isMyWarpEnabled;
    private final String legacyRedirectFrom;
    private final String legacyRedirectTo;
    private final Set<String> noMVTP = new HashSet<>();

    public static final String FILE_NAME = "config.yml";

    public McmeConnectConfig(File dataFolder) {
        this.config = new YamlConfiguration(new File(dataFolder, FILE_NAME));

        legacyRedirectEnabled = config.getBoolean("legacyRedirect.enabled",true);
        legacyRedirectFrom = config.getString("legacyRedirect.from","newplayerworld");
        legacyRedirectTo = config.getString("legacyRedirect.to","world");
        isPVsupported = config.getBoolean("premiumVanish", false);
        isMyWarpEnabled = config.getKeys().contains("myWarp") && config.getSection("myWarp").containsKey("enabled") ?
                          (Boolean) config.getSection("myWarp").get("enabled") : false;
        isServerWatchdogEnabled = config.getBoolean("serverWatchdog", true);
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

    public boolean isPVsupported() {
        return isPVsupported;
    }

    public boolean isServerWatchdogEnabled() {
        return isServerWatchdogEnabled;
    }

    public boolean isMyWarpEnabled() {
        return isMyWarpEnabled;
    }

    public String getThemedbuildWorld() {
        return config.getString("themedbuildWorld", "themedbuilds");
    }

    public Map<String, Object> getDatabaseConfig() {
        return config.getSection("database");
    }

    public List<String> getScheduledRestarts() {
        return config.getStringList("scheduledRestarts");
    }

    public Map<String, Object> getMyWarpSection() {
        return config.getSection("myWarp");
    }

    public YamlConfiguration getRawConfig() {
        return config;
    }


}
