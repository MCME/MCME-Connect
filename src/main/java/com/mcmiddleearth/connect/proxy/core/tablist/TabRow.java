package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;

/**
 * One rendered tab list row. Immutable; no Velocity types so it stays unit-testable.
 *
 * @param displayName  text shown in the row
 * @param iconTexture  base64 skin texture for the 8x8 head icon, or null for the default
 */
public record TabRow(Component displayName, String iconTexture) {

    private static final TabRow BLANK = new TabRow(Component.empty(), null);

    public static TabRow blank() {
        return BLANK;
    }
}
