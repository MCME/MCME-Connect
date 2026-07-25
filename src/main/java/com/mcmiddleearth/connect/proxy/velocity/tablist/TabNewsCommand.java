package com.mcmiddleearth.connect.proxy.velocity.tablist;

import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.tablist.AnnouncementStore;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

/** {@code /tabnews add <text> | remove <n> | list}. Content is plain text; styling is config. */
public class TabNewsCommand implements SimpleCommand {

    private final AnnouncementStore store;
    private final TabListService service;

    public TabNewsCommand(AnnouncementStore store, TabListService service) {
        this.store = store;
        this.service = service;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.TABVIEW);
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            usage(invocation);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "add" -> add(invocation, args);
            case "remove" -> remove(invocation, args);
            case "list" -> list(invocation);
            default -> usage(invocation);
        }
    }

    private void add(Invocation invocation, String[] args) {
        if (args.length < 2) {
            reply(invocation, "Usage: /tabnews add <text>", NamedTextColor.RED);
            return;
        }
        String text = String.join(" ", List.of(args).subList(1, args.length));
        store.add(text);
        service.applyToAll();
        reply(invocation, "Announcement added.", NamedTextColor.GREEN);
    }

    private void remove(Invocation invocation, String[] args) {
        if (args.length < 2) {
            reply(invocation, "Usage: /tabnews remove <number>", NamedTextColor.RED);
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            reply(invocation, "'" + args[1] + "' is not a number.", NamedTextColor.RED);
            return;
        }
        if (store.remove(index)) {
            service.applyToAll();
            reply(invocation, "Announcement removed.", NamedTextColor.GREEN);
        } else {
            reply(invocation, "No announcement number " + index + ".", NamedTextColor.RED);
        }
    }

    private void list(Invocation invocation) {
        List<String> all = store.list();
        if (all.isEmpty()) {
            reply(invocation, "No announcements.", NamedTextColor.GRAY);
            return;
        }
        for (int i = 0; i < all.size(); i++) {
            reply(invocation, (i + 1) + ". " + all.get(i), NamedTextColor.GRAY);
        }
    }

    private void usage(Invocation invocation) {
        reply(invocation, "Usage: /tabnews add <text> | remove <number> | list", NamedTextColor.RED);
    }

    private void reply(Invocation invocation, String message, NamedTextColor color) {
        invocation.source().sendMessage(Component.text(message, color));
    }
}
