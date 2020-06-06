package com.mcmiddleearth.connect.bungee.tabList;

public class PacketHandler {

    public boolean onPlayerListPacket(PlayerListItem packet) {
        if (packet.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (PlayerListItem.Item item : packet.getItems()) {
                serverPlayerList.put(getName(item), item.getPing());
            }
        } else {
            for (PlayerListItem.Item item : packet.getItems()) {
                serverPlayerList.remove(getName(item));
            }
        }
        return true;
    }

    private static String getName(PlayerListItem.Item item) {
        if (item.getDisplayName() != null) {
            return item.getDisplayName();
        } else if (item.getUsername() != null) {
            return item.getUsername();
        } else {
            throw new AssertionError("DisplayName and Username are null");
        }
    }

}
