package com.dua3.utility.rest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/** Small HTTP/1.1 loopback server for REST client integration tests. */
final class LoopbackRestServer implements AutoCloseable {
    private static final int STARTUP_TIMEOUT_SECONDS = 5;

    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final CountDownLatch accepting = new CountDownLatch(1);
    private final List<ReceivedRequest> requests = new CopyOnWriteArrayList<>();
    private final AtomicReference<Throwable> serverFailure = new AtomicReference<>();
    private final Function<ReceivedRequest, Response> responder;
    private final URI baseUri;

    LoopbackRestServer(Function<ReceivedRequest, Response> responder) throws IOException {
        this.responder = Objects.requireNonNull(responder, "responder");
        serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        String host = serverSocket.getInetAddress().getHostAddress();
        if (host.contains(":")) {
            host = '[' + host + ']';
        }
        baseUri = URI.create("http://" + host + ':' + serverSocket.getLocalPort() + '/');
        serverThread = Thread.ofVirtual().name("rest-test-server").start(this::serve);
        awaitStartup();
    }

    URI baseUri() {
        return baseUri;
    }

    List<ReceivedRequest> requests() {
        return List.copyOf(requests);
    }

    private void awaitStartup() throws IOException {
        try {
            if (!accepting.await(STARTUP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                closeQuietly();
                throw new IOException("loopback REST test server did not start accepting connections");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            closeQuietly();
            throw new IOException("interrupted while starting loopback REST test server", exception);
        }
    }

    private void serve() {
        accepting.countDown();
        while (!serverSocket.isClosed()) {
            try (Socket socket = serverSocket.accept()) {
                serve(socket);
            } catch (SocketException exception) {
                if (!serverSocket.isClosed()) {
                    serverFailure.compareAndSet(null, exception);
                }
                return;
            } catch (Throwable throwable) {
                serverFailure.compareAndSet(null, throwable);
                return;
            }
        }
    }

    private void serve(Socket socket) throws IOException {
        ReceivedRequest request = readRequest(socket.getInputStream());
        requests.add(request);
        Response response = Objects.requireNonNull(responder.apply(request), "responder returned null");
        writeResponse(socket.getOutputStream(), response);
    }

    private static ReceivedRequest readRequest(InputStream source) throws IOException {
        BufferedInputStream input = new BufferedInputStream(source);
        String requestLine = readLine(input);
        if (requestLine == null || requestLine.isBlank()) {
            throw new IOException("empty HTTP request");
        }
        String[] requestParts = requestLine.split(" ", 3);
        if (requestParts.length != 3) {
            throw new IOException("invalid HTTP request line: " + requestLine);
        }

        Map<String, List<String>> headers = new LinkedHashMap<>();
        String line;
        while ((line = readLine(input)) != null && !line.isEmpty()) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new IOException("invalid HTTP request header: " + line);
            }
            String name = line.substring(0, separator).toLowerCase(Locale.ROOT);
            String value = line.substring(separator + 1).trim();
            headers.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
        }

        byte[] body = headers.containsKey("content-length")
                ? readFixedLengthBody(input, Integer.parseInt(headers.get("content-length").getFirst()))
                : isChunked(headers) ? readChunkedBody(input) : new byte[0];
        return new ReceivedRequest(requestParts[0], URI.create(requestParts[1]), immutableHeaders(headers), body);
    }

    private static byte[] readFixedLengthBody(InputStream input, int contentLength) throws IOException {
        byte[] body = input.readNBytes(contentLength);
        if (body.length != contentLength) {
            throw new IOException("HTTP request ended before its body was complete");
        }
        return body;
    }

    private static boolean isChunked(Map<String, List<String>> headers) {
        return headers.getOrDefault("transfer-encoding", List.of()).stream()
                .anyMatch(value -> value.equalsIgnoreCase("chunked"));
    }

    private static byte[] readChunkedBody(InputStream input) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while (true) {
            String sizeLine = Objects.requireNonNull(readLine(input), "chunked request ended before chunk size");
            int extension = sizeLine.indexOf(';');
            int chunkSize = Integer.parseInt((extension >= 0 ? sizeLine.substring(0, extension) : sizeLine).trim(), 16);
            if (chunkSize == 0) {
                while (!Objects.requireNonNull(readLine(input), "chunked request ended in trailers").isEmpty()) {
                    // Ignore trailer fields.
                }
                return body.toByteArray();
            }
            body.writeBytes(readFixedLengthBody(input, chunkSize));
            String terminator = readLine(input);
            if (terminator == null || !terminator.isEmpty()) {
                throw new IOException("chunked request has an invalid chunk terminator");
            }
        }
    }

    private static String readLine(InputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        int value;
        while ((value = input.read()) != -1) {
            if (value == '\n') {
                if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
                    line.setLength(line.length() - 1);
                }
                return line.toString();
            }
            line.append((char) value);
        }
        return line.isEmpty() ? null : line.toString();
    }

    private static Map<String, List<String>> immutableHeaders(Map<String, List<String>> headers) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        headers.forEach((name, values) -> copy.put(name, List.copyOf(values)));
        return Map.copyOf(copy);
    }

    private static void writeResponse(OutputStream output, Response response) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>(response.headers());
        headers.putIfAbsent("Content-Length", String.valueOf(response.body().length));
        headers.putIfAbsent("Connection", "close");

        output.write(("HTTP/1.1 " + response.status() + ' ' + reasonPhrase(response.status()) + "\r\n")
                .getBytes(StandardCharsets.US_ASCII));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            output.write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes(StandardCharsets.US_ASCII));
        }
        output.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        output.write(response.body());
        output.flush();
    }

    private static String reasonPhrase(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 401 -> "Unauthorized";
            case 418 -> "I'm a teapot";
            default -> "Test Response";
        };
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
        serverThread.join(TimeUnit.SECONDS.toMillis(STARTUP_TIMEOUT_SECONDS));
        if (serverThread.isAlive()) {
            throw new AssertionError("loopback REST test server did not stop within five seconds");
        }
        Throwable failure = serverFailure.get();
        if (failure != null) {
            throw new AssertionError("loopback REST test server failed", failure);
        }
    }

    private void closeQuietly() {
        try {
            close();
        } catch (Exception ignored) {
            // The original startup failure is more useful.
        }
    }

    record ReceivedRequest(String method, URI target, Map<String, List<String>> headers, byte[] body) {
        String header(String name) {
            List<String> values = headers.get(name.toLowerCase(Locale.ROOT));
            if (values == null || values.isEmpty()) {
                throw new AssertionError("request does not contain header " + name);
            }
            return values.getFirst();
        }

        String bodyAsUtf8() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    record Response(int status, Map<String, String> headers, byte[] body) {
        static Response text(int status, String body) {
            return new Response(status, Map.of("Content-Type", "text/plain; charset=utf-8"),
                    body.getBytes(StandardCharsets.UTF_8));
        }

        static Response json(int status, String body) {
            return new Response(status, Map.of("Content-Type", "application/json"),
                    body.getBytes(StandardCharsets.UTF_8));
        }
    }
}
