package com.mcmiddleearth.connect.statistics;

import com.mcmiddleearth.connect.statistics.StatisticSnapshot.EntityValue;
import com.mcmiddleearth.connect.statistics.StatisticSnapshot.MaterialValue;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The database thread copies a player's statistics out of the JDBC results before the main thread
 * applies them. Until 3.1.1 the main thread read the results itself, a tick later, and by then
 * another load or a save had often re-run the shared query and closed them. Each test here reads,
 * closes the result, and only then looks at what was read.
 */
class StatisticSnapshotTest {

    @Test
    void generalValuesStayReadableAfterTheResultIsClosed() throws SQLException {
        Map<String, Object> row = zeroColumns(stat -> stat.getType() == Statistic.Type.UNTYPED, Statistic::name);
        row.put(Statistic.PLAY_ONE_MINUTE.name(), 72000);
        row.put(Statistic.JUMP.name(), 15);
        FakeResult result = new FakeResult(List.of(row));

        assertTrue(result.resultSet().next());
        Map<Statistic, Integer> general = StatisticSnapshot.readGeneral(result.resultSet());
        result.resultSet().close();

        assertEquals(72000, general.get(Statistic.PLAY_ONE_MINUTE));
        assertEquals(15, general.get(Statistic.JUMP));
        assertThrows(SQLException.class, () -> result.resultSet().getInt(Statistic.JUMP.name()),
                "the fake must refuse reads after close like Connector/J, or this test proves nothing");
    }

    @Test
    void materialRowsAreCopiedAndOldSuffixedNamesMarkedForMigration() throws SQLException {
        FakeResult rows = new FakeResult(List.of(
                materialRow("STONE", Statistic.MINE_BLOCK, 40),
                materialRow("DIRT_BLOCK", Statistic.MINE_BLOCK, 7),
                materialRow("NOT_A_MATERIAL", Statistic.MINE_BLOCK, 3)));

        StatisticSnapshot.MaterialRows read = StatisticSnapshot.readMaterials(rows.resultSet());
        rows.resultSet().close();

        assertEquals(List.of(new MaterialValue(Material.STONE, Statistic.MINE_BLOCK, 40),
                             new MaterialValue(Material.DIRT, Statistic.MINE_BLOCK, 7)),
                read.values(), "zero columns and unknown materials are skipped");
        assertEquals(List.of(new MaterialValue(Material.DIRT, Statistic.MINE_BLOCK, 7)), read.renamedValues());
        assertEquals(List.of("DIRT_BLOCK"), read.renamedRows());
    }

    @Test
    void unknownEntityNamesAreSkippedWithoutLosingTheOtherRows() throws SQLException {
        FakeResult rows = new FakeResult(List.of(
                entityRow("ZOMBIE", Statistic.KILL_ENTITY, 12),
                entityRow("NOT_A_MOB", Statistic.KILL_ENTITY, 2),
                entityRow("SKELETON", Statistic.ENTITY_KILLED_BY, 1)));

        StatisticSnapshot.EntityRows read = StatisticSnapshot.readEntities(rows.resultSet());
        rows.resultSet().close();

        assertEquals(List.of(new EntityValue(EntityType.ZOMBIE, Statistic.KILL_ENTITY, 12),
                             new EntityValue(EntityType.SKELETON, Statistic.ENTITY_KILLED_BY, 1)),
                read.values());
        assertEquals(List.of("NOT_A_MOB"), read.unknown());
    }

    private static Map<String, Object> materialRow(String material, Statistic stat, int value) {
        Map<String, Object> row = zeroColumns(
                s -> s.getType() == Statistic.Type.BLOCK || s.getType() == Statistic.Type.ITEM,
                StatisticSnapshot::materialColumn);
        row.put("material", material);
        row.put(StatisticSnapshot.materialColumn(stat), value);
        return row;
    }

    private static Map<String, Object> entityRow(String entity, Statistic stat, int value) {
        Map<String, Object> row = zeroColumns(s -> s.getType() == Statistic.Type.ENTITY, Statistic::name);
        row.put("entity", entity);
        row.put(stat.name(), value);
        return row;
    }

    private static Map<String, Object> zeroColumns(Predicate<Statistic> include,
                                                   java.util.function.Function<Statistic, String> column) {
        Map<String, Object> row = new HashMap<>();
        for (Statistic stat : Statistic.values()) {
            if (include.test(stat)) {
                row.put(column.apply(stat), 0);
            }
        }
        return row;
    }

    /** A forward-only ResultSet over in-memory rows that, like Connector/J, refuses reads after close. */
    private static final class FakeResult {
        private final List<Map<String, Object>> rows;
        private final ResultSet resultSet;
        private int index = -1;
        private boolean closed;

        FakeResult(List<Map<String, Object>> rows) {
            this.rows = rows;
            this.resultSet = (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
                        String name = method.getName();
                        if (name.equals("close")) {
                            closed = true;
                            return null;
                        }
                        if (name.equals("isClosed")) {
                            return closed;
                        }
                        if (closed) {
                            throw new SQLException("Operation not allowed after ResultSet closed");
                        }
                        return switch (name) {
                            case "next" -> ++index < this.rows.size();
                            case "getInt" -> ((Number) column(args[0])).intValue();
                            case "getString" -> (String) column(args[0]);
                            default -> throw new UnsupportedOperationException(name);
                        };
                    });
        }

        private Object column(Object label) throws SQLException {
            Object value = rows.get(index).get((String) label);
            if (value == null) {
                throw new SQLException("Column '" + label + "' not found.");
            }
            return value;
        }

        ResultSet resultSet() {
            return resultSet;
        }
    }
}
