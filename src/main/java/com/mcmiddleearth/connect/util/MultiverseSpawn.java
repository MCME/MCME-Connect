/*
 * Copyright (C) 2026 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.connect.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.logging.Logger;

/**
 * Resolves the spawn point of a world, preferring the Multiverse-managed spawn over the
 * vanilla world spawn.
 * <p>
 * Multiverse is an <em>optional</em> runtime peer of MCME-Connect: it is declared as a
 * softdepend in plugin.yml and every call into it is guarded here, so a Multiverse that is
 * missing, disabled or simply a different major version degrades to the vanilla spawn
 * rather than propagating out of a scheduler task.
 * <p>
 * Note the {@code catch (Throwable)}. Multiverse 5 renamed its packages from
 * {@code com.onarandombox.MultiverseCore} to {@code org.mvplugins.multiverse.core}, and a
 * rename like that surfaces as {@link NoClassDefFoundError} / {@link NoSuchMethodError} -
 * subclasses of {@link Error}, not {@link Exception}. A {@code catch (Exception)} guard
 * would let exactly the failure we are defending against straight through.
 *
 * @author MCME
 */
public final class MultiverseSpawn {

    private static final String MULTIVERSE_PLUGIN = "Multiverse-Core";

    /** Guards the log so an incompatible Multiverse reports once, not once per teleport. */
    private static volatile boolean incompatibilityLogged = false;

    private MultiverseSpawn() {
    }

    /**
     * Returns the spawn location to teleport to for the given world: the Multiverse spawn
     * when Multiverse is enabled and knows the world, otherwise the vanilla world spawn.
     * Never returns {@code null}, and always returns a copy that is safe to mutate.
     *
     * @param world the world whose spawn is wanted, never {@code null}
     * @return a spawn location, never {@code null}
     */
    public static Location of(World world) {
        Location multiverseSpawn = multiverseSpawnOrNull(world);
        return (multiverseSpawn != null ? multiverseSpawn : world.getSpawnLocation()).clone();
    }

    /**
     * @return the Multiverse spawn for {@code world}, or {@code null} when Multiverse is
     *         unavailable, does not manage this world, or is not API-compatible.
     */
    private static Location multiverseSpawnOrNull(World world) {
        // isPluginEnabled(), not getPlugin() != null: a plugin that failed to enable is
        // still "present" and still casts cleanly to its own API types.
        if (!Bukkit.getPluginManager().isPluginEnabled(MULTIVERSE_PLUGIN)) {
            return null;
        }
        try {
            // First touch of MultiverseWorldLookup - and therefore of any Multiverse class -
            // happens here, inside the guard and inside this try. See that class for why.
            return MultiverseWorldLookup.spawnOrNull(world);
        } catch (Throwable ex) {
            logIncompatibilityOnce(ex);
            return null;
        }
    }

    private static void logIncompatibilityOnce(Throwable ex) {
        if (incompatibilityLogged) {
            return;
        }
        incompatibilityLogged = true;
        Logger.getLogger("MCME-Connect").warning(
                "Multiverse-Core is installed but its spawn API could not be used ("
                + ex.getClass().getSimpleName() + ": " + ex.getMessage()
                + "). Falling back to vanilla world spawns. This is logged once per server start.");
    }
}
