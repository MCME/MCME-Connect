package com.mcmiddleearth.connect.statistics;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A player's statistics as read from the database, copied out of the JDBC results.
 *
 * <p>The database thread builds a snapshot and closes its results before the main thread applies
 * it. Until 3.1.1 the main thread read the results itself, a tick later. The prepared statements are
 * shared between all players and re-running one closes its previous result, so by then another
 * player's load or any save had often closed them: {@code Operation not allowed after ResultSet
 * closed}, or a {@code NullPointerException} inside the driver when the two collided mid-read.
 */
final class StatisticSnapshot {

    record MaterialValue(Material material, Statistic statistic, int value) {}

    record EntityValue(EntityType entity, Statistic statistic, int value) {}

    /**
     * Material rows as read. {@code renamedValues} and {@code renamedRows} are the values and row keys
     * stored under an older name with a {@code _BLOCK} or {@code _ITEM} suffix, to be moved to the
     * current material name.
     */
    record MaterialRows(List<MaterialValue> values, List<MaterialValue> renamedValues, List<String> renamedRows) {}

    /** Entity rows as read. {@code unknown} holds entity names this server's API does not know. */
    record EntityRows(List<EntityValue> values, List<String> unknown) {
        static final EntityRows NONE = new EntityRows(List.of(), List.of());
    }

    private final Map<Statistic, Integer> general;
    private final List<MaterialValue> materials;
    private final EntityRows entities;

    StatisticSnapshot(Map<Statistic, Integer> general, List<MaterialValue> materials, EntityRows entities) {
        this.general = general.isEmpty() ? Map.of() : new EnumMap<>(general);
        this.materials = List.copyOf(materials);
        this.entities = entities;
    }

    /** Reads the general (untyped) statistics from the current row of {@code row}. */
    static Map<Statistic, Integer> readGeneral(ResultSet row) throws SQLException {
        Map<Statistic, Integer> values = new EnumMap<>(Statistic.class);
        for (Statistic stat : Statistic.values()) {
            if (stat.getType() == Statistic.Type.UNTYPED) {
                values.put(stat, row.getInt(stat.name()));
            }
        }
        return values;
    }

    /** Reads every remaining row of {@code rows}, skipping zero values and unknown materials. */
    static MaterialRows readMaterials(ResultSet rows) throws SQLException {
        List<MaterialValue> values = new ArrayList<>();
        List<MaterialValue> renamedValues = new ArrayList<>();
        List<String> renamedRows = new ArrayList<>();
        while (rows.next()) {
            String name = rows.getString("material");
            Material material = materialOf(name);
            if (material == null) {
                continue;
            }
            boolean renamed = !material.name().equals(name);
            for (Statistic stat : Statistic.values()) {
                if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
                    int value = rows.getInt(materialColumn(stat));
                    if (value > 0) {
                        MaterialValue read = new MaterialValue(material, stat, value);
                        values.add(read);
                        if (renamed) {
                            renamedValues.add(read);
                        }
                    }
                }
            }
            if (renamed) {
                renamedRows.add(name);
            }
        }
        return new MaterialRows(List.copyOf(values), List.copyOf(renamedValues), List.copyOf(renamedRows));
    }

    /** Reads every remaining row of {@code rows}, skipping zero values and entity names this API lacks. */
    static EntityRows readEntities(ResultSet rows) throws SQLException {
        List<EntityValue> values = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        while (rows.next()) {
            String name = rows.getString("entity");
            EntityType entity;
            try {
                entity = EntityType.valueOf(name);
            } catch (IllegalArgumentException | NullPointerException notAnEntity) {
                unknown.add(String.valueOf(name));
                continue;
            }
            for (Statistic stat : Statistic.values()) {
                if (stat.getType() == Statistic.Type.ENTITY) {
                    int value = rows.getInt(stat.name());
                    if (value > 0) {
                        values.add(new EntityValue(entity, stat, value));
                    }
                }
            }
        }
        return new EntityRows(List.copyOf(values), List.copyOf(unknown));
    }

    /** The material table's column for {@code stat}. DROP is an SQL keyword, so its column is DROP_. */
    static String materialColumn(Statistic stat) {
        return stat.name().equals("DROP") ? "DROP_" : stat.name();
    }

    /** The current material for a stored name, also accepting an older {@code _BLOCK} or {@code _ITEM} suffix. */
    static Material materialOf(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException notCurrent) {
            try {
                return withoutBlockOrItemSuffix(name);
            } catch (IllegalArgumentException | NullPointerException unknown) {
                return null;
            }
        }
    }

    private static Material withoutBlockOrItemSuffix(String name) {
        String[] parts = name.split("_");
        String last = parts[parts.length - 1];
        if (last.equals("BLOCK") || last.equals("ITEM")) {
            return Material.valueOf(String.join("_", Arrays.copyOf(parts, parts.length - 1)));
        }
        return null;
    }

    /**
     * Writes the snapshot into the player's statistics. Main thread only. A value the server's API
     * refuses for its material or entity is skipped and logged, as before.
     */
    void applyTo(Player player, Logger log) {
        general.forEach((stat, value) -> player.setStatistic(stat, value));
        for (MaterialValue value : materials) {
            try {
                player.setStatistic(value.statistic(), value.material(), value.value());
            } catch (IllegalArgumentException ex) {
                log.log(Level.WARNING, "Skipped statistic " + value + " for " + player.getName(), ex);
            }
        }
        for (EntityValue value : entities.values()) {
            try {
                player.setStatistic(value.statistic(), value.entity(), value.value());
            } catch (IllegalArgumentException ex) {
                log.log(Level.WARNING, "Skipped statistic " + value + " for " + player.getName(), ex);
            }
        }
        if (!entities.unknown().isEmpty()) {
            log.warning("Skipped creature statistics for " + player.getName()
                    + " stored under unknown entity names " + entities.unknown());
        }
    }
}
