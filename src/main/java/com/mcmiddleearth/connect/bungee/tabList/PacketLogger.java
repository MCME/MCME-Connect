package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.tabList.PlayerList;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class PacketLogger {

    public static void sendItem(ProxiedPlayer player, PlayerListItemUpdate packet) {
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
Logger.getGlobal().info("Send to: "+player.getName()+" Items: "+packet.getItems().length);
for(PlayerListItem.Item item: packet.getItems()) {
    Logger.getGlobal().info("Item: "+item.getUsername()+", "+item.getUuid()+", "+item.getDisplayName()+", "+item.getGamemode()+", "+item.getListed());
}
//Logger.getGlobal().info(" Name: "+ Arrays.stream(packet.getItems()).findAny().get().getUsername()+" Action: "+packet.getActions().toString());
        player.unsafe().sendPacket(packet);
    }

    public static void sendItem(ProxiedPlayer player, PlayerListItemRemove packet) {
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
Logger.getGlobal().info("Send to: "+player.getName()+" Name: "+ Arrays.stream(packet.getUuids()).findAny().get().toString());
        player.unsafe().sendPacket(packet);
    }

    public static void sendHeaderFooter(ProxiedPlayer player, PlayerListHeaderFooter packet) {

        player.unsafe().sendPacket(packet);
    }


    public static void receivePacked(PlayerListItemUpdate packet) {

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
            }*/
        }
    /*    List<PvpPlayer> playerList = new ArrayList<>();
        playerList.add(new PvpPlayer());
        PvpTeam team1 = new PvpTeam();
        PvpTeam team2 = new PvpTeam();
        playerList.stream().sorted((player1,player2) -> (Integer.compare(player1.getPvpSkill(), player2.getPvpSkill())))
                  .forEachOrdered(player -> {
            if(team1.getTotalSkill()<team2.getTotalSkill()) {
                team1.add(player);
            } else {
                team2.add(player);
            }
        });

    }
    static class PvpPlayer {
        public int getPvpSkill(){return 0;}
    }

    static class PvpTeam {
        public int getTotalSkill(){return 0;}
        public void add(PvpPlayer player){}
    }*/

}
