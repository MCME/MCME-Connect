package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeContentProviderTest {

    @Test
    void testPassesThroughRowsFromHealthyProvider() {
        TabContentProvider healthy = size -> List.of(new TabRow(Component.text("ok"), null));
        AtomicInteger logCount = new AtomicInteger();
        SafeContentProvider safe = new SafeContentProvider("tips", healthy, m -> logCount.incrementAndGet());
        assertEquals(1, safe.rows(20).size());
        assertEquals(0, logCount.get());
    }

    @Test
    void testReturnsEmptyRegionWhenProviderThrows() {
        TabContentProvider broken = size -> {
            throw new IllegalStateException("boom");
        };
        SafeContentProvider safe = new SafeContentProvider("tips", broken, m -> { });
        assertTrue(safe.rows(20).isEmpty());
    }

    @Test
    void testLogsOnlyOnceAcrossRepeatedFailures() {
        TabContentProvider broken = size -> {
            throw new IllegalStateException("boom");
        };
        AtomicInteger logCount = new AtomicInteger();
        SafeContentProvider safe = new SafeContentProvider("tips", broken, m -> logCount.incrementAndGet());
        safe.rows(20);
        safe.rows(20);
        safe.rows(20);
        assertEquals(1, logCount.get());
    }

    @Test
    void testResetAllowsLoggingAgainAfterReload() {
        TabContentProvider broken = size -> {
            throw new IllegalStateException("boom");
        };
        AtomicInteger logCount = new AtomicInteger();
        SafeContentProvider safe = new SafeContentProvider("tips", broken, m -> logCount.incrementAndGet());
        safe.rows(20);
        safe.resetFailureLatch();
        safe.rows(20);
        assertEquals(2, logCount.get());
    }
}
