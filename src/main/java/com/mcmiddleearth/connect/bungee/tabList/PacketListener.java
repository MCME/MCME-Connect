package com.mcmiddleearth.connect.bungee.tabList;

public class PacketListener extends MessageToMessageDecoder<PacketWrapper> {
    private final ServerConnection connection;
    private final PacketHandler handler;
    private final ProxiedPlayer player;

    public PacketListener(ServerConnection connection, PacketHandler handler, ProxiedPlayer player) {
        this.connection = connection;
        this.handler = handler;
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PacketWrapper packetWrapper, List<Object> out) {
        boolean shouldRelease = true;
        try {
            if (!connection.isObsolete()) {
                if (packetWrapper.packet != null) {
                    if (packetWrapper.packet instanceof PlayerListItem) {
                        boolean result = handler.onPlayerListPacket((PlayerListItem) packetWrapper.packet);
                        if (result) {
                            player.unsafe().sendPacket(packetWrapper.packet);
                        }
                        return;
                    }
                }
            }
            out.add(packetWrapper);
            shouldRelease = false;
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        } finally {
            if (shouldRelease) {
                packetWrapper.trySingleRelease();
            }
        }
    }
}
