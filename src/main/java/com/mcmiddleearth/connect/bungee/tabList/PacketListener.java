package com.mcmiddleearth.connect.bungee.tabList;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.PacketWrapper;
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
//Logger.getGlobal().info("decode");
        boolean shouldRelease = true;
        try {
            if (!connection.isObsolete()) {
                if (packetWrapper.packet != null) {
                    if (packetWrapper.packet instanceof PlayerListItem) {
                        PlayerListItem playerListPacket = (PlayerListItem)packetWrapper.packet;
                        switch(playerListPacket.getAction()) {
                            case ADD_PLAYER:
                                TabViewManager.handleAddPlayerPacket(player,playerListPacket);
                                break;
                            case UPDATE_GAMEMODE:
                                TabViewManager.handleUpdateGamemodePacket(player,playerListPacket);
                                break;
                            case UPDATE_LATENCY:
                                TabViewManager.handleUpdateLatencyPacket(player,playerListPacket);
                                break;
                            case UPDATE_DISPLAY_NAME:
                                TabViewManager.handleUpdateDisplayNamePacket(player,playerListPacket);
                                break;
                            case REMOVE_PLAYER:
                                TabViewManager.handleRemovePlayerPacket(player,playerListPacket);
                                break;
                        }
                        //boolean result = handler.onPlayerListPacket((PlayerListItem) packetWrapper.packet, player);
//Logger.getGlobal().info("handle");
//                        if (result) {
//Logger.getGlobal().info("send");
//                            player.unsafe().sendPacket(packetWrapper.packet);
//                        }
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
}
