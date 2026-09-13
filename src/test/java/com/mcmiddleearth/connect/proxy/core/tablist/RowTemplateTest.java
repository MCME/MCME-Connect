package com.mcmiddleearth.connect.proxy.core.tablist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RowTemplateTest {

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void testSubstitutesTextPlaceholder() {
        RowTemplate template = new RowTemplate("<white><text>");
        assertEquals("Server meeting Sunday", plain(template.render("Server meeting Sunday")));
    }

    @Test
    void testAppliesMiniMessageStyling() {
        RowTemplate template = new RowTemplate("<gold><text>");
        Component rendered = template.render("Tips");
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.GOLD, rendered.color());
    }

    @Test
    void testTreatsContentAsLiteralNotMarkup() {
        RowTemplate template = new RowTemplate("<white><text>");
        assertEquals("<red>not styled", plain(template.render("<red>not styled")));
    }

    @Test
    void testEmptyContentRendersEmptyText() {
        RowTemplate template = new RowTemplate("<white><text>");
        assertEquals("", plain(template.render("")));
    }
}
