package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.connect.Channel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins how the proxy treats subchannels it no longer handles. Each backend keeps the Connect jar
 * it started with, so after a proxy upgrade the backends still on 3.0.3 or older go on sending
 * {@code world_uuid}, {@code afk} and {@code player} until they restart. The proxy must accept
 * and ignore those messages. {@code world_uuid} used to reach the MyWarp bridge, and with
 * {@code myWarp.enabled: false} it threw a NullPointerException on every player join.
 */
class PluginMessageHandlerTest {

    /** A backend whose every method throws, proving an ignored message never touches it. */
    private static final McmeServerInfo OLD_BACKEND = (McmeServerInfo) Proxy.newProxyInstance(
            McmeServerInfo.class.getClassLoader(),
            new Class<?>[]{McmeServerInfo.class},
            (proxy, method, args) -> {
                throw new UnsupportedOperationException(method.getName());
            });

    @Test
    void ignoresWorldUuidFromOlderBackends() {
        assertIgnored(out -> {
            out.writeUTF("world_uuid");
            out.writeUTF(UUID.randomUUID().toString());
            out.writeUTF("world");
        });
    }

    @Test
    void ignoresAfkFromOlderBackends() {
        assertIgnored(out -> {
            out.writeUTF("afk");
            out.writeUTF(UUID.randomUUID().toString());
            out.writeBoolean(true);
        });
    }

    @Test
    void ignoresPlayerListRequestsFromOlderBackends() {
        assertIgnored(out -> out.writeUTF("player"));
    }

    private static void assertIgnored(Consumer<ByteArrayDataOutput> writer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writer.accept(out);
        assertTrue(PluginMessageHandler.handlePluginMessage(Channel.MAIN, OLD_BACKEND, null, out.toByteArray()),
                "a message on the Connect channel must count as handled, so Velocity does not forward it");
    }
}
