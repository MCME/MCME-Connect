package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.tabList.PlayerList;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.UUID;
import java.util.logging.Logger;

public class PacketLogger {

    public static void sendItem(ProxiedPlayer player, PlayerListItem packet) {
        /*if(packet != null && packet.getAction()!=null && player.getName().equals("Eriol_Eandur") ) {
            UUID uuid = player.getUniqueId();
            if(packet.getAction().equals(PlayerListItem.Action.REMOVE_PLAYER) || packet.getAction().equals(PlayerListItem.Action.ADD_PLAYER)) {
                for (PlayerListItem.Item item : packet.getItems()) {
                    if(item.getUuid().equals(uuid)) {
                        Logger.getLogger("PacketLogger").info("---------------------------Eriol "+packet.getAction());
                    }
                }
            }
        }*/
        player.unsafe().sendPacket(packet);
    }

    public static void sendHeaderFooter(ProxiedPlayer player, PlayerListHeaderFooter packet) {

        player.unsafe().sendPacket(packet);
    }


    public static void receivePacked(PlayerListItem packet) {
        /*ProxiedPlayer player = ProxyServer.getInstance().getPlayer("Eriol_Eandur");
        if(packet != null && packet.getAction()!=null && player!=null) {
            UUID uuid = player.getUniqueId();
            if (packet.getAction().equals(PlayerListItem.Action.REMOVE_PLAYER) || packet.getAction().equals(PlayerListItem.Action.ADD_PLAYER)) {
                for (PlayerListItem.Item item : packet.getItems()) {
                    if (item.getUuid().equals(uuid)) {
                        Logger.getLogger("PacketLogger").info("++++++++++++++++++++++++++++++Eriol " + packet.getAction());
                    }
                }
            }
        }*/
    }
}
