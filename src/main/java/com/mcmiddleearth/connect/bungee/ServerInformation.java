package com.mcmiddleearth.connect.bungee;

import com.google.common.io.ByteArrayDataInput;

public class ServerInformation {

    private String name;
    private double[] tps;
    private double tickTime;
    private double currentTick;
    private String paperVersion;
    private String bukkitVersion;
    private String minecraftVersion;
    private int viewDistance;
    private String worldType;

    public ServerInformation(String name) {
        this.name = name;
        paperVersion = bukkitVersion = minecraftVersion = worldType = "unknown";
        tps = new double[3];
    }

    public void updateFromPluginMessage(ByteArrayDataInput in) {
        tps = new double[3];
        for(int i = 0; i < tps.length; i++) {
            tps[i] = in.readDouble();
        }
        tickTime = in.readDouble();
        currentTick = in.readInt();
        paperVersion = in.readUTF();
        bukkitVersion = in.readUTF();
        minecraftVersion = in.readUTF();
        viewDistance = in.readInt();
        worldType = in.readUTF();
    }

    public String getName() {
        return name;
    }

    public double[] getTps() {
        return tps;
    }

    public double getTickTime() {
        return tickTime;
    }

    public double getCurrentTick() {
        return currentTick;
    }

    public String getPaperVersion() {
        return paperVersion;
    }

    public String getBukkitVersion() {
        return bukkitVersion;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public String getWorldType() {
        return worldType;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

}
