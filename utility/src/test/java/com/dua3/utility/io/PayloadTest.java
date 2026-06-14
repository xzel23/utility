package com.dua3.utility.io;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayloadTest {

    @TempDir
    Path tempDir;

    private enum ResourceKind {
        FILE,
        NETWORK
    }

    private enum AccessKind {
        STREAM,
        CHANNEL
    }

    private record OpenedPayload(Payload payload, Resource resource) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            payload.close();
            resource.close();
        }
    }

    private interface Resource extends AutoCloseable {
        URI uri();
    }

    private record FileResource(URI uri) implements Resource {
        @Override
        public void close() {
            // nothing to close
        }
    }

    private static final class OneShotHttpResource implements Resource {
        private final ServerSocket serverSocket;
        private final Thread serverThread;
        private final AtomicReference<Throwable> serverError = new AtomicReference<>();
        private final URI uri;

        OneShotHttpResource(byte[] body) throws IOException {
            serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            uri = URI.create("http://127.0.0.1:" + serverSocket.getLocalPort() + "/payload");
            serverThread = Thread.ofVirtual().start(() -> serve(body));
        }

        private void serve(byte[] body) {
            try (Socket socket = serverSocket.accept()) {
                OutputStream out = socket.getOutputStream();
                out.write(("HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Length: " + body.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n").getBytes(StandardCharsets.US_ASCII));
                out.write(body);
                out.flush();
            } catch (SocketException ignored) {
                // expected if the server is closed while waiting for a request
            } catch (Throwable t) {
                serverError.compareAndSet(null, t);
            }
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            serverThread.join(5_000);
            Throwable error = serverError.get();
            if (error != null) {
                throw new AssertionError("Network test server failed", error);
            }
        }
    }

    private static Stream<Arguments> resourceAndAccessKinds() {
        return Stream.of(ResourceKind.values())
                .flatMap(resourceKind -> Stream.of(AccessKind.values())
                        .map(accessKind -> Arguments.of(resourceKind, accessKind)));
    }

    private static Stream<Arguments> resourceKindsAndPayloadSizes() {
        return Stream.of(ResourceKind.values())
                .flatMap(resourceKind -> Stream.of(0, 1, 7, 8, 9, 16)
                        .map(size -> Arguments.of(resourceKind, size)));
    }

    @DisplayName("Reads identical bytes from payload stream/channel for file and network URIs")
    @ParameterizedTest(name = "[{index}] resource={0}, access={1}")
    @MethodSource("resourceAndAccessKinds")
    void contentRoundTrip_forAllStreamChannelAndResourceCombinations(ResourceKind resourceKind, AccessKind accessKind) throws Exception {
        byte[] content = "payload-content-1234567890".getBytes(StandardCharsets.UTF_8);
        try (OpenedPayload opened = openPayload(resourceKind, content)) {
            assertArrayEquals(content, readContent(opened.payload(), accessKind));
        }
    }

    @DisplayName("Enforces single-consumption: second accessor call always fails")
    @ParameterizedTest(name = "[{index}] resource={0}, firstAccess={1}")
    @MethodSource("resourceAndAccessKinds")
    void payloadCanOnlyBeConsumedOnce_forAllResourceAndAccessorCombinations(ResourceKind resourceKind, AccessKind firstAccess) throws Exception {
        byte[] content = "single-consume-check".getBytes(StandardCharsets.UTF_8);
        AccessKind secondAccess = firstAccess == AccessKind.STREAM ? AccessKind.CHANNEL : AccessKind.STREAM;

        try (OpenedPayload opened = openPayload(resourceKind, content)) {
            assertArrayEquals(content, readContent(opened.payload(), firstAccess));
            assertThrows(IllegalStateException.class, () -> readContent(opened.payload(), secondAccess));
        }
    }

    @DisplayName("Computes magic8Bytes from first up-to-8 bytes and preserves source URI")
    @ParameterizedTest(name = "[{index}] resource={0}, payloadSize={1}")
    @MethodSource("resourceKindsAndPayloadSizes")
    void magicBytesMatchFirstEightBytes_forFileAndNetworkResources(ResourceKind resourceKind, int size) throws Exception {
        byte[] content = new byte[size];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i + 1);
        }

        try (OpenedPayload opened = openPayload(resourceKind, content)) {
            assertEquals(expectedMagic8Bytes(content), opened.payload().magic8Bytes());
            assertEquals(opened.resource().uri(), opened.payload().uri().orElse(null));
        }
    }

    private OpenedPayload openPayload(ResourceKind resourceKind, byte[] content) throws Exception {
        Resource resource = openResource(resourceKind, content);
        try {
            return new OpenedPayload(Payload.fromUri(resource.uri()), resource);
        } catch (Throwable t) {
            resource.close();
            throw t;
        }
    }

    private Resource openResource(ResourceKind resourceKind, byte[] content) throws IOException {
        return switch (resourceKind) {
            case FILE -> {
                Path path = Files.createTempFile(tempDir, "payload-", ".bin");
                Files.write(path, content);
                yield new FileResource(path.toUri());
            }
            case NETWORK -> new OneShotHttpResource(Arrays.copyOf(content, content.length));
        };
    }

    private static byte[] readContent(Payload payload, AccessKind accessKind) throws IOException {
        return switch (accessKind) {
            case STREAM -> {
                try (InputStream in = payload.stream()) {
                    yield in.readAllBytes();
                }
            }
            case CHANNEL -> {
                try (ReadableByteChannel channel = payload.channel();
                     InputStream in = Channels.newInputStream(channel)) {
                    yield in.readAllBytes();
                }
            }
        };
    }

    private static long expectedMagic8Bytes(byte[] content) {
        long value = 0;
        int bytesRead = Math.min(content.length, 8);
        for (int i = 0; i < bytesRead; i++) {
            value = (value << 8) | (content[i] & 0xFFL);
        }
        if (bytesRead < 8) {
            value <<= (8 - bytesRead) * 8;
        }
        return value;
    }
}
