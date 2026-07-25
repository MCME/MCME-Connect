package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Shows a rotating window of configured tips. The clock is injected as a seconds supplier so
 * rotation is deterministic under test rather than wall-clock dependent.
 */
public class TipsProvider implements TabContentProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final List<String> tips;
    private final String headerTemplate;
    private final RowTemplate rowTemplate;
    private final String iconTexture;
    private final int windowSize;
    private final int rotateSeconds;
    private final LongSupplier secondsSupplier;

    public TipsProvider(List<String> tips, String headerTemplate, RowTemplate rowTemplate,
                        String iconTexture, int windowSize, int rotateSeconds,
                        LongSupplier secondsSupplier) {
        this.tips = List.copyOf(tips);
        this.headerTemplate = headerTemplate;
        this.rowTemplate = rowTemplate;
        this.iconTexture = iconTexture;
        this.windowSize = Math.max(1, windowSize);
        this.rotateSeconds = rotateSeconds;
        this.secondsSupplier = secondsSupplier;
    }

    @Override
    public List<TabRow> rows(int regionSize) {
        if (tips.isEmpty() || regionSize <= 0) {
            return List.of();
        }
        List<TabRow> rows = new ArrayList<>();
        rows.add(new TabRow(MINI_MESSAGE.deserialize(headerTemplate), iconTexture));
        int offset = windowOffset();
        for (int i = 0; i < windowSize && i < tips.size(); i++) {
            rows.add(new TabRow(rowTemplate.render(tips.get((offset + i) % tips.size())), iconTexture));
        }
        return rows.size() > regionSize ? rows.subList(0, regionSize) : rows;
    }

    private int windowOffset() {
        if (rotateSeconds <= 0) {
            return 0;
        }
        long step = secondsSupplier.getAsLong() / rotateSeconds;
        return (int) ((step * windowSize) % tips.size());
    }
}
