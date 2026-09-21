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

import org.bukkit.Location;
import org.bukkit.World;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.external.vavr.control.Option;

/**
 * Every reference to a Multiverse type in MCME-Connect lives in this class, and nowhere else.
 * <p>
 * That isolation is deliberate rather than stylistic. The JVM verifies a method the first
 * time it is invoked, and verification may force-load the classes named in its bytecode. If
 * the {@code isPluginEnabled} guard and the Multiverse calls shared a method, a missing
 * Multiverse could raise {@link NoClassDefFoundError} on entry to that method - before the
 * guard ever executed. Keeping them in separate classes defers Multiverse class loading to
 * the first instruction that actually touches this class, which {@link MultiverseSpawn}
 * only reaches once the guard has passed and inside a {@code try}.
 *
 * @author MCME
 */
final class MultiverseWorldLookup {

    private MultiverseWorldLookup() {
    }

    /**
     * @param world the world to look up
     * @return the Multiverse-managed spawn for {@code world}, or {@code null} if Multiverse
     *         is not initialised or does not manage this world
     */
    static Location spawnOrNull(World world) {
        if (!MultiverseCoreApi.isLoaded()) {
            return null;
        }
        Option<LoadedMultiverseWorld> mvWorld = MultiverseCoreApi.get()
                                                                 .getWorldManager()
                                                                 .getLoadedWorld(world);
        return mvWorld.isDefined() ? mvWorld.get().getSpawnLocation() : null;
    }
}
