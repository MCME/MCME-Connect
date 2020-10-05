package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.log.Log;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class PacketListener extends MessageToMessageDecoder<PacketWrapper> {
    private final ServerConnection connection;
    private final ProxiedPlayer player;

    public PacketListener(ServerConnection connection, ProxiedPlayer player) {
        this.connection = connection;
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PacketWrapper packetWrapper, List<Object> out) {
        String component = "tab.in";
        if (!connection.isObsolete()) {
            if (packetWrapper.packet != null) {
                if (packetWrapper.packet instanceof PlayerListItem) {
                    try {
                        PlayerListItem playerListPacket = (PlayerListItem) packetWrapper.packet;
                        switch (playerListPacket.getAction()) {
                            case ADD_PLAYER:
                                printListItemPacket(component + ".add", ((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleAddPlayerPacket(player, playerListPacket);
                                break;
                            case UPDATE_GAMEMODE:
                                printListItemPacket(component + ".gamemode", ((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateGamemodePacket(player, playerListPacket);
                                break;
                            case UPDATE_LATENCY:
                                printListItemPacket(component + ".latency", ((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateLatencyPacket(player, playerListPacket);
                                break;
                            case UPDATE_DISPLAY_NAME:
                                printListItemPacket(component + ".display", ((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateDisplayNamePacket(player, playerListPacket);
                                break;
                            case REMOVE_PLAYER:
                                printListItemPacket(component + ".remove", ((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleRemovePlayerPacket(player, playerListPacket);
                                break;
                        }
                        return;
                    } catch (Throwable th) {
                        Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "Failed to inject packet listener", th);
                    } finally {
                        packetWrapper.trySingleRelease();
                    }
                } else if(packetWrapper.packet instanceof PlayerListHeaderFooter) {
                    try {
                        TabViewManager.handleHeaderFooter(player,(PlayerListHeaderFooter)packetWrapper.packet);
                        return;
                    } catch (Throwable th) {
                        Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "Failed to inject packet listener", th);
                    } finally {
                        packetWrapper.trySingleRelease();
                    }
                }
            }
        }
        out.add(packetWrapper);
    }


    protected void invalid_decode(ChannelHandlerContext ctx, PacketWrapper packetWrapper, List<Object> out) {
        String component = "tab.in";
        boolean shouldRelease = true;
        try {
            if (!connection.isObsolete()) {
                if (packetWrapper.packet != null) {
                    if (packetWrapper.packet instanceof PlayerListItem) {
                        PlayerListItem playerListPacket = (PlayerListItem)packetWrapper.packet;
                        switch(playerListPacket.getAction()) {
                            case ADD_PLAYER:
                                printListItemPacket(component+".add",((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleAddPlayerPacket(player,playerListPacket);
                                break;
                            case UPDATE_GAMEMODE:
                                printListItemPacket(component+".gamemode",((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateGamemodePacket(player,playerListPacket);
                                break;
                            case UPDATE_LATENCY:
                                printListItemPacket(component+".latency",((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateLatencyPacket(player,playerListPacket);
                                break;
                            case UPDATE_DISPLAY_NAME:
                                printListItemPacket(component+".display",((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleUpdateDisplayNamePacket(player,playerListPacket);
                                break;
                            case REMOVE_PLAYER:
                                printListItemPacket(component+".remove",((PlayerListItem) packetWrapper.packet));
                                TabViewManager.handleRemovePlayerPacket(player,playerListPacket);
                                break;
                        }
                        return;
                    } else if(packetWrapper.packet instanceof PlayerListHeaderFooter) {
                        TabViewManager.handleHeaderFooter(player,(PlayerListHeaderFooter)packetWrapper.packet);
                        return;
                    }
                }
            }
            out.add(packetWrapper);
            shouldRelease = false;
            } catch (Throwable th) {
                Logger.getLogger(PacketListener.class.getName()).log(Level.SEVERE, "Failed to inject packet listener", th);
        } finally {
            if (shouldRelease) {
                packetWrapper.trySingleRelease();
            }
        }
    }

    public static void printListItemPacket(String component, PlayerListItem packet) {
        Log.info(component,"got packet: "+packet.getAction().name());
        Log.verbose(component,"Item ("+packet.getItems().length+"):");
        for(int i = 0; i< packet.getItems().length;i++) {
            PlayerListItem.Item item = packet.getItems()[i];
            Log.verbose(component,"-" + i + ": " + packet.getItems()[i].getUuid().toString());
            Log.verbose(component,"------ username: " + item.getUsername());
            Log.verbose(component,"------ displayn: " + item.getDisplayName());
            Log.verbose(component,"------ gamemode: " + item.getGamemode());
            Log.verbose(component,"------ pingpong: " + item.getPing());
            if (item.getProperties() != null) {
                Log.frequent(component,"Properties: " + item.getProperties().length);
                for (String[] propertie : item.getProperties()) {
                    Log.frequent(component,"------Name: " + propertie[0]);
                    Log.frequent(component,"------Value: " + propertie[1]);
                }
            }
        }
    }
}
