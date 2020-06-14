package com.mcmiddleearth.connect.tabList;

import java.util.Objects;
import java.util.UUID;

public class ConnectedPlayer {

    private final UUID uuid;

    private final String name;

    private String displayName;

    public ConnectedPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ConnectedPlayer
                && ((ConnectedPlayer)other).getUuid().equals(uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
