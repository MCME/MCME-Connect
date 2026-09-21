package com.mcmiddleearth.connect.proxy.core.warp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests - no proxy, no database.
 * <p>
 * These pin the rule that decides whether Connect claims /warp and /to. That rule has now been
 * lost twice: it was disabled on the dev/no_warp branch in November 2025, that branch was never
 * merged, and every later branch re-registered the commands. On Velocity the CommandManager
 * silently replaces an existing alias, so re-registering takes /warp away from MCME-Warps and
 * players get forwarded to their backend, where EssentialsX answers instead.
 */
class WarpHandlerTest {

    @Test
    void claimsWarpOnlyWhenTheMyWarpBridgeIsOnAndNobodyElseOwnsTheAlias() {
        assertTrue(WarpHandler.shouldRegisterWarpCommands(true, false),
                "bridge enabled and alias free - Connect should serve the legacy MyWarp bridge");
        assertFalse(WarpHandler.shouldRegisterWarpCommands(true, true),
                "alias already taken (MCME-Warps) - Connect must not silently replace it");
        assertFalse(WarpHandler.shouldRegisterWarpCommands(false, false),
                "bridge disabled - registering would expose a command with no connector behind it");
        assertFalse(WarpHandler.shouldRegisterWarpCommands(false, true),
                "bridge disabled and alias taken - both reasons to stay out of the way");
    }

    @Test
    void handleDoesNotThrowWhenNoMyWarpConnectorIsConfigured() {
        // myWarp.enabled=false means McmeConnect never builds the connector, so it stays null.
        // handle() must decline rather than NPE; the player argument is never reached.
        assertFalse(WarpHandler.handle(null, new String[]{"/warp", "somewhere"}),
                "with no MyWarp connector the bridge must decline and let the command fall through");
    }

    @Test
    void updateCacheIsANoOpWhenNoMyWarpConnectorIsConfigured() {
        assertDoesNotThrow(WarpHandler::updateCache,
                "cache refresh must tolerate a disabled MyWarp bridge");
    }

    @Test
    void recognisesWarpCommandsButNotTheirSubcommands() {
        assertTrue(WarpHandler.isWarpCommand(new String[]{"/warp", "rivendell"}));
        assertTrue(WarpHandler.isWarpCommand(new String[]{"/to", "rivendell"}));
        assertFalse(WarpHandler.isWarpCommand(new String[]{"/warp", "list"}),
                "subcommands belong to the warp plugin itself, not the cross-server bridge");
        assertFalse(WarpHandler.isWarpCommand(new String[]{"/warp"}),
                "a bare /warp has no destination to route");
    }
}
