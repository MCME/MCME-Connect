package com.mcmiddleearth.connect.proxy.velocity;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * velocity-plugin.json is generated at compile time from the {@code @Plugin} annotation on
 * ConnectVelocityPlugin, and an annotation value must be a compile-time constant, so the version
 * is a hardcoded literal that Maven cannot substitute.
 *
 * <p>The two drifted on 2026-09-21: the pom said 3.0.2 while the jar told the proxy 3.0.1. That is
 * how this project ended up with two different production jars both declaring 2.0.1, one of which
 * hijacked /warp - indistinguishable from the proxy's plugin list.
 */
class VelocityPluginDescriptorTest {

    @Test
    void testTheGeneratedDescriptorDeclaresThePomVersion() throws Exception {
        String pomVersion = System.getProperty("project.version");
        assertNotNull(pomVersion, "surefire must pass project.version through; see the pom");

        String json;
        try (InputStream in = getClass().getResourceAsStream("/velocity-plugin.json")) {
            assertNotNull(in, "velocity-plugin.json is generated into target/classes at compile time");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        Matcher version = Pattern.compile("\"version\"\s*:\s*\"([^\"]+)\"").matcher(json);
        assertTrue(version.find(), "descriptor declares no version: " + json);
        assertEquals(pomVersion, version.group(1),
                "the @Plugin annotation's version must be bumped alongside the pom");
    }
}
