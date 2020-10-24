package com.mcmiddleearth.connect.bungee.tabList.tabView.configuration;

import java.util.List;

public class PlayerItemPartConfig {

    private String text;
    private List<String> permissions;
    private boolean shorten;
    private Boolean afk;
    private boolean requireAllPermissions;
    private List<String> servers;
    private Boolean vanished;
    private Boolean watched;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isShorten() {
        return shorten;
    }

    public void setShorten(boolean shorten) {
        this.shorten = shorten;
    }

    public Boolean isAfk() {
        return afk;
    }

    public void setAfk(Boolean afk) {
        this.afk = afk;
    }

    public boolean isRequireAllPermissions() {
        return requireAllPermissions;
    }

    public void setRequireAllPermissions(boolean requireAllPermissions) {
        this.requireAllPermissions = requireAllPermissions;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public Boolean isVanished() {
        return vanished;
    }

    public void setVanished(Boolean vanished) {
        this.vanished = vanished;
    }

    public Boolean isWatched() { return watched; }

    public void setWatched(Boolean watched) { this.watched = watched; }
}
