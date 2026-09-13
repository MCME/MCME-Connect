package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * A MiniMessage template with a single {@code <text>} placeholder. The template is admin-authored
 * config and may contain markup; the substituted content is staff-authored and is always inserted
 * literally, so a moderator cannot inject styling into the network-wide tab list.
 */
public class RowTemplate {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final String template;

    public RowTemplate(String template) {
        this.template = template;
    }

    public Component render(String text) {
        return MINI_MESSAGE.deserialize(template, Placeholder.unparsed("text", text));
    }
}
